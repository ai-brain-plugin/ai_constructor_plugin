package settings

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*
import java.awt.BorderLayout

class PromptDirectoryDialog : DialogWrapper(true) {
    private val textArea = JTextArea(10, 50)

    init {
        // Установка параметров для текстовой области, чтобы текст не выходил за рамки
        textArea.lineWrap = true // Перенос строк
        textArea.wrapStyleWord = true // Переносить по словам
        title = "Enter AI Prompt"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(JLabel("Describe in more detail what you want AI to find/describe in the folder (if the prompt is empty, it will simply send its description):"), BorderLayout.NORTH)
        panel.add(JScrollPane(textArea), BorderLayout.CENTER)
        return panel
    }

    fun getPrompt(): String = textArea.text
}