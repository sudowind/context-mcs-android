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

    public override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    public override fun onBindViewHolder(holder:ViewHolder, position:Int) {
        holder.mItem = mValues.get(position)
        holder.mStatusView.setText(mValues.get(position).status)
        holder.mContentView.setText(mValues.get(position).title)
        holder.mDesView.setText(mValues.get(position).details)
        holder.mDeadlineView.setText(mValues.get(position).deadline)

        holder.mView.setOnClickListener(object:View.OnClickListener {
            public override fun onClick(v:View) {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener!!.onListFragmentInteraction(holder.mItem!!)
                }
            }
        })
    }

    public override fun getItemCount():Int {
        return mValues.size
    }

    inner class ViewHolder( val mView:View):RecyclerView.ViewHolder(mView) {
        val mStatusView:TextView
        val mContentView:TextView
        val mDesView:TextView
        val mDeadlineView:TextView
        var mItem:DummyItem? = null

        init{
            mStatusView = mView.findViewById(R.id.status) as TextView
            mContentView = mView.findViewById(R.id.content) as TextView
            mDesView = mView.findViewById(R.id.description) as TextView
            mDeadlineView = mView.findViewById(R.id.deadline) as TextView
        }

        public override fun toString():String {
            return super.toString() + " '" + mContentView.getText() + "'"
        }
    }
}
