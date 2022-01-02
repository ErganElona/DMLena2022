package com.lena.pasletp1.form

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lena.pasletp1.R
import com.lena.pasletp1.databinding.ActivityFormBinding
import com.lena.pasletp1.tasklist.Task
import java.util.*

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val task = intent.getSerializableExtra("task") as? Task

        if (task != null) {
            binding.formTitle.text.append(task.title)
            binding.formDesc.text.append(task.description)
        }

        binding.formAdd.setOnClickListener {
            val id = task?.id ?: UUID.randomUUID().toString()
            val title = binding.formTitle.text.toString()
            val desc = binding.formDesc.text.toString()
            val newTask = Task(id,title,desc)
            intent.putExtra("task", newTask)
            setResult(RESULT_OK, intent)
            finish()
        }

        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type)
        {
            binding.formDesc.text.append(intent.getStringExtra(Intent.EXTRA_TEXT)?: "")
        }

    }
}