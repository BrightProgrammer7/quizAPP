package com.example.myquiizapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myquiizapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var quizModellist : MutableList<QuizModel>
    lateinit var adapter: QuizListAdapter
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quizModellist= mutableListOf()

        getDataFromFirebase()
        binding.btnMaps.setOnClickListener {
            // Créez une intention pour ouvrir l'activité de la carte
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        binding.btnLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            finishAffinity()
        }

 }
    private fun setupRecyclerView(){
        adapter=QuizListAdapter(quizModellist)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=adapter



    }
    private fun getDataFromFirebase(){
        //dummy data
//        val listQuestionModel = mutableListOf<QuestionModel>()
//        listQuestionModel.add(QuestionModel("what is android ?", mutableListOf("Language","OS","Programming","Computer","Geography","None"),"OS"))
//        listQuestionModel.add(QuestionModel("who owns android ?", mutableListOf("Language","OS","Google","Computer","Geography","None"),"Google"))
//        listQuestionModel.add(QuestionModel("which assistant android uses ?", mutableListOf("Language","OS","Cortana","Computer","Geography","None"),"Cortana"))
//
//        quizModellist.add(QuizModel("1","Programming","All the basic programing","10",listQuestionModel))
//        quizModellist.add(QuizModel("2", "Computer", "All the Computer question", "20",listQuestionModel))
//        quizModellist.add(QuizModel("3", "Geography", "Boost your geographic knowledge", "15",listQuestionModel))
//        quizModellist.add(QuizModel("4", "IOT", "All the basic programing", "10",listQuestionModel))
//        quizModellist.add(QuizModel("5", "JEE", "All the basic programing", "10",listQuestionModel))
       FirebaseDatabase.getInstance().reference
           .get()
           .addOnSuccessListener { dataSnapshot->

               if (dataSnapshot.exists()){
                   for (snapshot in dataSnapshot.children){
                       val quizModel =snapshot.getValue(QuizModel::class.java)//convert data to model
                       if (quizModel != null) {
                           quizModellist.add(quizModel)
                       }
                   }
                   setupRecyclerView()
               }
//               setupRecyclerView()
           }



    }

}