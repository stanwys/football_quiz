package com.example.footballapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_end_quiz.*

class EndQuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_quiz)

        val category=intent.getStringExtra("category")
        val score=intent.getFloatExtra("score",0F)
        val display = getDisplayData(category,score)

        updateBestScores(category,score)
        prepareView(display,score)
        backToMenuButton.setOnClickListener {
            goBackToMainMenu()
        }
    }

    private fun goBackToMainMenu(){
        val inT = Intent(this,MainActivity::class.java)
        inT.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(inT)
        this.finish()
    }

    private fun updateBestScores(categoryName: String, score : Float){
        val dbHandler = MyDBHandler(this)
        dbHandler.openDatabase()
        val categoryId = dbHandler.getCategoryID(categoryName)
        if (categoryId != null){
            dbHandler.updateLocalBestScores(categoryId,score)
        }
        dbHandler.close()
    }

    private fun prepareView(display: Display, score : Float){
        if (display.image != null){
            imageView.setImageBitmap((BitmapFactory.decodeByteArray(display.image, 0, display.image.size)))
            imageView.visibility = View.VISIBLE
        }
        else{
            imageView.visibility = View.GONE
        }
        if (display.description != null && display.scoreClass != null){
            classTextView.text = display.scoreClass
            descriptionTextView.text = display.description
            classTextView.visibility = View.VISIBLE
            descriptionTextView.visibility = View.VISIBLE
        }
        else{
            classTextView.visibility = View.GONE
            descriptionTextView.visibility = View.GONE
        }
        scoreTextView.text = String.format("%.2f", score)
    }

    private fun getDisplayData(categoryName: String, score : Float):Display{
        val dbHandler = MyDBHandler(this)
        dbHandler.openDatabase()
        var texts : List<String?>? = listOf(null,null)
        var image : ByteArray? = null
        val categoryId = dbHandler.getCategoryID(categoryName)
        if (categoryId != null){
            texts = dbHandler.getDescriptionValues(categoryId,score)
            image = dbHandler.getScoreImage(categoryId,score)
        }
        dbHandler.close()
        return Display(texts?.get(0), texts?.get(1),image)
    }

}
