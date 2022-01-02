package com.lena.pasletp1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lena.pasletp1.databinding.ActivityMainBinding
import com.lena.pasletp1.form.FormActivity
import com.lena.pasletp1.tasklist.TaskListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            val fragment = binding.fragmentTasklist.getFragment<TaskListFragment>()
            val fwdIntent = Intent(fragment.context, FormActivity::class.java).apply {
                action = Intent.ACTION_SEND
                putExtras(intent)
                type = "text/plain"
            }
            fragment.formLauncher.launch(fwdIntent)
        }
    }
}