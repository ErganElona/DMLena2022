package com.lena.pasletp1.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lena.pasletp1.network.TasksRepository
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TaskListViewModel : ViewModel() {

    private val taskList = MutableStateFlow<List<Task>>(emptyList())

    private val repository = TasksRepository(object : TasksRepository.TaskListInterface {
        override fun replaceTasks(tasks: List<Task>) {
            taskList.value = tasks
        }
        override fun findTaskById(id: String): Task? {
            return taskList.value.firstOrNull { it.id == id }
        }
        override fun addTask(task: Task) {
            taskList.value += task
        }
        override fun removeTask(task: Task) {
            taskList.value -= task
        }
    })

    fun collect(collector: FlowCollector<List<Task>>) {
        viewModelScope.launch {
            taskList.collect(collector)
        }
    }

    fun createOrUpdate(task: Task) {
        viewModelScope.launch {
            repository.createOrUpdate(task)
            repository.refresh()
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            repository.refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

}