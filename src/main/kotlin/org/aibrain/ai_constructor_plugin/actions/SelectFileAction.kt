package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.ui.Messages
import settings.PluginSettings
import settings.KeyDialog
import settings.MultiPromptDialog
import settings.ConfirmChangesDialog
import utils.FileUtils
import utils.RunWithProgress

import settings.PromptDialog

import network.ApiClient

class SelectFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)
            ?: return

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

        if (file.isDirectory) {
            val multiPromptDialog = MultiPromptDialog()
            if (!multiPromptDialog.showAndGet()) return
            val promptMap = multiPromptDialog.getPrompts() // Map<String, String>

            var createdCount = 0
            var globalDecision: ConfirmChangesDialog.ResultType? = null

            for ((name, prompt) in promptMap.entries) {
                if (globalDecision == ConfirmChangesDialog.ResultType.SKIP_ALL) continue
                if (globalDecision == ConfirmChangesDialog.ResultType.APPLY_ALL) {
                    val content = RunWithProgress.runWithProgress(project, "Loading") {
                        ApiClient.sendFileContent(settings.apiKey!!, "", prompt, serverUrl!!, project.name, null)
                    }
                        if (content != null) {
                            FileUtils.createFile(project, file, name, content)
                            createdCount++
                        }
                        continue

                }

                val content = RunWithProgress.runWithProgress(project, "Loading") {
                    ApiClient.sendFileContent(settings.apiKey!!, "", prompt, serverUrl!!, project.name, null)
                }
                    if (content == null) continue

                    val dialog = ConfirmChangesDialog(project, name, content, false)
                    if (!dialog.showAndGet()) continue

                    when (dialog.resultType) {
                        ConfirmChangesDialog.ResultType.APPLY_ONE -> {
                            FileUtils.createFile(project, file, name, content)
                            createdCount++
                        }

                        ConfirmChangesDialog.ResultType.APPLY_ALL -> {
                            globalDecision = ConfirmChangesDialog.ResultType.APPLY_ALL
                            FileUtils.createFile(project, file, name, content)
                            createdCount++
                        }

                        ConfirmChangesDialog.ResultType.SKIP_ONE -> {}
                        ConfirmChangesDialog.ResultType.SKIP_ALL -> {
                            globalDecision = ConfirmChangesDialog.ResultType.SKIP_ALL
                            break
                        }

                        null -> {}
                    }

            }

            Messages.showInfoMessage(
                project,
                "Created $createdCount file(s) in folder",
                "AI Plugin"
            )

        } else {
            val content = String(file.contentsToByteArray())
            val promptDialog = PromptDialog()
            if (!promptDialog.showAndGet()) return
            val prompt = promptDialog.getPrompt()
            val image = promptDialog.getImageAsFile()

            val updated = RunWithProgress.runWithProgress(project, "Loading") {
                ApiClient.sendFileContent(settings.apiKey!!, content, prompt, serverUrl!!, project.name, image)
            }
                if (updated == null) {
                    //Messages.showErrorDialog(project, "Server error or invalid response for file request", "AI Plugin")
                    return
                }

                if (content == updated) {
                    Messages.showInfoMessage(project, "File is already up to date", "AI Plugin")
                    return
                }

                val dialog = ConfirmChangesDialog(project, file.name, updated, false)
                if (dialog.showAndGet()) {
                    FileUtils.overwriteFile(project, file, updated)
                    Messages.showInfoMessage(project, "File updated by AI server", "AI Plugin")
                } else {
                    Messages.showInfoMessage(project, "Update skipped", "AI Plugin")
                }

        }
    }
}
