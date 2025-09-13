import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

class OpenFile {

    fun openFile(): File? {
        // Устанавливаем тему
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        // Создаем JFileChooser
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Выберите файл"

        // Показать диалог выбора файла
        val result = fileChooser.showOpenDialog(null)

        return if (result == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile // Возвращаем выбранный файл
        } else {
            null // Если отмена, возвращаем null
        }
    }
}
