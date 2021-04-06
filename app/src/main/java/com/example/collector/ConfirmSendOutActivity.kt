package com.example.collector

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_cofirm_send_out.*

class ConfirmSendOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cofirm_send_out)

        val cur_id = intent.getStringExtra(OutboxActivity.USER_ID)

        val context = this
        val db = DataBaseHandler(context)

        val name = cur_id?.let{ db.readDataName(it) }
        txt_out_title.text = name

        val bmp = cur_id?.let { db.readDataImg(it) }
        imageView_out.setImageBitmap(bmp)


        val inference_ret = cur_id?.let{ db.readDataItem(it) }
        txt_inference_rtval.text = inference_ret

        button_back_to_outbox.setOnClickListener{
            val intent = Intent(this, OutboxActivity::class.java)
            startActivity(intent)
        }
    }

}