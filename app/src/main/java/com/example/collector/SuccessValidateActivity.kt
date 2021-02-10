package com.example.collector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_success_validate.*

class SuccessValidateActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_success_validate)

            button_to_outbox.setOnClickListener{
                val intent = Intent(this, OutboxActivity::class.java)
                startActivity(intent)
            }
        }
}
