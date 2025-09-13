package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import network.ApiClient
import settings.PluginSettings
import javax.swing.JComboBox

import settings.ConfirmChangesDialog
import utils.FileUtils
import utils.RunWithProgress

// Действие 1: Комментирование кода
class CommentCodeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)
            ?: return
        val settings = PluginSettings.getInstance()
        val apiKey = settings.apiKey ?: run {
            Messages.showErrorDialog(project, "API Key is missing", "AI Plugin")
            return
        }
        val serverUrl = settings.serverUrl ?: run {
            Messages.showErrorDialog(project, "Server URL is missing", "AI Plugin")
            return
        }

        val languages = arrayOf("English", "Русский")
        val selectedLang = Messages.showChooseDialog(
            project,
            "Select comment language",   // message
            "AI Plugin",                 // title
            Messages.getQuestionIcon(),
            languages,                   // options
            languages[0],                 // default option
        ) ?: return


        val content = String(file.contentsToByteArray())
        val prompt = "Add comment on $selectedLang language for this code"
        val result = RunWithProgress.runWithProgress(project, "The content is being generated...") {
            ApiClient.sendFileContent(apiKey, content, prompt, serverUrl, project.name, null)
        }

            if (result != null) {
                val dialog = ConfirmChangesDialog(project, file.name, result, false)
                if (dialog.showAndGet()) {
                    FileUtils.overwriteFile(project, file, result)
                    Messages.showInfoMessage(project, "File was commented by AI", "AI Plugin")
                } else {
                    Messages.showInfoMessage(project, "Comment skipped", "AI Plugin")
                }
            } else {
                Messages.showErrorDialog(project, "Error generating comments", "AI Plugin")
            }

    }
}
