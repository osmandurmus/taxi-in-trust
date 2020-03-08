package com.xormoti.taxi_in_trust


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }
}
