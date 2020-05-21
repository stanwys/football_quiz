package com.example.footballapp

import android.media.Image

class Data(){
    private var quizzes : List<Quiz>? = null
    private var divisions : List<Division>? = null
    private var descriptions : List<Description>? = null
    private var tiers : List<Tier>? = null
    private var pictures : List<Picture>? = null

    fun isAllDataNull(): Boolean = (
            (this.quizzes == null) && (this.divisions == null) &&
                    (this.descriptions == null) && (this.tiers == null) && (this.pictures == null)
            )

    fun isAllDataNotNull(): Boolean = (
            (this.quizzes != null) && (this.divisions != null) &&
                    (this.descriptions != null) && (this.tiers != null) && (this.pictures != null)
            )

    fun setQuizzes(quizList: List<Quiz>){
        this.quizzes = quizList
    }

    fun getQuizzes():List<Quiz>? = this.quizzes

    fun setDivisions(divisionList : List<Division>){
        this.divisions = divisionList
    }

    fun getDivisions():List<Division>? = this.divisions

    fun setDescriptions(descriptionList : List<Description>){
        this.descriptions = descriptionList
    }

    fun getDescriptions():List<Description>? = this.descriptions

    fun setTiers(tierList : List<Tier>){
        this.tiers = tierList
    }

    fun getTiers():List<Tier>? = this.tiers

    fun setPictures(imageList : List<Picture>){
        this.pictures = imageList
    }

    fun getPictures():List<Picture>? = this.pictures

}

data class Quiz(val category: String,val quiz : List<Question>)

data class Question(val id : String, val question : String, val answers : List<String>){

    fun getCorrectAnswer(): String {
        return if (answers.size > 0) answers[0] else ""
    }

}

data class Picture(val id : String, val blob : ByteArray)

data class Rank(val id: String,val descId : String, val imageId : String, val tierId : String)

data class Description(val id : String, val descClass : String,val desc : String)

data class Division(val category: String, val ranks : List<Rank>)

data class Tier(val id:String, val lower : String, val upper : String)

data class Display(val description : String?, val scoreClass :String?, val image : ByteArray?)

data class Score(val category: String, val score:Float, val scoreClass: String)