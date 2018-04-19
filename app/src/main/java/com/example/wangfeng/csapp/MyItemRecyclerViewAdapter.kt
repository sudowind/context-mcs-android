package com.example.wangfeng.csapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.wangfeng.csapp.ItemFragment.OnListFragmentInteractionListener
import com.example.wangfeng.csapp.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(private val mValues:List<DummyItem>, private val mListener:OnListFragmentInteractionListener?):RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position:Int) {
        holder.mItem = mValues[position]
        holder.mStatusView.text = mValues[position].status
        holder.mContentView.text = mValues[position].title
        holder.mDesView.text = mValues[position].details
        holder.mDeadlineView.text = mValues[position].deadline

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(holder.mItem!!)
        }
    }

    override fun getItemCount():Int {
        return mValues.size
    }

    inner class ViewHolder( val mView : View):RecyclerView.ViewHolder(mView) {
        val mStatusView:TextView = mView.findViewById(R.id.status) as TextView
        val mContentView:TextView = mView.findViewById(R.id.content) as TextView
        val mDesView:TextView = mView.findViewById(R.id.description) as TextView
        val mDeadlineView:TextView = mView.findViewById(R.id.deadline) as TextView
        var mItem:DummyItem? = null

        override fun toString():String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
