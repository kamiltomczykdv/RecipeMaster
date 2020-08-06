package com.example.recipemaster.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipemaster.R


class ImageAdapter(
    private val context: Context,
    private val urls: ArrayList<String>,
    private val clickListener: (ImageView) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.photo_iv)
        fun bind(image: ImageView, click: (ImageView) -> Unit) {
            itemView.setOnClickListener {
                click(image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_item, parent, false)
        )

    override fun getItemCount(): Int = urls.size

    override fun onBindViewHolder(holder: ImageAdapter.ViewHolder, position: Int) {
        Glide
            .with(context)
            .load(urls[position])
            .override(400, 400)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.image)

        holder.bind(holder.image, clickListener)
    }
}