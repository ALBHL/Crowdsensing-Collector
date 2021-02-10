package com.example.collector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.images_row.view.*


class ShowImageAdapter(val images: String): RecyclerView.Adapter<CustomViewHolder>() {
    val videoTitles = listOf<String>("Fisrt", "Second", "Third", "Forth", "Fifth")
    val image_urls =  images.replace("\\s+".toRegex(), " ").trim().split(" ")

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
        Picasso.get().load(imageURL).into(holder.view.imageViewRow)
    }

}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) {

}
