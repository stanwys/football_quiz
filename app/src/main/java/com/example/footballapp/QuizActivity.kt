package com.example.footballapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_quiz.*
import java.security.SecureRandom
import kotlin.math.min
import kotlinx.android.synthetic.main.activity_quiz.view.*
import kotlin.math.ceil


class QuizActivity : AppCompatActivity() {

    private val mNumAnswersInDatabase = 4
    private lateinit var mQuestionList : ArrayList<Question>
    private var mQuestionIndex = 0
    private var mNumQuestions = 0
    private var mMaxScore = 0
    private var mScore = 0.0
    private lateinit var mHandler : Handler
    private lateinit var mRunExecute : Runnable
    private lateinit var mRunPrepare : Runnable
    private lateinit var mTimer : CountDownTimer
    private lateinit var mCategory : String
    private val mTimeForQuestionInMillis = 16000L

    companion object {
        val mSecureRandomGenerator = SecureRandom()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        mCategory=intent.getStringExtra("category")

        mQuestionList = getQuestionList(mCategory)
        mNumQuestions = min(20,mQuestionList.size)
        mMaxScore =  min(20,mQuestionList.size)
        mQuestionIndex = randomizeQuestionIndex()
        mHandler = Handler()

        mRunExecute = Runnable {
            executeProcedure()
        }

        mRunPrepare = Runnable {
            prepareButtons(mQuestionIndex)
            mTimer.start()
        }

        mQuestionIndex = randomizeQuestionIndex()
        numQuestionsTextView.text = "/ $mMaxScore"
        startQuestion(mQuestionIndex)

        mTimer = object : CountDownTimer(mTimeForQuestionInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                blockButtons()
                updateTimerText(0)
                makeButtonsGray()
                mHandler.postDelayed(mRunExecute,1000)
            }
        }
    }

    fun updateTimerText(timeUntilFinish : Long){
        timeTextView.text = (timeUntilFinish / 1000).toString()
    }


    fun onButtonAClick(v : View){
        mTimer.cancel()
        blockButtons()
        if ( isCorrectAnswer(v.aButton.text.toString(),mQuestionIndex)){
            v.aButton.setBackgroundResource(R.drawable.correct_button_background)
            mScore += 1
        }
        else{
            v.aButton.setBackgroundResource(R.drawable.wrong_button_background)
        }
        mHandler.postDelayed(mRunExecute,1000)
    }

    fun onButtonBClick(v : View){
        mTimer.cancel()
        blockButtons()
        if ( isCorrectAnswer(v.bButton.text.toString(),mQuestionIndex)){
            v.bButton.setBackgroundResource(R.drawable.correct_button_background)
            mScore += 1
        }
        else{
            v.bButton.setBackgroundResource(R.drawable.wrong_button_background)
        }
        mHandler.postDelayed(mRunExecute,1000)
    }

    fun onButtonCClick(v : View){
        mTimer.cancel()
        blockButtons()
        if ( isCorrectAnswer(v.cButton.text.toString(),mQuestionIndex)){
            v.cButton.setBackgroundResource(R.drawable.correct_button_background)
            mScore += 1
        }
        else{
            v.cButton.setBackgroundResource(R.drawable.wrong_button_background)
        }
        mHandler.postDelayed(mRunExecute,1000)
    }

    fun onButtonDClick(v : View){
        mTimer.cancel()
        blockButtons()
        if ( isCorrectAnswer(v.dButton.text.toString(),mQuestionIndex)){
            v.dButton.setBackgroundResource(R.drawable.correct_button_background)
            mScore += 1
        }
        else{
            v.dButton.setBackgroundResource(R.drawable.wrong_button_background)
        }
        mHandler.postDelayed(mRunExecute,1000)
    }

    private fun calcReadTime(length : Int):Long{
        return ceil(length/20.0).toLong()* 1000 //miliseconds
    }

    private fun startQuestion(index : Int ){
        hideButtons()
        doneQuestionsTextView.text = (mMaxScore-mNumQuestions+1).toString()
        updateTimerText(mTimeForQuestionInMillis)
        questionTextView.text = mQuestionList[index].question
        val timeForReading =calcReadTime(mQuestionList[index].question.length)
        mHandler.postDelayed(mRunPrepare,timeForReading)
    }

    private fun hideButtons(){
        aButton.visibility = View.GONE
        bButton.visibility = View.GONE
        cButton.visibility = View.GONE
        dButton.visibility = View.GONE
    }

    private fun blockButtons(){
        aButton.isClickable = false
        bButton.isClickable = false
        cButton.isClickable = false
        dButton.isClickable = false
    }

    private fun freeButtons(){
        aButton.isClickable = true
        bButton.isClickable = true
        cButton.isClickable = true
        dButton.isClickable = true
    }

    private fun makeButtonsGray(){
        val buttons = listOf<Button>(aButton,bButton,cButton,dButton)
        buttons.map{it.setBackgroundResource(R.drawable.gray_background)}
    }

    private fun executeProcedure(){
        mNumQuestions -= 1
        freeButtons()
        if (mNumQuestions > 0) {
            removeQuestionFromList(mQuestionIndex)
            mQuestionIndex = randomizeQuestionIndex()
            startQuestion(mQuestionIndex)
        }
        else{
            val inT = Intent(this,EndQuizActivity::class.java)
            inT.putExtra("category",mCategory)
            inT.putExtra("score",(100*mScore/mMaxScore).toFloat())
            inT.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(inT)
            this.finish()
        }
    }

    private fun removeQuestionFromList(index : Int){
        mQuestionList.removeAt(index)
    }

    private fun isCorrectAnswer(buttonText : String, index : Int): Boolean{
        return (buttonText == mQuestionList[index].getCorrectAnswer())
    }

    private fun randomizeQuestionIndex():Int{
        return mSecureRandomGenerator.nextInt(mQuestionList.size)
    }

    private fun prepareButtons(index : Int){
        val answerList = mQuestionList[index].answers.shuffled(mSecureRandomGenerator)
        val buttons = listOf<Button>(aButton,bButton,cButton,dButton)
        for (i in 0 until mNumAnswersInDatabase){
            if(i < answerList.size){
                buttons[i].text = answerList[i]
                buttons[i].setBackgroundResource(R.drawable.button_background)
                buttons[i].visibility = View.VISIBLE
            }
            else{
                buttons[i].visibility = View.GONE
            }
        }
    }

    private fun getQuestionList(category : String): ArrayList<Question>{
        val questionList = arrayListOf<Question>()
        val dbHandler = MyDBHandler(this)
        dbHandler.openDatabase()
        val categoryId = dbHandler.getCategoryID(category)
        if (categoryId != null) {
            val cursor = dbHandler.getCategoryTasksList(categoryId)
            while(cursor?.moveToNext()!!){
                val answerList = mutableListOf<String>()
                for (i in 0 until mNumAnswersInDatabase){
                    if(cursor.getString(4 + i).length > 0){
                        answerList.add(cursor.getString(4 + i))
                    }
                }
                questionList.add(Question(cursor.getString(2),cursor.getString(3),answerList))
            }
        }
        dbHandler.close()
        return questionList
    }

}
