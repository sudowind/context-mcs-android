package com.example.wangfeng.csapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.wangfeng.csapp.QuestionFragment.OnListFragmentInteractionListener
import com.example.wangfeng.csapp.dummy.QuestionContent
import com.example.wangfeng.csapp.dummy.QuestionContent.QuestionItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class QuestionnaireViewAdapter(private val mValues:List<QuestionContent.QuestionItem>, private val mListener:OnListFragmentInteractionListener?):RecyclerView.Adapter<QuestionnaireViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.questionnaire_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position:Int) {
        holder.mItem = mValues[position]
        holder.mStatusView.text = mValues[position].status
        holder.mContentView.text = mValues[position].name
        holder.mDeadlineView.text = mValues[position].deadline

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(holder.mItem!!)
        }
    }

    override fun getItemCount():Int {
        return mValues.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mValues[position].type == "task") {
            return 0
        } else {
            return 1
        }
    }

    inner class ViewHolder( val mView : View):RecyclerView.ViewHolder(mView) {
        val mStatusView:TextView = mView.findViewById(R.id.status) as TextView
        val mContentView:TextView = mView.findViewById(R.id.content) as TextView
        val mDeadlineView:TextView = mView.findViewById(R.id.deadline) as TextView
        var mItem:QuestionItem? = null

        override fun toString():String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
