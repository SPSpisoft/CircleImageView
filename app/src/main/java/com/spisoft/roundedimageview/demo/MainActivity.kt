package com.spisoft.roundedimageview.demo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.spisoft.roundedimageview.RoundedImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val v = findViewById<RoundedImageView>(R.id.vv)
        v.setOnClickListener(View.OnClickListener {
            v.setPoint(RoundedImageView.PointType.WARNING)
//            Toast.makeText(this, "sss", Toast.LENGTH_SHORT).show()
        })
    }
}