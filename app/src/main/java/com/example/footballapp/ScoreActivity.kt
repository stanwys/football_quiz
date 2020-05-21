package com.example.footballapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_score.*
import kotlinx.android.synthetic.main.activity_score.view.*

class ScoreActivity : AppCompatActivity() {

    private var listView: ListView? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        listView = findViewById(R.id.scoreListView)
        val scores = getScoreList()
        val adapter = ScoreAdapter(this,R.layout.score_item,ArrayList(scores))
        listView?.setAdapter(adapter)
    }

    private fun getScoreList(): List<Score>{
        var scores = mutableListOf<Score>()
        val dbHandler = MyDBHandler(this)
        dbHandler.openDatabase()
        val cursor =  dbHandler.getBestScores()
        if (cursor != null){
            while(cursor.moveToNext()){
                val category=dbHandler.getCategoryName(cursor.getString(0))
                scores.add(Score(category,cursor.getFloat(1),cursor.getString(2)))
            }
        }
        cursor?.close()
        dbHandler.close()
        return scores
    }
}
