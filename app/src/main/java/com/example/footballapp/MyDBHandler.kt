package com.example.footballapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.PointF.length
import android.util.Log

import android.os.AsyncTask.execute
import java.nio.file.Files.size
import android.database.sqlite.SQLiteStatement
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaRouter
import android.os.AsyncTask
import android.widget.Toast
import com.google.firebase.database.core.view.QuerySpec
import org.w3c.dom.Document
import java.nio.file.Files.getLastModifiedTime
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL
import java.sql.Statement

class MyDBHandler(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, DB_VERSION) {

    private var mDataBase: SQLiteDatabase? = null
    private var mNeedUpdate = false
    private val DB_PATH:String
    private val mTasksFields = arrayOf("A","B","C","D")

    companion object {
        private val DB_VERSION=BuildConfig.VERSION_CODE
        private val DB_NAME="FootballQuiz.db"
        private val DB_NUM_ANSWERS = 4
        private val INSERT_QUESTION = "INSERT INTO Tasks(CATEGORY_ID, QUESTION_ID ,QUESTION, A, B, C, D) VALUES (?,?,?,?,?,?,?)"
        private val INSERT_CATEGORY = "INSERT INTO Categories(Name) VALUES (?)"
        private val INSERT_DESCRIPTION = "INSERT INTO Descriptions(ID,DESCRIP,CLASS) VALUES (?,?,?)"
        private val INSERT_DIVISION = "INSERT INTO Divisions(CATEGORY_ID,RANK_ID,TIER_ID,DESC_ID,IMAGE_ID) VALUES(?,?,?,?,?)"
        private val INSERT_PICTURE = "INSERT INTO Images(ID,DATA) VALUES(?,?)"
        private val INSERT_TIER = "INSERT INTO Tiers(ID,LOWER,UPPER) VALUES (?,?,?)"
        private val INSERT_SCORE = "INSERT INTO BestScores(CATEGORY_ID,SCORE,CLASS) VALUES (?,?,?)"
    }

    init {
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = mContext.applicationInfo.dataDir + "/databases/"
        else
            DB_PATH = "/data/data/" + mContext.packageName + "/databases/"

        copyDatabase()
        this.writableDatabase
    }

    @Throws(IOException::class)
    fun updateDataBase() {
        println("MyDB "+"updateDataBase")
        if (mNeedUpdate) {
            val dbFile = File(DB_PATH + DB_NAME)
            if (dbFile.exists()){
                dbFile.delete()
                println("MyDB updateDataBase usuniety plik")
            }

            copyDatabase()

            mNeedUpdate = false
        }
    }

    private fun checkDatabase(): Boolean {
        val dbFile = File(DB_PATH + DB_NAME)
        if (dbFile.exists()==true)Log.d("lol","we are the champions!")
        return dbFile.exists()
    }

    private fun copyDatabase() {
        if (!checkDatabase()) {
            this.writableDatabase
            this.close()
            try {
                copyDBFile()
                println("copyDBFIle")
            } catch (mIOException: IOException) {
                throw Error("ErrorCopyingDataBase")
            }
        }
    }

    @Throws(IOException::class)
    private fun copyDBFile() {
        val mInput = mContext.assets.open(DB_NAME)
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        val mOutput = FileOutputStream(DB_PATH + DB_NAME)
        val mBuffer = ByteArray(1024)
        var mLength: Int
        mLength=1
        var size:Int=0
        while (mLength  > 0) {
            mLength = mInput.read(mBuffer)
            if(mLength>0){
                mOutput.write(mBuffer, 0, mLength)
                size+=mLength
            }
        }
        mOutput.flush()
        mOutput.close()
        mInput.close()
    }

    @Throws(SQLException::class)
    fun openDatabase(): Boolean {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return mDataBase != null
    }

    @Synchronized
    override fun close() {
        println("zamykamy baze")
        if (mDataBase != null)
            mDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("myDB","onCreate")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        println("onUpgrade"+newVersion.toString()+ " "+oldVersion.toString())
        if (newVersion > oldVersion) {
            mNeedUpdate = true
            updateDataBase()
            println("updateDataBase method executed")
        }
    }

    fun getCategoryList(): Cursor? {
        val query = "SELECT * FROM Categories"
        val data = mDataBase?.rawQuery(query, null)
        return data
    }

