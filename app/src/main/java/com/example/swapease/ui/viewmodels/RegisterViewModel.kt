import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapease.data.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.Result

class RegisterViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> get() = _registrationResult

    private val _googleSignInResult = MutableLiveData<Result<String>>()
    val googleSignInResult: LiveData<Result<String>> get() = _googleSignInResult

    fun registerWithEmailAndPassword(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val userId = firebaseAuth.currentUser?.uid ?: ""
                val newUser = User(uid = userId, username = username, email = email, null)
                firestore.collection("users").document(userId).set(newUser).await()
                sendVerificationEmail()
                _registrationResult.value = Result.success("Registration successful")
            } catch (e: Exception) {
                _registrationResult.value = Result.failure(e)
            }
        }
    }

    fun signInWithGoogle(idToken: String?,data: Intent?) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()

                val googleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(data).await()
                val userId = firebaseAuth.currentUser?.uid ?: ""
                val username = googleSignInAccount.displayName ?: ""
                val email = googleSignInAccount.email ?: ""
                val newUser = User(uid = userId, username = username, email = email, null)

                firestore.collection("users").document(userId).set(newUser).await()

                _googleSignInResult.value = Result.success("Google Sign-In successful")
            } catch (e: Exception) {
                _googleSignInResult.value = Result.failure(e)
            }
        }
    }

    private suspend fun sendVerificationEmail() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.await()
    }
}
