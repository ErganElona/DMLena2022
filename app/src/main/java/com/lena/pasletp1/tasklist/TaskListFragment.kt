package com.lena.pasletp1.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lena.pasletp1.R
import com.lena.pasletp1.databinding.FragmentTaskListBinding
import com.lena.pasletp1.form.FormActivity
import com.lena.pasletp1.network.Api
import com.lena.pasletp1.network.TasksRepository
import kotlinx.coroutines.launch
import java.util.*

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!


    private val tasksRepository = TasksRepository()

    val formLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = result.data?.getSerializableExtra("task") as? Task
        task?.let {
            lifecycleScope.launch {
                tasksRepository.createOrUpdate(it)
                tasksRepository.refresh()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val adapterListener = object : TaskListListener {
            override fun onClickDelete(task: Task) {
                lifecycleScope.launch {
                    tasksRepository.delete(task)
                }
            }

            override fun onClickEditTask(task: Task) {
                val intent = Intent(context, FormActivity::class.java)
                intent.putExtra("task",task)
                formLauncher.launch(intent)
            }

        }
        val adapter = TaskListAdapter(adapterListener)

        binding.recyclerView.adapter = adapter

        binding.fab.setOnClickListener {
            val intent = Intent(context, FormActivity::class.java)
            formLauncher.launch(intent)
        }
        lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
            tasksRepository.taskList.collect { newList ->
                adapter.submitList(newList)
            }
        }
        lifecycleScope.launch {
            tasksRepository.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val userInfo = Api.userWebService.getInfo().body()!!
            binding.userInfo.text = "${userInfo.firstName} ${userInfo.lastName}"
        }
        lifecycleScope.launch {
            tasksRepository.refresh()
        }
    }
}