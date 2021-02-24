package com.example.collector

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.example.collector.CustomViewHolder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_collector.*
import kotlinx.android.synthetic.main.images_row.view.*

class ValidateImageAdapter(val images: String): RecyclerView.Adapter<CustomViewHolder>() {
    val videoTitles = listOf<String>("Fisrt", "Second", "Third", "Forth", "Fifth")
    val image_urls =  images.replace("\\s+".toRegex(), " ").trim().split(" ")
//    var update_urls = ""

    // number of items
    override fun getItemCount(): Int {
        return image_urls.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        println(image_urls)
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.images_row, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val videoTitle = videoTitles.get(position)
        val imageURL = image_urls.get(position)

        holder.view.textViewImageTitle.text = videoTitle
//        Picasso.get().load(imageURL).into(holder.view.imageViewRow)

//        holder.view.btn_inbox_takepic.setOnClickListener{
//            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            try {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            } catch (e: ActivityNotFoundException) {
//                Toast.makeText(this, "cannot open cam", Toast.LENGTH_SHORT).show()
//            }
//        }

//        holder.view.checkImage.setOnCheckedChangeListener() {
//            val checked: Boolean = holder.view.checkImage.isChecked
//            if (checked) {
//                update_urls = ""
//            }
//        }
//        fun onCheckboxClicked(view: View) {
//            if (view is CheckBox) {
//                val checked: Boolean = view.isChecked
//
//                when (view.id) {
//                    R.id.checkbox_meat -> {
//                        if (checked) {
//                            // Put some meat on the sandwich
//                        } else {
//                            // Remove the meat
//                        }
//                    }
//                    R.id.checkbox_cheese -> {
//                        if (checked) {
//                            // Cheese me
//                        } else {
//                            // I'm lactose intolerant
//                        }
//                    }
//                    // TODO: Veggie sandwich
//                }
//            }
//        }
    }

}
