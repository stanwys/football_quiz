package com.example.footballapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_choose_quiz.*
import kotlinx.android.synthetic.main.activity_choose_quiz.view.*
import kotlinx.android.synthetic.main.quiz_item.view.*

class ChooseQuizActivity : AppCompatActivity() {
    //private lateinit var mLocalDBHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_quiz)

        val categories = getCategoriesList()

        quizListView.adapter = ChooseQuizAdapter(
            this,//this!!.context!! ,
            R.layout.quiz_item,
            ArrayList(categories)
        )

    }

    private fun getCategoriesList():List<String>{
        var categoryList = mutableListOf<String>()
        val localDBHandler = MyDBHandler(this)
        localDBHandler.openDatabase()

        val cursor = localDBHandler.getCategoryList()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                categoryList.add(cursor.getString(1))
            }
        }
        cursor?.close()
        localDBHandler.close()
        return categoryList
    }

}
