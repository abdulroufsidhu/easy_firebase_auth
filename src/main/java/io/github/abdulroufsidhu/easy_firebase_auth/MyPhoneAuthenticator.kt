package io.github.abdulroufsidhu.easy_firebase_auth

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

private const val TAG = "my-phone-auther"

class MyPhoneAuthenticator {
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var onCodeSent: ((String)-> PhoneAuthCredential)? = null
    private var onSuccess: ((Task<AuthResult>)->Unit)? = null

    fun initiate() {
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]
    }

    fun startPhoneNumberVerification(activity: Activity, phoneNumber: String, onCodeSent: (String,)-> PhoneAuthCredential, onSuccess: (Task<AuthResult>)->Unit) {
        // [START setting_callbacks]
        this.onCodeSent = onCodeSent
        this.onSuccess = onSuccess
        // [END setting_callbacks]


        // [START callback_definition]
        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Log.d(TAG, "onCodeAutoRetrievalTimeOut: $p0")
            }

            override fun onCodeSent(varificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(varificationId, token)
                resendingToken = token
                Log.d(TAG, "onCodeSent: varificationId: $varificationId")
                signInWithPhoneAuthCredential( activity, onCodeSent(varificationId,), onSuccess )
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.i(TAG, "onVerificationCompleted: varification complete")
                signInWithPhoneAuthCredential(activity, credential, onSuccess)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.w(TAG, "onVerificationFailed: $exception", exception)
            }
        }
        // [END callback_definition]

        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callback) // OnVerificationStateChangedCallbacks
        resendingToken?.let {
            options.setForceResendingToken(it)
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
        // [END start_phone_auth]
    }

    // [START resend_verification]
    fun resendVerificationCode(
        activity: Activity,
        phoneNumber: String,
    ) {
        if (this.onCodeSent == null) throw Throwable("initiate before resending")
        if (this.onCodeSent == null) throw Throwable("initiate before resending")
        if (this.resendingToken == null) throw Throwable("initiate before resending")
        startPhoneNumberVerification(activity, phoneNumber, this.onCodeSent!!, this.onSuccess!! )
    }
    // [END resend_verification]

    fun verifyPhoneNumberWithCode(verificationId: String?, code: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId!!, code)
    }

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(activity: Activity, credential: PhoneAuthCredential, onSuccess: (Task<AuthResult>) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    resendingToken = null
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    Log.d(TAG, "signInWithCredential:success -> $user ")
                    onSuccess(task)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    // [END sign_in_with_phone]
}