    fun getCategoryName(categoryId: String): String {
        val query = "SELECT Name FROM Categories WHERE ID = ?"
        val cursor= mDataBase?.rawQuery(query, arrayOf(categoryId))
        var value = ""
        if (cursor != null && cursor.moveToFirst() && cursor.count > 0){
            value = cursor.getString(0)
        }
        cursor?.close()
        return value
    }

    fun getTasksList(): Cursor? {
        val query = "SELECT * FROM Tasks"
        val data = mDataBase?.rawQuery(query, null)
        return data
    }

    fun getBestScores() : Cursor? {
        val query = "SELECT * FROM BestScores"
        val data = mDataBase?.rawQuery(query, null)
        return data
    }

    fun getCategoryTasksList(category_id : String): Cursor? {
        val query = "SELECT * FROM Tasks WHERE CATEGORY_ID = ?"
        val selectionArgs = arrayOf(category_id)
        val data = mDataBase?.rawQuery(query, selectionArgs)
        return data
    }

    fun getCategoryID(name : String):String?{
        val query = "SELECT ID FROM Categories WHERE NAME = ?"
        val selectionArgs= arrayOf(name.toUpperCase())
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        var value : String? = null
        if (cursor != null && cursor.moveToFirst() && cursor.count > 0){
            value = cursor.getString(0)
        }
        cursor?.close()
        return value
    }

    fun getDescription(id : String):Cursor?{
        val query = "SELECT * FROM Descriptions WHERE ID = ?"
        val selectionArgs = arrayOf(id)
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        return cursor
    }

    fun getDivision(categoryId: String,rankId : String):Cursor?{
        val query = "SELECT * FROM Divisions WHERE CATEGORY_ID = ? AND RANK_ID = ?"
        val selectionArgs = arrayOf(categoryId,rankId)
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        return cursor
    }

    fun getTask(categoryId: String, questionId: String):Cursor?{
        val query = "SELECT * FROM Tasks WHERE CATEGORY_ID = ? AND QUESTION_ID = ?"
        val selectionArgs= arrayOf(categoryId,questionId)
        val data = mDataBase?.rawQuery(query, selectionArgs)
        return data
    }

    fun getPicture(id : String):Cursor?{
        val query = "SELECT * FROM Images WHERE ID = ?"
        val selectionArgs= arrayOf(id)
        val data = mDataBase?.rawQuery(query, selectionArgs)
        return data
    }

    fun getTier(id : String):Cursor?{
        val query = "SELECT * FROM Tiers WHERE ID = ?"
        val selectionArgs= arrayOf(id)
        val data = mDataBase?.rawQuery(query, selectionArgs)
        return data
    }

    fun getDescriptionValues(categoryId: String, score : Float):List<String?>?{
        val query = "SELECT De.DESCRIP,De.CLASS " +
                "FROM Descriptions De, Divisions D,Tiers T WHERE D.CATEGORY_ID = ? AND " +
                "D.TIER_ID = T.ID AND T.LOWER <= ? AND ? < T.UPPER AND D.DESC_ID = De.ID"
        val selectionArgs= arrayOf(categoryId,score.toString(),score.toString())
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        var values : List<String?>? = null
        cursor?.moveToFirst()
        if (cursor?.count != 0) {
            try{
                values = listOf(cursor?.getString(0),cursor?.getString(1))
            }
            catch (anyExeception : Exception){
            }
        }
        cursor?.close()

        return values
    }

    fun getScoreImage(categoryId: String, score : Float) : ByteArray? {
        val query = "SELECT I.DATA " +
                "FROM Images I, Divisions D,Tiers T WHERE D.CATEGORY_ID = ? AND " +
                "D.TIER_ID = T.ID AND T.LOWER <= ? AND ? < T.UPPER AND D.IMAGE_ID = I.ID"
        val selectionArgs= arrayOf(categoryId,score.toString(),score.toString())
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        cursor?.moveToFirst()
        var image : ByteArray? = null
        if (cursor?.count != 0) {
            try{
                image = cursor!!.getBlob(0)
            }
            catch (anyExeception : Exception){
            }
        }
        cursor?.close()
        return image
    }

