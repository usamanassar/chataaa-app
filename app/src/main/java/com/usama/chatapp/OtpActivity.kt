package com.usama.chatapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.usama.chatapp.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var dialog: ProgressDialog? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /** ok now add firebase to*/

        dialog = ProgressDialog(this@OtpActivity).apply {
            setMessage("Sending OTP.....")
            setCancelable(false)
            show()
        }

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        val phoneNumber = intent.getStringExtra("phoneNumber")
        binding.phone.text = "Verify $phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this@OtpActivity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    // Handle Verification completed
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle Verification failed
                    dialog?.dismiss()
                    // Optionally, show a message to the user
                }

                @SuppressLint("ServiceCast")
                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog?.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.otpView.setOtpCompletionListener { otp ->
            verificationId?.let {
                val credential = PhoneAuthProvider.getCredential(it, otp)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@OtpActivity,"Successful",Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@OtpActivity,SetupProfileActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(this@OtpActivity,"Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
