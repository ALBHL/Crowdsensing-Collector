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
        val images = intent.getStringExtra(InboxActivity.USER_KEY)
        val task_id = intent.getStringExtra(InboxActivity.ROW_ID)
        val cur_name = intent.getStringExtra(InboxActivity.ROW_NAME)
        val location_data = intent.getStringExtra(InboxActivity.USER_LOCATION)
        // test purpose
        Toast.makeText(this, "task_id" + task_id, Toast.LENGTH_SHORT).show()
        val context = this
        val db = DataBaseHandler(context)

        button_take_picture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "cannot open cam", Toast.LENGTH_SHORT).show()
            }
        }

        button_confirml.setOnClickListener {
            val bos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val img: ByteArray = bos.toByteArray()

            if (task_id != null) {
                db.updateRowImgByTaskId(task_id, img)
                db.updateRowByTaskId(task_id, "ready to be validated")
            }

            val intent = Intent(this, InferencerActivity::class.java)
            intent.putExtra(InboxActivity.USER_KEY, images)
            intent.putExtra(InboxActivity.ROW_ID, task_id)
            intent.putExtra(InboxActivity.ROW_NAME, cur_name)
            intent.putExtra(InboxActivity.USER_LOCATION, location_data)
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