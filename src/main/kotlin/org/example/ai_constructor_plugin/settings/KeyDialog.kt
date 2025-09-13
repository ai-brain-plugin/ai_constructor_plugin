package settings

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*
import java.awt.BorderLayout

class KeyDialog : DialogWrapper(true) {
    private val textField = JTextField()

    init {
        title = "Enter API Key"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(JLabel("Enter your API key:"), BorderLayout.NORTH)
        panel.add(textField, BorderLayout.CENTER)
        return panel
    }

    fun getKey(): String = textField.text
}