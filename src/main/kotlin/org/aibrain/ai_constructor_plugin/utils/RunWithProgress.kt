package utils

import javax.swing.SwingWorker // Импортируем SwingWorker для работы с фоновыми потоками
import javax.swing.JDialog // Импортируем JDialog для прогресс индикатора
import javax.swing.JProgressBar // Импортируем прогресс бар
import java.awt.BorderLayout // Импортируем для оформления диалога
import com.intellij.openapi.wm.WindowManager
import javax.swing.*
import javax.swing.border.EmptyBorder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project
import java.awt.Dimension
import java.io.PrintWriter
import java.io.StringWriter

object RunWithProgress {
    fun <T> runWithProgress(project: Project, title: String, task: () -> T?): T? {
        val frame = WindowManager.getInstance().getFrame(project)
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
        var thrown: Throwable? = null

        val worker = object : SwingWorker<T?, Void?>() {
            override fun doInBackground(): T? {
                return try {
                    task()
                } catch (ex: Throwable) {
                    // Сохраняем исключение для отображения в UI-потоке
                    thrown = ex
                    null
                }
            }

            override fun done() {
                try {
                    try {
                        result = get()
                    } catch (ex: Exception) {
                        // Если get() выбросил ExecutionException или другие исключения — извлекаем причину
                        if (thrown == null) {
                            thrown = ex.cause ?: ex
                        }
                    }

                    // Если было исключение — показываем диалог с ошибкой и подробностями (stack trace)
                    thrown?.let { t ->
                        val message = t.message ?: t.toString()
                        Messages.showErrorDialog(project, message, "AI Plugin")

                        // Формируем стек-трейс в текстовое поле и показываем в отдельном диалоге
                        val sw = StringWriter()
                        t.printStackTrace(PrintWriter(sw))
                        val stackTrace = sw.toString()

                        val textArea = JTextArea(stackTrace).apply {
                            isEditable = false
                            caretPosition = 0
                        }
                        val scrollPane = JScrollPane(textArea).apply {
                            preferredSize = Dimension(700, 350)
                        }

                        // Показываем детальный диалог с стек-трейсом, центрируем относительно основного фрейма
                        //JOptionPane.showMessageDialog(frame ?: progressDialog, scrollPane, "Error details", JOptionPane.ERROR_MESSAGE)
                    }
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