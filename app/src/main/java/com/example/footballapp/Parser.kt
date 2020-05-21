package com.example.footballapp

import com.google.firebase.database.DataSnapshot
import java.util.ArrayList

class Parser {

    fun parseFirebaseDatabaseToTierList(dataSnapshot: DataSnapshot):List<Tier>{
        val tierList = mutableListOf<Tier>()
        val numTiers = dataSnapshot.childrenCount.toInt()
        for( i in 0 until numTiers){
            tierList.add(parseTier(dataSnapshot.child("$i")))
        }
        return tierList
    }

    private fun parseTier(dataSnapshot: DataSnapshot):Tier{
        return Tier(
            dataSnapshot.child("id").value.toString(),
            dataSnapshot.child("lower").value.toString(),
            dataSnapshot.child("upper").value.toString()
        )
    }

    fun parseFirebaseDatabaseToDescriptionList(dataSnapshot: DataSnapshot):List<Description>{
        val descList = mutableListOf<Description>()
        val numDescriptions = dataSnapshot.childrenCount.toInt()
        for (i in 0 until numDescriptions){
            descList.add(parseDescription(dataSnapshot.child("$i")))
        }
        return descList
    }

    private fun parseDescription(dataSnapshot: DataSnapshot): Description {
        return Description(
            dataSnapshot.child("id").value.toString(),
            dataSnapshot.child("class").value.toString(),
            dataSnapshot.child("description").value.toString()
        )
    }

    fun parseFirebaseDatabaseToDivisionList(dataSnapshot: DataSnapshot):List<Division>{
        val divisionList = mutableListOf<Division>()
        val numDivisions = dataSnapshot.childrenCount.toInt()
        for (i in 0 until numDivisions){
            divisionList.add(parseDivision(dataSnapshot.child("$i")))
        }
        return divisionList
    }

    private fun parseDivision(dataSnapshot: DataSnapshot) : Division {
        val category = dataSnapshot.child("category").value.toString()
        val rankList = mutableListOf<Rank>()
        val numRanks = dataSnapshot.child("ranks").childrenCount.toInt()
        for (i in 0 until numRanks){
            rankList.add(parseRank(i,dataSnapshot.child("ranks/$i")))
        }
        return Division(category,rankList)
    }

    private fun parseRank(id : Int, dataSnapshot: DataSnapshot):Rank{
        return Rank(
            (id+1).toString(),
            dataSnapshot.child("desc_id").value.toString(),
            dataSnapshot.child("image_id").value.toString(),
            dataSnapshot.child("tier_id").value.toString()
        )
    }

    fun parseFirebaseDatabaseToQuizList(dataSnapshot: DataSnapshot): List<Quiz>{
        val numQuizes = dataSnapshot.childrenCount.toInt()
        val quizList = ArrayList<Quiz>(numQuizes)
        for (i in 0 until numQuizes){
            quizList.add(parseQuiz(dataSnapshot.child("$i")))
        }
        return quizList
    }

    private fun parseQuiz(dataSnapshot: DataSnapshot):Quiz{
        val category = dataSnapshot.child("category").value.toString()
        val numQuestions = dataSnapshot.child("quiz").childrenCount.toInt()
        val questionList = ArrayList<Question>(numQuestions)
        for (i in 0 until numQuestions){
            questionList.add(parseQuestion((i+1).toString(), dataSnapshot.child("quiz/$i")))
        }
        return Quiz(category,questionList)
    }

    private fun parseQuestion(id: String,dataSnapshot: DataSnapshot):Question{
        val question = dataSnapshot.child("question").value.toString()
        val numAnswers = dataSnapshot.child("answers").childrenCount.toInt()
        val answerList = ArrayList<String>(numAnswers)
        for (i in 0 until numAnswers){
            answerList.add(dataSnapshot.child("answers/$i").value.toString())
        }
        return Question(id, question,answerList)
    }

}