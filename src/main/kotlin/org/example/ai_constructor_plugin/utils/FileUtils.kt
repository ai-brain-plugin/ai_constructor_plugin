package utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

object FileUtils {
    private fun VirtualFile.writeContent(content: String) {
        setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
    }

    fun overwriteFile(project: Project, file: VirtualFile, newContent: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            file.writeContent(newContent)
        }
    }
fun createFile(project: Project, folder: VirtualFile, name: String, content: String) {
    WriteCommandAction.runWriteCommandAction(project) {
        // Убираем кавычки из имени файла
        val cleanedName = name.trim('\'', '`', ' ')

        // Разделяем путь на директорию и имя файла
        val pathParts = cleanedName.split("/")
        val fileName = pathParts.last() // Имя файла
        val directoryPath = pathParts.dropLast(1).joinToString("/") // Путь к папке

        var currentFolder = folder

        // Проверяем и создаем папки по пути
        if (directoryPath.isNotEmpty()) {
            directoryPath.split("/").forEach { part ->
                currentFolder = if (currentFolder.findChild(part) == null) {
                    currentFolder.createChildDirectory(this, part)
                } else {
                    currentFolder.findChild(part)!!
                }
            }
        }

        // Создаем файл в конечной папке
        val newFile = currentFolder.createChildData(this, fileName)
        newFile.writeContent(content)
    }
}
}