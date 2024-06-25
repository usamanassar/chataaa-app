package com.usama.chatapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.usama.chatapp.databinding.ActivityVerificationBinding

class Verification : AppCompatActivity() {
    private var binding: ActivityVerificationBinding? = null
    private var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null) {
            val intent = Intent(this@Verification, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            supportActionBar?.hide()
            binding?.editNumber?.requestFocus()
            binding?.continuebtn?.setOnClickListener {
                val intent = Intent(this@Verification, OtpActivity::class.java)
                intent.putExtra("phoneNumber", binding?.editNumber?.text.toString())
                startActivity(intent)
            }
        }
    }
}