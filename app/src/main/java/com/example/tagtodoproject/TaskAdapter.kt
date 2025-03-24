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
    private val onCheckboxClick: (Task, Int) -> Unit // Callback untuk checkbox
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbTaskCompleted: CheckBox = itemView.findViewById(R.id.cbTaskCompleted)
        private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        private val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        private val tvTaskTags: TextView = itemView.findViewById(R.id.tvTaskTags)
        private val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(task: Task, position: Int) {
            tvTaskName.text = task.name
            tvTaskDate.text = "Tanggal: ${task.date}"
            tvTaskTags.text = "Tags: ${task.tags}"
            tvTaskCategory.text = "Kategori: ${task.category}"
            cbTaskCompleted.isChecked = task.isCompleted

            // Checkbox untuk menandai tugas selesai
            cbTaskCompleted.setOnClickListener {
                onCheckboxClick(task, position)
            }

            // Tombol Edit
            btnEdit.setOnClickListener {
                onEditClick(task, position)
            }

            // Tombol Hapus
            btnDelete.setOnClickListener {
                onDeleteClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position], position)
    }

    override fun getItemCount(): Int = tasks.size
}