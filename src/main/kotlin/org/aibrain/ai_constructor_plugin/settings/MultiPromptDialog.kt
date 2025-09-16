package settings

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*
import java.awt.*

class MultiPromptDialog : DialogWrapper(true) {
    private val listModel = DefaultListModel<Pair<String, String>>()
    private val list = JList(listModel)
    private val listScroll = JScrollPane(list)
    private val addButton = JButton("+")
    private val fileField = JTextField()
    private val promptField = JTextArea()
    private val panel = JPanel(GridBagLayout()) // <--- свойство класса

    init {
        title = "Multiple Prompts"

        val gbc = GridBagConstraints()
        gbc.insets = Insets(5, 5, 5, 5)
        gbc.gridx = 0
        gbc.weightx = 1.0

        // Информационная надпись
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weighty = 0.0
        panel.add(JLabel("Multiple file generation saves the history. This means you can generate several files that interact with each other!"), gbc)

        // Поле имени файла
        gbc.gridy++
        panel.add(JLabel("File name:"), gbc)
        gbc.gridy++
        panel.add(fileField, gbc)
        fileField.maximumSize = Dimension(Int.MAX_VALUE, fileField.preferredSize.height)

        // Поле промта
        gbc.gridy++
        panel.add(JLabel("Prompt:"), gbc)
        gbc.gridy++
        gbc.fill = GridBagConstraints.BOTH
        gbc.weighty = 1.0
        promptField.lineWrap = true
        promptField.wrapStyleWord = true
        val promptScroll = JScrollPane(promptField)
        panel.add(promptScroll, gbc)

        // Кнопка "+"
        gbc.gridy++
        gbc.fill = GridBagConstraints.NONE
        gbc.weighty = 0.0
        panel.add(addButton, gbc)


        // Список промтов, изначально скрыт
        gbc.gridy++
        gbc.fill = GridBagConstraints.BOTH
        gbc.weighty = 0.5
        listScroll.isVisible = false
        panel.add(listScroll, gbc)

        // Обработчик кнопки
        addButton.addActionListener {
            val f = fileField.text.trim()
            val p = promptField.text.trim()
            if (f.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Fill both fields before add new prompt", "Error", JOptionPane.WARNING_MESSAGE)
                return@addActionListener
            }
            listModel.addElement(f to p)
            fileField.text = ""
            promptField.text = ""
            if (!listScroll.isVisible) {
                listScroll.isVisible = true
                panel.revalidate()
                panel.repaint()
            }
        }

        init()
        setSize(800, 600)
    }

    override fun createCenterPanel(): JComponent? = panel

    override fun getPreferredFocusedComponent(): JComponent? = fileField

    override fun getPreferredSize(): Dimension = Dimension(800, 600)

    fun getPrompts(): Map<String, String> =
        listModel.elements().toList().associate { it.first to it.second }
}
