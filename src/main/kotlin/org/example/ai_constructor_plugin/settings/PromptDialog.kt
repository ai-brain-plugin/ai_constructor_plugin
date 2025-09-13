package settings

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.*
import java.awt.BorderLayout
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.Transferable
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.Image
import javax.imageio.ImageIO
import java.io.File
import java.io.IOException
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Cursor // Импортируем Cursor
import java.awt.*
import java.awt.image.BufferedImage   // ✅ вот это добавляем

class PromptDialog : DialogWrapper(true) {
    private val textArea = JTextArea(10, 50) // Создание текстовой области с размерами 10x50
    private var image: Image? = null // Переменная для хранения изображения
    private val imagePanel = JPanel() // Панель для отображения изображения

    init {
        // Установка параметров для текстовой области, чтобы текст не выходил за рамки
        textArea.lineWrap = true // Включение переноса строк
        textArea.wrapStyleWord = true // Включение переноса по словам
        title = "Enter AI Prompt" // Установка заголовка диалогового окна
        init() // Инициализация диалогового окна
    }
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Заголовок
        panel.add(JLabel("Enter prompt for AI:"), BorderLayout.NORTH)

        // Контейнер для текста и выбора изображения
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        // Поле для текста
        contentPanel.add(JScrollPane(textArea))

        // Отступ между текстом и выбором картинки
        contentPanel.add(Box.createRigidArea(Dimension(0, 15)))

        // Панель для выбора картинки
        imagePanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(java.awt.Color.WHITE, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )

        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
        }

        val label = JLabel("Select the image (PNG, JPG)").apply {
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    chooseImageFromFile()
                }
            })
        }

        buttonPanel.add(label)
        buttonPanel.add(Box.createRigidArea(Dimension(0, 10)))
        imagePanel.add(buttonPanel)

        contentPanel.add(imagePanel)

        // Отступ
        contentPanel.add(Box.createRigidArea(Dimension(0, 15)))

// Метка про стоимость
        val costLabel = JLabel("<html><small style='color:gray;'>Cost of request using image = 5 regular requests</small></html>")
        costLabel.alignmentX = Component.CENTER_ALIGNMENT
        contentPanel.add(costLabel)


        panel.add(contentPanel, BorderLayout.CENTER)
        return panel
    }

    fun getPrompt(): String = textArea.text // Получение текста из текстовой области

    fun getImage(): Image? = image // Метод для получения изображения
    fun getImageAsFile(): File? {
        image?.let { img ->
            val bufferedImage = if (img is BufferedImage) {
                img
            } else {
                // Конвертация Image → BufferedImage
                val bImage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
                val g2d = bImage.createGraphics()
                g2d.drawImage(img, 0, 0, null)
                g2d.dispose()
                bImage
            }

            val tempFile = File.createTempFile("image", ".png").apply {
                deleteOnExit() // Файл удалится при завершении программы
            }

            ImageIO.write(bufferedImage, "png", tempFile)
            return tempFile
        }
        return null
    }


    fun pasteImageFromClipboard() {
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable: Transferable = clipboard.getContents(null)

        if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                image = transferable.getTransferData(DataFlavor.imageFlavor) as Image
                updateImagePanel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadImageFromFile(file: File) {
        try {
            image = ImageIO.read(file)
            updateImagePanel()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun updateImagePanel() {
        imagePanel.removeAll()
        val imageIcon = ImageIcon(image)
        val scaledImage = imageIcon.image.getScaledInstance(-1, 200, Image.SCALE_SMOOTH) // Масштабируем изображение
        imagePanel.add(JLabel(ImageIcon(scaledImage))) // Отображаем масштабированное изображение
        imagePanel.preferredSize = Dimension(400, 200) // Установка ширины и высоты панели
        imagePanel.revalidate()
        imagePanel.repaint()
    }

    private fun chooseImageFromFile() {
        val fileChooser = JFileChooser()
        val returnValue = fileChooser.showOpenDialog(null)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            loadImageFromFile(fileChooser.selectedFile)
        }
    }
}