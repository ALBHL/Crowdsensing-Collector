package com.example.collector

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_collector.*
import java.io.ByteArrayOutputStream

class CollectorActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var imageBitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector)

        val cur_id = intent.getStringExtra(InboxActivity.ROW_ID)

        val context = this
        val db = DataBaseHandler(context)

        button_take_picture.setOnClickListener{
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "cannot open cam", Toast.LENGTH_SHORT).show()
            }
        }

        button_confirml.setOnClickListener{
            val bos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val img: ByteArray = bos.toByteArray()

            if (cur_id != null) {
                db.updateRowImg(cur_id, img)
                db.updateRow(cur_id, "ready to be validated")
            }

            val intent = Intent(this, InferencerActivity::class.java)
            intent.putExtra("ROW_ID", cur_id)
            startActivity(intent)
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            imageView_taken.setImageBitmap(imageBitmap)
        }
    }

}