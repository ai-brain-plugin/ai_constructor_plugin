package settings

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*
import java.awt.BorderLayout
import java.awt.Desktop
import java.net.URI

class KeyDialog : DialogWrapper(true) {
    private val textField = JTextField()

    init {
        title = "Enter API Key"
        init()
    }

  override fun createCenterPanel(): javax.swing.JComponent {
    val panel = javax.swing.JPanel()
    panel.layout = javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS)
    panel.border = javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)

    val promptLabel = javax.swing.JLabel("Enter your API key:")
    promptLabel.alignmentX = javax.swing.JComponent.LEFT_ALIGNMENT

    // Предполагается, что textField объявлен где-то в классе; в противном случае можно раскомментировать строку ниже
    // val textField = javax.swing.JTextField(30)
    textField.alignmentX = javax.swing.JComponent.LEFT_ALIGNMENT
    // чтобы текстовое поле растягивалось по ширине контейнера, но сохраняло высоту
    textField.maximumSize = java.awt.Dimension(Int.MAX_VALUE, textField.preferredSize.height)

    // Ссылка на авторизацию
    val infoLabel = javax.swing.JLabel("<html><a href=\"https://t.me/ai_constructor_plugin_bot\">Get API Key</a></html>")
    infoLabel.alignmentX = javax.swing.JComponent.LEFT_ALIGNMENT
    infoLabel.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    infoLabel.border = javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0)
    infoLabel.addMouseListener(object : java.awt.event.MouseAdapter() {
        override fun mouseClicked(e: java.awt.event.MouseEvent?) {
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI("https://t.me/ai_constructor_plugin_bot"))
            } catch (ex: Exception) {
                // Игнорируем ошибки открытия браузера, можно вывести лог при необходимости
            }
        }
    })

    panel.add(promptLabel)
    panel.add(javax.swing.Box.createRigidArea(java.awt.Dimension(0, 6)))
    panel.add(textField)
    panel.add(javax.swing.Box.createRigidArea(java.awt.Dimension(0, 10)))
    panel.add(infoLabel)

    return panel
}

    fun getKey(): String = textField.text
}