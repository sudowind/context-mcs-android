package com.example.wangfeng.csapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

class TaskUnaccepted : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.task_detail_unaccepted)
        setContentView(R.layout.task_detail_unaccepted)
        val bundle = this.intent.extras
        if (bundle != null) {
            val title = bundle.getString("title")
            val desc = bundle.getString("desc")
            val deadline = bundle.getString("deadline")
            Log.v("task detail", title)
            Log.v("task detail", desc)
            val titleView = findViewById<TextView>(R.id.taskTitle)
            val descView = findViewById<TextView>(R.id.description)
            val deadLineView = findViewById<TextView>(R.id.deadLine)
            titleView.text = title
            descView.text = desc
            deadLineView.text = deadline
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnAccept -> {
                Log.i("detail", "accept")
            }
            R.id.btnRefuse -> {
                Log.i("detail", "refuse")
            }
        }
    }
}
