package com.example.myapp.applist

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.myapp.database.AppData

@BindingAdapter("appIcon")
fun ImageView.setIconImage(item: AppData){
    setImageDrawable(item.icon)
}
