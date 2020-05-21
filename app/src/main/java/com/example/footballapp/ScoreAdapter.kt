package com.example.footballapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.score_item.view.*
import java.util.ArrayList

class ScoreAdapter (private val mContext: Context, private val mResource: Int, objects: ArrayList<Score>) :
    ArrayAdapter<Score>(mContext, mResource, objects) {

    private class ViewHolder {
        internal var category: TextView? = null
        internal var score: TextView? = null
        internal var scoreClass: TextView? = null
    }

    override fun getView(position: Int, convView: View?, parent: ViewGroup): View {

        var convertView = convView
        val score = getItem(position)

        val holder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            convertView = inflater.inflate(mResource, parent, false)
            holder = ViewHolder()
            holder.category = convertView.categoryTextView
            holder.score = convertView.scoreTextView
            holder.scoreClass = convertView.classTextView
            convertView.tag = holder

        }else{
            holder = convertView.tag as ViewHolder
        }

        holder.category?.text = score.category
        holder.score?.text = String.format("%.2f",score.score)
        holder.scoreClass?.text = score.scoreClass

        return convertView!!
    }
}