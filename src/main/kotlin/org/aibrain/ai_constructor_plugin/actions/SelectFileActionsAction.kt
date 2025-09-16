package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import settings.PluginSettings
import settings.KeyDialog
import settings.MultiPromptDialog
import settings.ConfirmChangesDialog
import utils.FileUtils
import org.aibrain.ai_constructor_plugin.actions.ExplainFolderAction
import actions.CommentCodeAction
import actions.FileDescriptionAction
import settings.PromptDialog
import javax.swing.JButton // Импортируем JButton
import javax.swing.JFrame // Импортируем JFrame
import java.awt.GridLayout // Импортируем GridLayout
import settings.SelectActionDialog
import network.ApiClient
import utils.RunWithProgress
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
class SelectFileActionsAction : AnAction() {
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
            // Диалог для директории
            val options = arrayOf("Describe directory", "Generate files")
            val selectedOption = SelectActionDialog(options).showAndGetSelection()

            when (selectedOption) {
                "Describe directory" -> {
                    ExplainFolderAction().actionPerformed(e)
                }
                "Generate files" -> {
                    val promptDialog = PromptDialog()
                    if (!promptDialog.showAndGet()) return
                    val prompt = promptDialog.getPrompt()
                    val image = promptDialog.getImageAsFile()


                    fun getFilesInDirectory(): List<String> {
                        val virtualFile = file
                       return virtualFile.children.map { it.path }

                    }

                    var createdCount = 0
                    var globalDecision: ConfirmChangesDialog.ResultType? = null

                    val content = RunWithProgress.runWithProgress(project, "Loading") {
                        ApiClient.sendFolderPrompt(settings.apiKey!!, prompt, getFilesInDirectory(), serverUrl!!, project.name, image)
                    }

                    for ((name, content) in (content as Map<String, String>).entries) {
                        if (globalDecision == ConfirmChangesDialog.ResultType.SKIP_ALL) continue
                        if (globalDecision == ConfirmChangesDialog.ResultType.APPLY_ALL) {

                                if (content != null) {
                                    FileUtils.createFile(project, file, name, content)
                                    createdCount++
                                }
                                continue

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
                }
            }
        } else {
            // Диалог для файла
            val options = arrayOf("Edit content", "Comment content", "Describe content")
            val selectedOption = SelectActionDialog(options).showAndGetSelection()

            when (selectedOption) {
                "Edit content" -> {
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
                "Comment content" -> {
                    CommentCodeAction().actionPerformed(e)
                }
                "Describe content" -> {
                    FileDescriptionAction().actionPerformed(e)
                }
                // Логика для других опций ("Comment content", "Describe content") может быть добавлена здесь
            }
        }
    }
}

fun showCustomDialog(options: Array<String>): String? {
    var selectedOption: String? = null // Храним выбранный вариант
    val frame = JFrame("Select action")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.layout = GridLayout(options.size, 1) // Колонка кнопок

    options.forEach { option ->
        val button = JButton(option)
        button.addActionListener {
            selectedOption = option // Сохраняем выбранный вариант
            frame.dispose() // Закрываем диалог
        }
        frame.add(button)
    }

    frame.pack()
    frame.setLocationRelativeTo(null) // Центрировать окно
    frame.isVisible = true

    // Ждем, пока диалог закроется
    while (frame.isVisible) {
        Thread.sleep(100) // Ожидание закрытия диалога
    }

    return selectedOption // Возвращаем выбранный вариант
}