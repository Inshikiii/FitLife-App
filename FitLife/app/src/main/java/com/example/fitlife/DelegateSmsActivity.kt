package com.example.fitlife

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class DelegateSmsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delegate_sms)

        val etMessage = findViewById<EditText>(R.id.etSmsMessage)
        val btnSend = findViewById<Button>(R.id.btnSendSms)

        // Get checklist text passed from previous screen
        val message = intent.getStringExtra("sms_text") ?: ""
        etMessage.setText(message)

        btnSend.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", etMessage.text.toString())
            }
            startActivity(smsIntent)
        }
    }
}
