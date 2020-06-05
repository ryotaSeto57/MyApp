package com.example.myapp.applist

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.myapp.AppData

@BindingAdapter("appName")
fun TextView.setAppDataName(item: AppData){
    text = item.name
}

@BindingAdapter("appIcon")
fun ImageView.setIconImage(item: AppData){
    setImageDrawable(item.icon)
}