package io.github.abdulroufsidhu.easy_firebase_auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "my-google-auther"

class MyGoogleAuthenticator(private val serverToken: String) {

    private lateinit var signinClient: GoogleSignInClient

    fun getSignInIntent() = signinClient.signInIntent

    fun initiate(activity: Activity) {
        val signinOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverToken)
            .requestEmail()
            .build()
        signinClient = GoogleSignIn.getClient(activity, signinOptions)
        Firebase.auth
    }

    fun onResult(activity: Activity, data: Intent?, onSuccess: (Task<AuthResult>)->Unit, onFailure: (Throwable)->Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            account.idToken?.let { token ->
                firebaseAuthWithGoogle(
                    token,
                    activity,
                    onSuccess,
                    onFailure
                )
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String, activity: Activity, onSuccess: (Task<AuthResult>) -> Unit, onFailure: (Throwable) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    onSuccess(task)
                } else {
                    // If sign in fails, display a message to the user.
                    onFailure(Throwable("SignIn Failed"))
                }
            }
            .addOnFailureListener(activity) {exception->
                onFailure(exception)
            }
    }
    // [END auth_with_google]
}
