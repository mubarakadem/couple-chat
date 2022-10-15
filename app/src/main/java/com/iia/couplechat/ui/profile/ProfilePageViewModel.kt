package com.iia.couplechat.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iia.couplechat.data.model.User
import com.iia.couplechat.data.util.FirebaseStorageReferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.SecureRandom
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration

@HiltViewModel
class ProfilePageViewModel : ViewModel() {
    val uiState = MutableStateFlow(ProfilePageState())
    private val auth = Firebase.auth.currentUser
    private val fireStore = Firebase.firestore
    private val storage = Firebase.storage
    private val storageReference = storage.reference
    private val profilePictureReference =
        storageReference.child(FirebaseStorageReferences.PROFILE_PICTURE_REFERENCE).child(auth?.uid!!)
    private val currentUser = Firebase.auth.currentUser

    fun handleEvent(event: ProfilePageEvent) {
        when (event) {
            is ProfilePageEvent.FirstNameChanged -> firstNameChanged(event.firstName)
            is ProfilePageEvent.LastNameChanged -> lastNameChanged(event.lastName)
            ProfilePageEvent.OnSave -> save()
            is ProfilePageEvent.MessageChanged -> messageChanged(event.message)
            is ProfilePageEvent.ShowPermissionChanged -> showPermissionChanged(event.shouldShowPermission)
            is ProfilePageEvent.PermissionGrantedChanged -> permissionGrantedChanged(event.permissionGranted)
            is ProfilePageEvent.ImageUriChanged -> imageUriChanged(event.imageUri)
            is ProfilePageEvent.LoadingChanged -> loadingChanged(event.loading)
        }
    }

    private fun firstNameChanged(firstName: String) {
        uiState.value = uiState.value.copy(firstName = firstName)
    }

    private fun lastNameChanged(lastName: String) {
        uiState.value = uiState.value.copy(lastName = lastName)
    }

    private fun save() {
        loadingChanged(true)
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)

        var user = User(
            userId = currentUser?.uid,
            firstName = uiState.value.firstName,
            lastName = uiState.value.lastName,
            invitationCode = bytes.toString(),
            invitationCodeExpireDate = LocalDate.now().plusDays(7).toString()
        )
        if (uiState.value.imageUri != null) {
            val fileReference = profilePictureReference.child("${UUID.randomUUID()}.jpg")
            fileReference.putFile(uiState.value.imageUri!!).addOnSuccessListener { taskSnapshot ->
                if (taskSnapshot.task.isSuccessful) {
                    user = user.copy(profilePictureUri = fileReference.path)
                    Log.d("TAG", "save: successfully uploaded")
                    saveOrUpdateUser(user)
                } else {
                    Log.d("TAG", "save: not successful")
                }
            }.addOnFailureListener { exception ->
                Log.d("TAG", "save: not successful: ${exception.message}")
            }
        } else {
            saveOrUpdateUser(user)
        }
    }

    private fun messageChanged(message: String) {
        uiState.value = uiState.value.copy(message = message)
    }

    private fun showPermissionChanged(shouldShowPermission: Boolean) {
        uiState.value = uiState.value.copy(shouldShowPermission = shouldShowPermission)
    }

    private fun permissionGrantedChanged(permissionGranted: Boolean) {
        uiState.value = uiState.value.copy(permissionGranted = permissionGranted)
    }

    private fun imageUriChanged(imageUri: Uri?) {
        uiState.value = uiState.value.copy(imageUri = imageUri)
    }

    private fun saveOrUpdateUser(user: User) {
        fireStore.collection("users").whereEqualTo("userId", auth?.uid).get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("TAG", "saveUser: query success")
                if (querySnapshot.documents.isNotEmpty()) {
                    val docReference = querySnapshot.documents[0].reference
                    docReference.update(
                        mapOf(
                            "firstName" to user.firstName,
                            "lastName" to user.lastName,
                            "profilePictureUri" to user.profilePictureUri,

                        ))
                        .addOnSuccessListener {
                            Log.d("TAG", "saveUser: successfully updated")
                            loadingChanged(false)
                        }
                        .addOnFailureListener {
                            Log.d("TAG", "saveUser: update failed $it")
                            loadingChanged(false)
                        }
                } else {
                    saveUser(user)
                }
            }.addOnFailureListener {
                Log.d("TAG", "saveUser: query failed $it")
            }

    }

    private fun saveUser(user: User) {
        fireStore.collection("users").add(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                messageChanged("Successfully Saved")
                Log.d("TAG", "save: success you son of a bitch")
                loadingChanged(false)
            } else {
                messageChanged("Not Successfully")
                Log.d("TAG", "save: not success you son of a bitch")
                loadingChanged(false)
            }
        }.addOnFailureListener { exception ->
            exception.message?.let { messageChanged(it) }
            loadingChanged(false)
        }
    }

    private fun loadingChanged(loading: Boolean) {
        uiState.value = uiState.value.copy(loading = loading)
    }
}