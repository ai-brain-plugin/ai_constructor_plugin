package org.aibrain.ai_constructor_plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import network.ApiClient
import settings.KeyDialog
import settings.PromptDialog
import settings.PluginSettings
import utils.RunWithProgress

class EnterPromptForAIAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val settings = PluginSettings.getInstance()

        // Если ключа нет — спрашиваем
        if (settings.apiKey.isNullOrBlank()) {
            val keyDialog = KeyDialog()
            if (keyDialog.showAndGet()) {
                settings.apiKey = keyDialog.getKey().trim()
            } else return
        }
        if (settings.serverUrl.isNullOrBlank()) {
            settings.serverUrl = Messages.showInputDialog(
                project,
                "Enter AI server base URL (e.g. https://example.com)",
                "Server URL",
                Messages.getQuestionIcon()
            )?.takeIf { it.isNotBlank() } ?: "http://127.0.0.1:5000"
        }
        val serverUrl = settings.serverUrl

        val promptDialog = PromptDialog()
        if (!promptDialog.showAndGet()) return
        val prompt = promptDialog.getPrompt()
        val image = promptDialog.getImageAsFile()
        val generated = RunWithProgress.runWithProgress(project, "Loading") {
            ApiClient.sendCutContent(settings.apiKey!!, "", prompt, serverUrl!!, project.name, image)
        }
            if (generated == null) {
               // Messages.showErrorDialog(project, "Server error or invalid response for prompt request", "AI Plugin")
                return

            }

            val caretModel = editor.caretModel
            val offset = caretModel.offset
            val document = editor.document

            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(offset, generated)
                caretModel.moveToOffset(offset + generated.length)
            }

            Messages.showInfoMessage(project, "Code generated and inserted", "AI Plugin")

    }
}
