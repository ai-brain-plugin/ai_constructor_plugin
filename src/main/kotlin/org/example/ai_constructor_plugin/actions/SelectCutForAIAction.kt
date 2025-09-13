package org.example.ai_constructor_plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import network.ApiClient
import settings.PluginSettings
import settings.KeyDialog
import settings.ConfirmChangesDialog
import settings.PromptDialog
import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.BorderLayout

class SelectCutForAIAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selection = editor?.selectionModel?.selectedText
        e.presentation.isEnabledAndVisible = !selection.isNullOrBlank()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val content = editor.selectionModel.selectedText ?: return
        val settings = PluginSettings.getInstance()

        if (settings.apiKey.isNullOrBlank()) {
            val keyDialog = KeyDialog()
            if (keyDialog.showAndGet()) {
                settings.apiKey = keyDialog.getKey().trim()
            } else return
        }

        // ✅ Берем serverUrl из настроек

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

        val frame = com.intellij.openapi.wm.WindowManager.getInstance().getFrame(project)
        val progressDialog = JDialog(frame, "The content is being generated...", true)
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true

        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(20, 20, 20, 20) // отступы
        panel.add(progressBar, BorderLayout.CENTER)

        progressDialog.layout = BorderLayout()
        progressDialog.add(panel, BorderLayout.CENTER)
        progressDialog.pack()
        progressDialog.setLocationRelativeTo(frame)

        // SwingWorker для фоновой работы
        object : SwingWorker<String?, Void?>() {
            override fun doInBackground(): String? {
                return try {
                    ApiClient.sendCutContent(settings.apiKey!!, content, prompt, serverUrl ?: "http://127.0.0.1:5000", project.name, image)
                } catch (ex: Exception) {
                    null
                }
            }

            override fun done() {
                progressDialog.dispose() // закрыть прогресс-бар
                try {
                    val updated = get()
                    if (updated == null) {
                        Messages.showErrorDialog(project, "Server error or invalid response for file request", "AI Plugin")
                        return
                    }

                    if (content == updated) {
                        Messages.showInfoMessage(project, "Selected text is already up to date", "AI Plugin")
                        return
                    }

                    val dialog = ConfirmChangesDialog(project, "Selected code fragment", updated, false)
                    if (dialog.showAndGet()) {
                        val document = editor.document
                        val selectionModel = editor.selectionModel
                        val start = selectionModel.selectionStart
                        val end = selectionModel.selectionEnd

                        WriteCommandAction.runWriteCommandAction(project) {
                            document.replaceString(start, end, updated)
                            selectionModel.removeSelection()
                        }

                        Messages.showInfoMessage(project, "Selected text updated by AI server", "AI Plugin")
                    } else {
                        Messages.showInfoMessage(project, "Update skipped", "AI Plugin")
                    }
                } catch (ex: Exception) {
                    Messages.showErrorDialog(project, ex.message ?: "Unknown API error", "AI Plugin")
                }
            }
        }.execute()

        progressDialog.isVisible = true // показать диалог
    }

}
