package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Meal
import com.example.model.MenuItem
import com.example.model.UserGoal
import com.example.model.UserProfile
import com.example.model.WeightLog
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CalorieRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(explicitUserId: String? = null): String {
        return explicitUserId ?: auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing Firestore operations.")
    }

    fun observeMeals(userId: String, dateString: String): Flow<List<Meal>> = flow {
        val path = "users/$userId/meals"
        val query = db.collection("users").document(userId).collection("meals")
            .whereEqualTo("dateString", dateString)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Meal::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeMenuItems(userId: String): Flow<List<MenuItem>> = flow {
        val path = "users/$userId/menu_items"
        val query = db.collection("users").document(userId).collection("menu_items")

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.frequencyCount }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeUserGoal(userId: String): Flow<UserGoal?> = flow {
        val path = "users/$userId/goals/current"
        val docRef = db.collection("users").document(userId).collection("goals").document("current")

        docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserGoal::class.java)?.copy(id = snapshot.id)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun addMeal(meal: Meal, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docId = if (meal.id.isNotBlank()) meal.id else UUID.randomUUID().toString()
        val mealsRef = db.collection("users").document(uid).collection("meals").document(docId)

        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "name" to meal.name,
            "mealType" to meal.mealType,
            "calories" to meal.calories,
            "protein" to meal.protein,
            "carbs" to meal.carbs,
            "fat" to meal.fat,
            "fiber" to meal.fiber,
            "servings" to meal.servings,
            "contextNotes" to meal.contextNotes,
            "imageUrl" to meal.imageUrl,
            "dateString" to meal.dateString,
            "hourOfDay" to meal.hourOfDay,
            "ingredientsJson" to meal.ingredientsJson,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            mealsRef.set(payload).await()
            // Auto-upsert into menu_items so frequent foods are remembered
            syncMealToMenuItem(meal, uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, mealsRef.path)
            throw e
        }
    }

    private suspend fun syncMealToMenuItem(meal: Meal, userId: String) {
        try {
            val menuCollection = db.collection("users").document(userId).collection("menu_items")
            val existing = menuCollection.whereEqualTo("name", meal.name).limit(1).get().await()

            if (!existing.isEmpty) {
                val doc = existing.documents[0]
                doc.reference.update(
                    mapOf(
                        "frequencyCount" to FieldValue.increment(1),
                        "lastLoggedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val singleServingCalories = if (meal.servings > 0) meal.calories / meal.servings else meal.calories
                val singleProtein = if (meal.servings > 0) meal.protein / meal.servings else meal.protein
                val singleCarbs = if (meal.servings > 0) meal.carbs / meal.servings else meal.carbs
                val singleFat = if (meal.servings > 0) meal.fat / meal.servings else meal.fat
                val singleFiber = if (meal.servings > 0) meal.fiber / meal.servings else meal.fiber

                val menuItemId = UUID.randomUUID().toString()
                val menuPayload = hashMapOf<String, Any>(
                    "id" to menuItemId,
                    "name" to meal.name,
                    "calories" to singleServingCalories,
                    "protein" to singleProtein,
                    "carbs" to singleCarbs,
                    "fat" to singleFat,
                    "fiber" to singleFiber,
                    "defaultPortion" to "1 serving",
                    "category" to meal.mealType,
                    "frequencyCount" to 1L,
                    "lastLoggedAt" to FieldValue.serverTimestamp(),
                    "userId" to userId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                menuCollection.document(menuItemId).set(menuPayload).await()
            }
        } catch (e: Exception) {
            // Non-critical background sync
        }
    }

    suspend fun updateMeal(meal: Meal, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docRef = db.collection("users").document(uid).collection("meals").document(meal.id)
        val payload = hashMapOf<String, Any>(
            "id" to meal.id,
            "name" to meal.name,
            "mealType" to meal.mealType,
            "calories" to meal.calories,
            "protein" to meal.protein,
            "carbs" to meal.carbs,
            "fat" to meal.fat,
            "fiber" to meal.fiber,
            "servings" to meal.servings,
            "contextNotes" to meal.contextNotes,
            "imageUrl" to meal.imageUrl,
            "dateString" to meal.dateString,
            "hourOfDay" to meal.hourOfDay,
            "ingredientsJson" to meal.ingredientsJson,
            "userId" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMeal(mealId: String, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docRef = db.collection("users").document(uid).collection("meals").document(mealId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun saveUserGoal(goal: UserGoal, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docRef = db.collection("users").document(uid).collection("goals").document("current")
        val payload = hashMapOf<String, Any>(
            "id" to "current",
            "calorieGoal" to goal.calorieGoal,
            "proteinGoal" to goal.proteinGoal,
            "carbsGoal" to goal.carbsGoal,
            "fatGoal" to goal.fatGoal,
            "goalType" to goal.goalType,
            "goalWeightKg" to goal.goalWeightKg,
            "startWeightKg" to goal.startWeightKg,
            "paceKgPerWeek" to goal.paceKgPerWeek,
            "manual" to goal.manual,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    /** Meals from [fromDate] (yyyy-MM-dd) onwards, used for the week strip and the history calendar. */
    fun observeMealsSince(userId: String, fromDate: String): Flow<List<Meal>> = flow {
        val path = "users/$userId/meals"
        val query = db.collection("users").document(userId).collection("meals")
            .whereGreaterThanOrEqualTo("dateString", fromDate)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Meal::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId).collection("profile").document("current")
        docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun saveProfile(profile: UserProfile, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docRef = db.collection("users").document(uid).collection("profile").document("current")
        val payload = hashMapOf<String, Any>(
            "name" to profile.name,
            "sex" to profile.sex,
            "age" to profile.age,
            "heightCm" to profile.heightCm,
            "activity" to profile.activity,
            "dietTags" to profile.dietTags,
            "userId" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    /** Latest weigh-ins, newest first. */
    fun observeWeights(userId: String): Flow<List<WeightLog>> = flow {
        val path = "users/$userId/weights"
        val query = db.collection("users").document(userId).collection("weights")
            .orderBy("dateString", Query.Direction.DESCENDING)
            .limit(120)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(WeightLog::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    /** One weigh-in per day: logging again on the same day replaces it. */
    suspend fun saveWeight(kg: Double, dateString: String, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docRef = db.collection("users").document(uid).collection("weights").document(dateString)
        val payload = hashMapOf<String, Any>(
            "dateString" to dateString,
            "kg" to kg,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addCustomMenuItem(item: MenuItem, explicitUserId: String? = null) {
        val uid = requireUserId(explicitUserId)
        val docId = if (item.id.isNotBlank()) item.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("menu_items").document(docId)
        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "name" to item.name,
            "calories" to item.calories,
            "protein" to item.protein,
            "carbs" to item.carbs,
            "fat" to item.fat,
            "fiber" to item.fiber,
            "defaultPortion" to item.defaultPortion,
            "category" to item.category,
            "frequencyCount" to item.frequencyCount,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
