package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import network.ApiClient
import settings.ExplainFolderDialog
import settings.PluginSettings
import javax.swing.JComboBox
import javax.swing.JTextArea
import javax.swing.JScrollPane
import javax.swing.SwingWorker // Импортируем SwingWorker для работы с фоновыми потоками
import javax.swing.JDialog // Импортируем JDialog для прогресс индикатора
import javax.swing.JProgressBar // Импортируем прогресс бар
import java.awt.BorderLayout // Импортируем для оформления диалога
import com.intellij.openapi.wm.WindowManager
import javax.swing.*
import utils.RunWithProgress
import javax.swing.JOptionPane

class FileDescriptionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val settings = PluginSettings.getInstance()
        val apiKey = settings.apiKey ?: run {
            Messages.showErrorDialog(project, "API Key is missing", "AI Plugin")
            return
        }
        val serverUrl = settings.serverUrl ?: run {
            Messages.showErrorDialog(project, "Server URL is missing", "AI Plugin")
            return
        }

        val content = String(file.contentsToByteArray())

        val options = arrayOf<String?>("English", "Русский")
        val choice: Int = JOptionPane.showOptionDialog(
            null,
            "Choose language for explanation",
            "Language",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        )
        val language = if (choice == 1) "ru" else "en"



        val result = RunWithProgress.runWithProgress(project, "The content is being generated...") {
            ApiClient.getCodeDescription(apiKey, content, serverUrl, language)
        }
            if (result != null) {
                val dialog = ExplainFolderDialog(project, result)
                dialog.show()
            } else {
                Messages.showErrorDialog(project, "Error generating file description", "AI Plugin")
            }



    }
}
