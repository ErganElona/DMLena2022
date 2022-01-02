package com.lena.pasletp1.network

import com.lena.pasletp1.tasklist.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TasksRepository(val taskList: TaskListInterface) {

    private val tasksWebService = Api.tasksWebService

    suspend fun refresh() {
        // Call HTTP (opération longue):
        val tasksResponse = tasksWebService.getTasks()
        // À la ligne suivante, on a reçu la réponse de l'API:
        if (tasksResponse.isSuccessful) {
            val fetchedTasks = tasksResponse.body()
            // on modifie la valeur encapsulée, ce qui va notifier ses Observers et donc déclencher leur callback
            if (fetchedTasks != null) taskList.replaceTasks(fetchedTasks)
        }
    }

    suspend fun createOrUpdate(task: Task) {
        val oldTask = taskList.findTaskById(task.id)
        val response = when {
            oldTask != null -> tasksWebService.update(task, task.id)
            else -> tasksWebService.create(task)
        }
        if (response.isSuccessful) {
            val updatedTask = response.body()
            if (updatedTask != null) {
                if (oldTask != null) taskList.removeTask(oldTask)
                taskList.addTask(updatedTask)
            }
        }
    }

    suspend fun delete(task: Task) {
        val taskToDelete = taskList.findTaskById(task.id) ?: return

        val response = tasksWebService.delete(task.id)
        if (response.isSuccessful) {
            taskList.removeTask(taskToDelete)
        }
    }

    interface TaskListInterface {
        fun replaceTasks(tasks: List<Task>)
        fun findTaskById(id: String): Task?
        fun addTask(task: Task)
        fun removeTask(task: Task)
    }
}