    private fun getBestScore(categoryId: String):Float?{
        val query = "SELECT SCORE FROM BestScores WHERE CATEGORY_ID = ?"
        val selectionArgs= arrayOf(categoryId)
        val cursor = mDataBase?.rawQuery(query, selectionArgs)
        cursor?.moveToFirst()
        var score : Float? = null
        if (cursor?.count !=0){
            try{
                score = cursor?.getFloat(0)
            }
            catch (anyExeception : Exception){
            }
        }
        return score
    }

    private fun updateQuestion(question: Question, categoryId: String){
        val numAnswers = question.answers.size
        try{
            val values = ContentValues()
            values.put("QUESTION",question.question.toUpperCase())
            for (index in 0 until DB_NUM_ANSWERS){
                if (index < numAnswers){
                    values.put(mTasksFields[index],question.answers[index])
                }
                else{
                    values.put(mTasksFields[index],"")
                }
            }
            mDataBase!!.update("Tasks", values, "CATEGORY_ID = ? AND QUESTION_ID = ? ",
                arrayOf(categoryId, question.id))
        }
        catch (anyException: Exception){
        }

    }

    private fun insertQuestion(question: Question, categoryId: String, stmt : SQLiteStatement){

        val numAnswers = question.answers.size
        try {
            stmt.bindString(1, categoryId)
            stmt.bindString(2, question.id)
            stmt.bindString(3, question.question.toUpperCase())
            for (index in 0 until numAnswers){
                stmt.bindString(4+index,question.answers[index])
            }
            for (index in numAnswers until 4){
                stmt.bindString(4+index,"")
            }
            stmt.execute()
            stmt.clearBindings()

        }
        catch (anyException: Exception){
        }
    }

    private fun insertCategory(name: String, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, name.toUpperCase())
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun insertDescription(desc : Description, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, desc.id)
            stmt.bindString(2, desc.desc)
            stmt.bindString(3, desc.descClass)
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun updateDescription(desc : Description){
        try {
            val values = ContentValues()
            values.put("DESCRIP",desc.desc)
            values.put("CLASS",desc.descClass)
            mDataBase!!.update("Descriptions", values, "ID = ? ",
                arrayOf(desc.id))
        }
        catch (anyException: Exception){
        }
    }

    private fun insertDivision(categoryId: String,rank : Rank, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, categoryId)
            stmt.bindString(2, rank.id)
            stmt.bindString(3, rank.tierId)
            stmt.bindString(4, rank.descId)
            stmt.bindString(5, rank.imageId)
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun updateDivision(categoryId: String, rank : Rank){
        try {
            val values = ContentValues()
            values.put("TIER_ID",rank.tierId)
            values.put("DESC_ID",rank.descId)
            values.put("IMAGE_ID",rank.imageId)
            mDataBase!!.update("Descriptions", values, "CATEGORY_ID = ? AND RANK_ID = ?",
                arrayOf(categoryId,rank.id))
        }
        catch (anyException: Exception){
        }
    }

    private fun insertPicture(picture : Picture, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, picture.id)
            stmt.bindBlob(2, picture.blob)
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun updatePicture(picture : Picture){
        try {
            val values = ContentValues()
            values.put("DATA",picture.blob)
            mDataBase!!.update("Images", values, "ID = ?",
                arrayOf(picture.id))
        }
        catch (anyException: Exception){
        }
    }

    private fun insertTier(tier : Tier, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, tier.id)
            stmt.bindString(2, tier.lower)
            stmt.bindString(3, tier.upper)
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun updateTier(tier : Tier){
        try {
            val values = ContentValues()
            values.put("LOWER",tier.lower)
            values.put("UPPER",tier.upper)
            mDataBase!!.update("Tiers", values, "ID = ?",
                arrayOf(tier.id))
        }
        catch (anyException: Exception){
        }
    }

    private fun insertScore(categoryId : String, score : Float, scoreClass : String, stmt : SQLiteStatement){
        try {
            stmt.bindString(1, categoryId)
            stmt.bindString(2, score.toString())
            stmt.bindString(3, scoreClass)
            stmt.execute()
            stmt.clearBindings()
        }
        catch (anyException: Exception){
        }
    }

    private fun updateScore(categoryId : String,score : Float, scoreClass : String){
        try {
            val values = ContentValues()
            values.put("SCORE",score)
            values.put("CLASS",scoreClass)
            mDataBase!!.update("BestScores", values, "CATEGORY_ID = ?",
                arrayOf(categoryId))
        }
        catch (anyException: Exception){
        }
    }

