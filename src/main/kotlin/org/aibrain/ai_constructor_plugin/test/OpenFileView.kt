import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter




class OpenFileView {

    private val openFile = OpenFile()

    fun showDialog() {
        // Устанавливаем тему
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        // Создаем основной фрейм
        val frame = JFrame("Open File Dialog")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Создаем панель
        val panel = JPanel(BorderLayout())
        val openButton = JButton("Открыть файл")
        val textArea = JTextArea()
        textArea.isEditable = false

        // Обработчик кнопки открытия файла
        openButton.addActionListener { openFileAndDisplayContent(textArea) }

        // Добавляем элементы на панель
        panel.add(openButton, BorderLayout.NORTH)
        panel.add(JScrollPane(textArea), BorderLayout.CENTER)

        // Настройка и отображение фрейма
        frame.contentPane.add(panel)
        frame.setSize(600, 400)
        frame.isVisible = true
    }

    private fun openFileAndDisplayContent(textArea: JTextArea) {
        val file = openFile.openFile()
        if (file != null) {
            // Читаем файл и отображаем его содержимое в текстовом поле
            val content = file.readText()
            textArea.text = content
        } else {
            textArea.text = "Операция была отменена."
        }
    }
}