package com.example.footballapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import java.util.ArrayList
import kotlinx.android.synthetic.main.quiz_item.view.*

class ChooseQuizAdapter (private val mContext: Context, private val mResource: Int, objects: ArrayList<String>) :
    ArrayAdapter<String>(mContext, mResource, objects) {

    override fun getView(position: Int, convView: View?, parent: ViewGroup): View {

        var convertView = convView
        val category = getItem(position)

        if (convertView == null) {
            val inflater = LayoutInflater.from(mContext)
            convertView = inflater.inflate(mResource, parent, false)
        }
        convertView?.button!!.setText(category)

        convertView?.button?.setOnClickListener(View.OnClickListener {
            val category = convertView.button.text.toString()
            val inT = Intent(mContext, QuizActivity::class.java)
            inT.putExtra("category", category)
            mContext.startActivity(inT)
        })

        return convertView!!
    }
}