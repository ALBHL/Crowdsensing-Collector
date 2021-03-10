package com.example.collector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_image.*
import kotlinx.android.synthetic.main.activity_validate_image.*
import kotlinx.android.synthetic.main.activity_setdata.*

class ValidateImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validate_image)

        val images = intent.getStringExtra(InboxActivity.USER_KEY)
        val cur_id = intent.getStringExtra(InboxActivity.ROW_ID)
        val cur_name = intent.getStringExtra(InboxActivity.ROW_NAME)
        val context = this
        val db = DataBaseHandler(context)
        val data = db.readData()

        textview_metadata.text = ""
        textview_metadata.text = "Inbox\n"
        for (i in 0 until data.size) {
            textview_metadata.append(data[i].id.toString() + " " + data[i].name + " " + data[i].age + data[i].imageurl +
                    "STAGE: " + data[i].cur_stage + "\n")
        }

        recyclerViewContents.layoutManager = LinearLayoutManager(this)
        recyclerViewContents.adapter = images?.let { ValidateImageAdapter(it) }

        buttonDisapprove.setOnClickListener {
            if (cur_id != null) {
                db.updateRow(cur_id, "deleted")
            }
            val intent = Intent(this, SuccessValidateActivity::class.java)
            startActivity(intent)
        }

        ButtonValidation.setOnClickListener {
            Toast.makeText(context,cur_id, Toast.LENGTH_SHORT).show()
            if (cur_id != null) {
                db.updateRow(cur_id, "ready to be validated")
            }
            val intent = Intent(this, SuccessValidateActivity::class.java)
            startActivity(intent)
        }
    }
}
