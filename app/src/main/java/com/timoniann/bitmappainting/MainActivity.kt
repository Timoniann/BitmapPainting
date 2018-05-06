package com.timoniann.bitmappainting

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val image = findViewById<PaintImageView>(R.id.image)

        image.setOnClickListener { _ ->

        }

        val ims = assets.open("nature.jpg")
        val bitmap = BitmapFactory.decodeStream(ims)

        image.addBackground(bitmap)


    }
}
