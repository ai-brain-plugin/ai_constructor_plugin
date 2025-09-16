package org.aibrain.ai_constructor_plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import utils.FolderTreeBuilder
import javax.swing.JOptionPane
import network.ApiClient
import settings.PluginSettings
import javax.swing.JTextArea
import javax.swing.JScrollPane
import com.intellij.openapi.ui.Messages
import settings.ExplainFolderDialog
import utils.RunWithProgress
import settings.PromptDirectoryDialog

class ExplainFolderAction : AnAction() {
    public override fun actionPerformed(e: AnActionEvent) {
        val folder: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = e.project ?: return
        if (folder == null || !folder.isDirectory()) {
            return
        }

        // строим дерево файлов
        val folderStructure: String? = FolderTreeBuilder.buildTree(folder)

        // выбор языка
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




        val settings = PluginSettings.getInstance()
        val apiKey = settings.apiKey ?: run {
            Messages.showErrorDialog(project, "API Key is missing", "AI Plugin")
            return
        }
        val serverUrl = settings.serverUrl ?: run {
            Messages.showErrorDialog(project, "Server URL is missing", "AI Plugin")
            return
        }
       val promptDialog = PromptDirectoryDialog()
        if (!promptDialog.showAndGet()) return
        val promptFromDialog = promptDialog.getPrompt()
        val image = null

        // формируем промпт
        val prompt: String = if (promptFromDialog.isBlank()) {
            String.format(
                "Explain the purpose of the following folder and its contents. Language: %s.\n\n%s",
                if (language == "ru") "Russian" else "English",
                folderStructure
            )
        } else {
            String.format(
                "There is a folder with the following composition: \"%s\". \n We have the following task: %s\n \"In aswer use language: %s.",
                folderStructure,
                promptFromDialog,
                if (language == "ru") "Russian" else "English",
            )
        }
        val result = RunWithProgress.runWithProgress(project, "Loading") {
            ApiClient.sendPrompt(apiKey, prompt, serverUrl, null, image)
        }
                if (result != null) {

                    val dialog = ExplainFolderDialog(project, result)
                    dialog.show()

                } else {
                    // Messages.showErrorDialog(project, "Error generating explain Folder", "AI Plugin")
                }


    }


}