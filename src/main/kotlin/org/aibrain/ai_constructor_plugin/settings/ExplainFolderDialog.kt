package settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.*

class ExplainFolderDialog(project: Project, private val content: String) : DialogWrapper(project) {
    init {
        init()
        title = "AI Plugin"
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        val textArea = JTextArea(content)  // используем content, а не text
        textArea.isEditable = false

        // Настраиваем поведение текстового поля
        textArea.lineWrap = true  // Включаем перенос строк
        textArea.wrapStyleWord = true  // Перенос строк происходит по словам

        val scrollPane = JScrollPane(textArea)
        scrollPane.preferredSize = java.awt.Dimension(600, 400)
        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }
}