    private fun updateLocalCategories(quizzes : List<Quiz>, stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try{
            val newCategories = quizzes.map{it.category}
            newCategories.map{
                insertCategory(it,stmt)
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    private fun updateLocalQuestions(quizzes : List<Quiz>, stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try {
            quizzes.map {
                val categoryId = getCategoryID(it.category)
                if (categoryId != null){
                    it.quiz.map {
                        if (it.question.length > 0 && it.answers.size > 0) {
                            val cursor = getTask(categoryId,it.id)
                            cursor?.moveToFirst()
                            if (cursor?.count == 0) {
                                insertQuestion(it, categoryId, stmt)
                            } else {
                                updateQuestion(it, categoryId)
                            }
                            cursor?.close()
                        }
                    }
                }
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    private fun updateLocalDescriptions(descriptions : List<Description>, stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try{
            descriptions.map{
                val cursor = getDescription(it.id)
                cursor?.moveToFirst()
                if (cursor?.count == 0) {
                    insertDescription(it, stmt)
                } else {
                    updateDescription(it)
                }
                cursor?.close()
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    private fun updateLocalDivisions(divisions : List<Division>,stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try{
            divisions.map{
                val categoryId = getCategoryID(it.category)
                if (categoryId!=null){
                    it.ranks.map {
                        val cursor = getDivision(categoryId,it.id)
                        cursor?.moveToFirst()
                        if (cursor?.count == 0) {
                            insertDivision(categoryId,it, stmt)
                        } else {
                            updateDivision(categoryId,it)
                        }
                        cursor?.close()
                    }
                }
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    private fun updateLocalPictures(pictures : List<Picture>,stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try{
            pictures.map{
                val cursor = getPicture(it.id)
                cursor?.moveToFirst()
                if (cursor?.count == 0) {
                    insertPicture(it, stmt)
                } else {
                    updatePicture(it)
                }
                cursor?.close()
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    private fun updateLocalTiers(tiers : List<Tier>,stmt: SQLiteStatement){
        mDataBase?.beginTransaction()
        try{
            tiers.map {
                val cursor = getTier(it.id)
                cursor?.moveToFirst()
                if (cursor?.count == 0) {
                    insertTier(it, stmt)
                } else {
                    updateTier(it)
                }
                cursor?.close()
            }
            mDataBase?.setTransactionSuccessful()
        }
        catch (anyException: Exception){
        } finally {
            mDataBase?.endTransaction()
        }
    }

    fun updateLocalBestScores(categoryId: String, score : Float){
        val scoreStmt = mDataBase?.compileStatement(INSERT_SCORE)
        val bestScore = getBestScore(categoryId)
        val texts = getDescriptionValues(categoryId,score)
        val descClass = if(texts != null && texts.get(1)!=null) texts.get(1) else ""
        if (bestScore != null){
            if (score > bestScore){
                updateScore(categoryId,score,descClass!!)
            }
        }
        else{
            insertScore(categoryId,score,descClass!!,scoreStmt!!)
        }

    }

    fun updateLocalDatabase(data : Data){
        if (mDataBase!= null){

            val questionStmt = mDataBase?.compileStatement(INSERT_QUESTION)
            val categoryStmt = mDataBase?.compileStatement(INSERT_CATEGORY)
            val descriptionStmt = mDataBase?.compileStatement(INSERT_DESCRIPTION)
            val divisionStmt = mDataBase?.compileStatement(INSERT_DIVISION)
            val pictureStmt =  mDataBase?.compileStatement(INSERT_PICTURE)
            val tierStmt = mDataBase?.compileStatement(INSERT_TIER)

            if (data.getQuizzes() != null) {
                updateLocalCategories(data.getQuizzes()!!, categoryStmt!!)
                updateLocalQuestions(data.getQuizzes()!!,questionStmt!!)
            }

            if (data.getDescriptions() != null){
                updateLocalDescriptions(data.getDescriptions()!!,descriptionStmt!!)
            }

            if (data.getDivisions() != null){
                updateLocalDivisions(data.getDivisions()!!,divisionStmt!!)
            }

            if (data.getPictures() != null){
                updateLocalPictures(data.getPictures()!!,pictureStmt!!)
            }

            if (data.getTiers() != null){
                updateLocalTiers(data.getTiers()!!,tierStmt!!)
            }
        }
    }
}
