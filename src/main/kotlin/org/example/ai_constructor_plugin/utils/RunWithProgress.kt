package utils

import javax.swing.SwingWorker // Импортируем SwingWorker для работы с фоновыми потоками
import javax.swing.JDialog // Импортируем JDialog для прогресс индикатора
import javax.swing.JProgressBar // Импортируем прогресс бар
import java.awt.BorderLayout // Импортируем для оформления диалога
import com.intellij.openapi.wm.WindowManager
import javax.swing.*
import javax.swing.border.EmptyBorder
import com.intellij.openapi.ui.Messages

object RunWithProgress {
    fun <T> runWithProgress(project: com.intellij.openapi.project.Project, title: String, task: () -> T?): T? {
        val frame = com.intellij.openapi.wm.WindowManager.getInstance().getFrame(project)
        val progressDialog = JDialog(frame, title, true)
        val progressBar = JProgressBar().apply { isIndeterminate = true }

        val panel = JPanel(BorderLayout()).apply {
            border = EmptyBorder(20, 20, 20, 20)
            add(progressBar, BorderLayout.CENTER)
        }

        progressDialog.layout = BorderLayout()
        progressDialog.add(panel, BorderLayout.CENTER)
        progressDialog.pack()
        progressDialog.setLocationRelativeTo(frame)

        var result: T? = null
        val worker = object : SwingWorker<T?, Void?>() {
            override fun doInBackground(): T? {
                return try {
                    task()
                } catch (ex: Exception) {
                    null
                }
            }

            override fun done() {
                try {
                    result = get()
                } catch (ex: Exception) {
                    Messages.showErrorDialog(project, ex.message ?: "Unknown error", "AI Plugin")
                } finally {
                    progressDialog.dispose()
                }
            }
        }

        worker.execute()
        progressDialog.isVisible = true
        return result
    }

}
