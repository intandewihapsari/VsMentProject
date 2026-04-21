package com.indri.vsmentproject.ui.manager.template

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig

class TemplateViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference

    // =========================
    // CREATE TEMPLATE
    // =========================
    fun createTemplate(template: TaskTemplateModel, onDone: (Boolean) -> Unit) {

        val ref = db.child(FirebaseConfig.PATH_TASK_TEMPLATES).push()

        val data = template.copy(id = ref.key ?: "")

        ref.setValue(data).addOnCompleteListener {
            onDone(it.isSuccessful)
        }
    }

    // =========================
    // UPDATE TEMPLATE
    // =========================
    fun updateTemplate(template: TaskTemplateModel, onDone: (Boolean) -> Unit) {

        db.child(FirebaseConfig.PATH_TASK_TEMPLATES)
            .child(template.id)
            .setValue(template)
            .addOnCompleteListener {
                onDone(it.isSuccessful)
            }
    }

    // =========================
    // DELETE TEMPLATE
    // =========================
    fun deleteTemplate(templateId: String, onDone: (Boolean) -> Unit) {

        db.child(FirebaseConfig.PATH_TASK_TEMPLATES)
            .child(templateId)
            .removeValue()
            .addOnCompleteListener {
                onDone(it.isSuccessful)
            }
    }

    // =========================
    // GENERATE TASK DARI TEMPLATE
    // =========================
    fun generateFromTemplate(
        template: TaskTemplateModel,
        villaId: String,
        villaName: String,
        staffId: String,
        staffName: String,
        deadline: String,
        onDone: (Boolean) -> Unit
    ) {

        val ref = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(villaId)
            .child("list_tugas")

        val tasks = template.tasks.map { taskName ->

            val taskRef = ref.push()

            TugasModel(
                id = taskRef.key ?: "",
                villa_id = villaId,
                villa_nama = villaName,
                tugas = taskName,
                staff_id = staffId,
                staff_name = staffName,
                deadline = deadline,
                status = "pending",
                kategori = template.kategori
            )
        }

        var successCount = 0

        tasks.forEach { task ->
            ref.child(task.id).setValue(task)
                .addOnCompleteListener {
                    if (it.isSuccessful) successCount++

                    if (successCount == tasks.size) {
                        onDone(true)
                    }
                }
        }
    }
}