package com.example.myquiizapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myquiizapp.databinding.QuizItemRecyclerRowBinding

class QuizListAdapter(private val quizModelList: List<QuizModel> ):
RecyclerView.Adapter<QuizListAdapter.MyViewHolder>(){
    class MyViewHolder(private val binding: QuizItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: QuizModel){
            //bind all the views
            binding.apply {
                quizTitleText.text=model.title
                quizTimeText.text =model.time + " min"
                quizSubtitleText.text =model.subtitle
                root.setOnClickListener{
                    val intent =Intent(root.context,QuizActivity::class.java)
                    QuizActivity.questionModelList=model.questionList
                    QuizActivity.time = model.time
                    root.context.startActivity(intent)
                }
            }

        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val binding =QuizItemRecyclerRowBinding.inflate(LayoutInflater.from(p0.context),p0,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return quizModelList.size
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        p0.bind(quizModelList[p1])
    }
}