package com.spisoft.roundedimageview.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spisoft.roundedimageview.RoundedImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val v = findViewById<RoundedImageView>(R.id.vv)
        val v0 = findViewById<RoundedImageView>(R.id.vn)
        v0.setPoint(RoundedImageView.PointType.ERROR)
        v.setSrc(R.drawable.profile_back)
        v.setOnClickListener(View.OnClickListener {
            v.setPoint(RoundedImageView.PointType.WARNING)
//            Toast.makeText(this, "sss", Toast.LENGTH_SHORT).show()
        })
    }
}