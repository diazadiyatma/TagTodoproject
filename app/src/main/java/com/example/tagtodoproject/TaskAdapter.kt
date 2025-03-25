package com.example.tagtodoproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onEditClick: (Task, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onCheckboxClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbTaskCompleted: CheckBox = itemView.findViewById(R.id.cbTaskCompleted)
        private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        private val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        private val tvTaskTags: TextView = itemView.findViewById(R.id.tvTaskTags)
        private val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        private var currentTask: Task? = null

        fun bind(task: Task, position: Int) {
            currentTask = task
            tvTaskName.text = task.name
            tvTaskDate.text = "Date: ${task.date}"
            tvTaskTags.text = "Tags: ${task.tags}"
            tvTaskCategory.text = "Category: ${task.category}"

            // Remove previous listener to avoid duplicates
            cbTaskCompleted.setOnCheckedChangeListener(null)
            cbTaskCompleted.isChecked = task.isCompleted

            // Set new listener
            cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != task.isCompleted) {
                    onCheckboxClick(task, position)
                }
            }

            btnEdit.setOnClickListener { onEditClick(task, position) }
            btnDelete.setOnClickListener { onDeleteClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount(): Int = tasks.size
}
