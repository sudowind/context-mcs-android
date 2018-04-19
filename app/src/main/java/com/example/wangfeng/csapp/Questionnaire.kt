package com.example.wangfeng.csapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.RadioButton



class Questionnaire : AppCompatActivity() {

    private var radioGroup : RadioGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionair)
        radioGroup = findViewById(R.id.radioGroup)
        loadQuestionnaire()
    }

    private fun loadQuestionnaire() {
        for (i in 0..9) {
            val radioButton = RadioButton(this)
            radioButton.text = "this is radioButton  $i"
            //设置radioButton的点击事件
            radioButton.setOnClickListener {
                Toast.makeText(this@Questionnaire, "this is radioButton  $i", Toast.LENGTH_SHORT).show()
            }
            //将radioButton添加到radioGroup中
            radioGroup?.addView(radioButton)
        }
//        radioGroup?.setOnCheckedChangeListener { p0, p1 ->
//            val radioBtn = findViewById<RadioButton>(p1)
//            Toast.makeText(this@Questionnaire, radioBtn.text, Toast.LENGTH_SHORT).show()
//        }
    }
}
