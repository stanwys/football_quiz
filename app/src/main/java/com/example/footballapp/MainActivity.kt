package com.example.footballapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
//import com.google.api.client.json.jackson2.JacksonFactory
import com.fasterxml.jackson.module.kotlin.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFbReference: FirebaseDatabase
    private lateinit var mStReference: FirebaseStorage
    private lateinit var mDbQuizReference: DatabaseReference//0
    private lateinit var mDbDescReference: DatabaseReference//1
    private lateinit var mDbDivsReference: DatabaseReference//2
    private lateinit var mDbTierReference: DatabaseReference//3
    private lateinit var mStImagesReference: StorageReference//4
    private lateinit var mFbParser: Parser
    private lateinit var mLocalDBHandler: MyDBHandler
    private val mLocks = mutableListOf<Boolean>(false,false,false,false,false)
    private val mUpdatedData = Data()
    private val ONE_MEGABYTE = 1024L * 1024L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main)

        mFbReference = FirebaseDatabase.getInstance()
        mStReference = FirebaseStorage.getInstance()
        mDbQuizReference = mFbReference.getReference("quizzes")
        mDbDescReference = mFbReference.getReference("descriptions")
        mDbDivsReference = mFbReference.getReference("divisions")
        mDbTierReference = mFbReference.getReference("tiers")
        mStImagesReference = mStReference.getReference("images")
        mFbParser = Parser()
        mLocalDBHandler = MyDBHandler(this)

        mDbQuizReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mLocks[0]=true
                mUpdatedData.setQuizzes(mFbParser.parseFirebaseDatabaseToQuizList(dataSnapshot))
                mLocks[0]=false
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        mDbDescReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mLocks[1]=true
                mUpdatedData.setDescriptions(mFbParser.parseFirebaseDatabaseToDescriptionList(dataSnapshot))
                mLocks[1]=false
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        mDbDivsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mLocks[2]=true
                mUpdatedData.setDivisions(mFbParser.parseFirebaseDatabaseToDivisionList(dataSnapshot))
                mLocks[2]=false
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        mDbTierReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mLocks[3]=true
                mUpdatedData.setTiers(mFbParser.parseFirebaseDatabaseToTierList(dataSnapshot))
                mLocks[3]=false
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        mStImagesReference.listAll().addOnSuccessListener { listResult ->
            val picList = mutableListOf<Picture>()
            mLocks[4]=true
            listResult.items.forEach { item ->
                    item.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    picList.add(Picture(item.name.substringBefore(".png"),it))
                }.addOnFailureListener {
                }
            }
            mUpdatedData.setPictures(picList)
            mLocks[4]=false
        }.addOnFailureListener {
        }

        updateButton.setOnClickListener {
            if ((mUpdatedData.isAllDataNotNull() && mLocks.all{!it}) || mLocks.any{it}) {
                updateButton.isClickable = false
                while(mLocks.any{it}){}
                Toast.makeText(this@MainActivity, "Baza danych jest aktualizowana",Toast.LENGTH_SHORT).show()
                val asyncTask = LocalDatabaseUpdater(mLocalDBHandler, mUpdatedData)
                asyncTask.execute()
            }
            else if(mUpdatedData.isAllDataNull()){
                Toast.makeText(this@MainActivity, "Włącz internet w celu pobrania aktualizacji",Toast.LENGTH_SHORT).show()
            }
        }

        resultsButton.setOnClickListener {
            val InT = Intent(this,ScoreActivity::class.java)
            startActivity(InT)
        }

        quitButton.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }

        playButton.setOnClickListener {

            val InT = Intent(this,ChooseQuizActivity::class.java)
            startActivity(InT)

                        /*
            // Attach a listener to read the data at our posts reference
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val post = dataSnapshot.getValue(Data::class.java)
                    System.out.println(post)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("The read failed: " + databaseError.code)
                }
            })*/

            /*
            val fileId = "0BwwA4oUTeiV1UVNwOHItT0xfa2M"
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)

            val testDirectory = File("$filesDir/pliki")
            val fos = FileOutputStream("$testDirectory"+"/"+"nic")
            outputStream.writeTo(fos)

            val request = Drive.Files.Get//.files().get(fileId)
            request.getMediaHttpDownloader().setProgressListener(CustomProgressListener())
            request.executeMediaAndDownloadTo(out)

*/
            /*
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )

            val driveService = Drive.Builder(
                Drive.HTTP_TRANSPORT.,
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(getString(R.string.app_name))
                .build()

            val fileId = "0BwwA4oUTeiV1UVNwOHItT0xfa2M"
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
*/
            /*
            //val url= URL("https://www.dropbox.com/s/nksb6vi2ahlqrqc/week.json?raw=1")
            //val url= URL("https://drive.google.com/uc?export=download&id=18SffVwTJmHHGrH7xjo28NUITXkV9hdLo")
            val url = URL("https://www.dropbox.com/s/aqr6f8p7eyhtism/football_quiz.json?raw=1")
            //val url=URL("https://www.dropbox.com/s/bb12o2ey8g0gkpo/football_questions.csv?raw=1")
            val connection= url.openConnection()
            connection.connect()
            val lengthOfFile = connection.contentLength
            Log.d("doInBack",lengthOfFile.toString())
            val isStream = url.openStream()
            val testDirectory = File("$filesDir/pliki")
            if(!testDirectory.exists())testDirectory.mkdirs()
            //check if the file exists, if yes then don't upload
            val file=File(testDirectory,"nic.json")
            if (!file.exists()) {
                Log.d("doInBack","Plik nic nie istnieje")
                val fos = FileOutputStream("$testDirectory"+"/"+"nic.json")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            }
            else{
                Log.d("doInBack","Plik nic już istnieje")
            }


            if(file.exists()){
                //read
                val mapper = jacksonObjectMapper()
                val data : Data = mapper.readValue<Data>(file)
                Log.d("nouts", "kicsk")

            }*/
        }
    }


    private inner class LocalDatabaseUpdater(private val db:MyDBHandler,private val data:Data): AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            db.openDatabase()
        }

        //@SuppressLint("NewApi")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            db.close()
            Toast.makeText(this@MainActivity, "Baza danych została zaktualizowana",Toast.LENGTH_LONG).show()
            updateButton.isClickable = true
        }

        override fun doInBackground(vararg params: String?):String {
            db.updateLocalDatabase(data)
            return  "OK"
        }
    }


    @SuppressLint("NewApi")
    private inner class JSONDownloader(private val url : String, private val fileName : String): AsyncTask<String, Int, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
        }

        @SuppressLint("NewApi")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //loadData(idProjektu)
            //  showData(idProjektu)
        }

        override fun doInBackground(vararg params: String?):String {

            val url = URL("https://www.dropbox.com/s/aqr6f8p7eyhtism/football_quiz.json?raw=1")
            //val url=URL("https://www.dropbox.com/s/bb12o2ey8g0gkpo/football_questions.csv?raw=1")
            val connection= url.openConnection()
            connection.connect()
            val lengthOfFile = connection.contentLength
            Log.d("doInBack",lengthOfFile.toString())
            val isStream = url.openStream()
            val testDirectory = File("$filesDir/pliki")
            if(!testDirectory.exists())testDirectory.mkdirs()
            //check if the file exists, if yes then don't upload
            val file=File(testDirectory,"nic.json")
            if (!file.exists()) {
                Log.d("doInBack","Plik nic nie istnieje")
                val fos = FileOutputStream("$testDirectory"+"/"+"nic.json")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            }
            else{
                Log.d("doInBack","Plik nic już istnieje")
            }


            if(file.exists()){
                //read
                val mapper = jacksonObjectMapper()
                val data : Data = mapper.readValue<Data>(file)
                Log.d("nouts", "kicsk")

            }
            return "nic"
        }

    }






    /*
    fun downloadFile(name: String, type: String) {
        try {
            val service = getDriveService()
            val destination = Config.getDestination()

            val parentFolder = getFolder(destination)
            val childFolder = getFolder(type, parentFolder)
            val backupToDownload = getFile(name, childFolder)

            val dirFile = java.io.File("downloads/$type")
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }
            val out = FileOutputStream("downloads/$type/$name")

            val request = service.files().get(backupToDownload.getId())
            request.executeMediaAndDownloadTo(out)
            //MessageUtil.sendConsoleMessage("Done downloading '$name' from google drive")
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun getDriveService(): Drive {
        val credential = Credential().
        return Drive.Builder(
            NetHttpTransport(), JacksonFactory(), credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()
    }*/
}
