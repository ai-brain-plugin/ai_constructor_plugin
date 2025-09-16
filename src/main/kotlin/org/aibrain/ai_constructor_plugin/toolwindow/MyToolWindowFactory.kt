package org.aibrain.ai_constructor_plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import settings.PluginSettings
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ItemEvent
import java.net.URI
import javax.swing.*
import javax.swing.border.EmptyBorder
import network.ApiClient
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent


class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val mainPanel = JPanel()
        val cardLayout = CardLayout()
        mainPanel.layout = cardLayout

        val settings = PluginSettings.getInstance()

        // ===== Страница настроек =====
        val settingsPanel = JPanel()
        settingsPanel.layout = BoxLayout(settingsPanel, BoxLayout.Y_AXIS)

        settingsPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // --- ЛОГО ---
        val rawIcon = ImageIcon(javaClass.getResource("/logo.png"))

// Пропорциональное масштабирование по высоте = 100 px
        val scaledImage = rawIcon.image.getScaledInstance(
            -1, // ширина автоматически
            100,
            java.awt.Image.SCALE_SMOOTH
        )

        val logoIcon = ImageIcon(scaledImage)
        val logoLabel = JLabel(logoIcon)
        logoLabel.alignmentX = JComponent.LEFT_ALIGNMENT
        settingsPanel.add(logoLabel)
        settingsPanel.add(Box.createVerticalStrut(15))
        // --- Вступительный текст (адаптивная высота и ширина) ---
// Используем JTextArea (или JEditorPane для HTML) и оборачиваем в BorderLayout-wrapper,
// чтобы он всегда занимал доступную ширину контейнера.
        val welcomeText = JTextArea(
            """
    Welcome to AI Brain Plugin!

    Key features include:
    - Get a description of a project and its contents in one click, in your preferred language (EN/RU) [Explain Folder]
    - Get a description of a file and its contents [Describe content]
    - Modify entire files or selected code fragments using prompts [Edit file / Select cut]
    - Generate code at any location in the editor [Enter prompt in editor]
    - Multi-file generation in any directory based on a prompt [Generate files]
    - Comment code in your preferred language (EN/RU) [Comment content]
    - Get an answer to your question [Go to chat]
    """.trimIndent()
        )
        welcomeText.isEditable = false
        welcomeText.isOpaque = false
        welcomeText.lineWrap = true
        welcomeText.wrapStyleWord = true
        welcomeText.foreground = Color(200, 200, 200)
        welcomeText.font = welcomeText.font.deriveFont(13f)

// Вместо fixed maximumSize по высоте — только ширину
        welcomeText.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        welcomeText.preferredSize = Dimension(0, welcomeText.preferredSize.height)

        welcomeText.alignmentX = Component.LEFT_ALIGNMENT

// обертка с BorderLayout — гарантирует, что приветствие займёт всю доступную ширину в Column-BoxLayout
        val welcomeWrapper = JPanel(BorderLayout())
        welcomeWrapper.isOpaque = false
        welcomeWrapper.alignmentX = Component.LEFT_ALIGNMENT
        welcomeWrapper.add(welcomeText, BorderLayout.CENTER)
// если нужно чуть отступов влево/вправо, можно добавить EmptyBorder:
        welcomeWrapper.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)

        settingsPanel.add(welcomeWrapper)
        settingsPanel.add(Box.createVerticalStrut(10))



        // --- Делаем панель скроллируемой ---
        val scrollPane = JScrollPane(settingsPanel)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

        val apiPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        val apiField = JTextField(settings.apiKey ?: "", 20)
        apiField.maximumSize = apiField.preferredSize
        apiPanel.add(JLabel("API Key:"))
        apiPanel.add(apiField)
        apiPanel.alignmentX = JComponent.LEFT_ALIGNMENT
        apiPanel.maximumSize = apiPanel.preferredSize
        settingsPanel.add(apiPanel)
        settingsPanel.add(Box.createVerticalStrut(5))

        // Ссылка на авторизацию
        val infoLabel = JLabel("<html><a href=https://t.me/ai_constructor_plugin_bot>Get API Key</a></html>")
        infoLabel.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
        infoLabel.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                Desktop.getDesktop().browse(URI("https://t.me/ai_constructor_plugin_bot"))
            }
        })
        infoLabel.alignmentX = JComponent.LEFT_ALIGNMENT
        settingsPanel.add(infoLabel)
        settingsPanel.add(Box.createVerticalStrut(10))

        // Server URL
        val urlPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        val urlField = JTextField(settings.serverUrl ?: "http://127.0.0.1:5000", 20)
        urlField.maximumSize = urlField.preferredSize
        urlPanel.add(JLabel("Server URL:"))
        urlPanel.add(urlField)
        urlPanel.maximumSize = urlPanel.preferredSize
        urlPanel.alignmentX = JComponent.LEFT_ALIGNMENT
        //settingsPanel.add(urlPanel)
        //settingsPanel.add(Box.createVerticalStrut(10))

        apiField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = save()

            private fun save() {
                settings.apiKey = apiField.text.trim()
            }
        })

        urlField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = save()

            private fun save() {
                settings.serverUrl = urlField.text.trim()
            }
        })

        // --- Кнопки и остальной UI как у тебя ---
        val moreAboutPluginButton = JButton("More about plugin")
        moreAboutPluginButton.alignmentX = JComponent.LEFT_ALIGNMENT
        moreAboutPluginButton.addActionListener {
            showDocumentation(cardLayout, mainPanel)
        }
        settingsPanel.add(moreAboutPluginButton)
        settingsPanel.add(Box.createVerticalStrut(10))

        val askButton = JButton("Go to chat")
        askButton.alignmentX = JComponent.LEFT_ALIGNMENT
        settingsPanel.add(askButton)
        settingsPanel.add(Box.createVerticalStrut(10))

        // Создаем чекбокс для запоминания чата
        val rememberChatCheckBox = JCheckBox("Memorize user queries (Pro tariff)")
        rememberChatCheckBox.alignmentX = JComponent.LEFT_ALIGNMENT
        settingsPanel.add(rememberChatCheckBox)
        settingsPanel.add(Box.createVerticalStrut(10))

        rememberChatCheckBox.isSelected = settings.isChatRemembered ?: false

        rememberChatCheckBox.addItemListener { event: ItemEvent ->
            settings.isChatRemembered = event.stateChange == ItemEvent.SELECTED
        }

        mainPanel.add(scrollPane, "SETTINGS")

        // ===== Страница чата =====
        val chatPanel = ChatPanel(project, settings, cardLayout, mainPanel)

        // ===== Страница документации =====
        val documentationPanel = JPanel()
        documentationPanel.layout = BoxLayout(documentationPanel, BoxLayout.Y_AXIS)

        val backButton = JButton("Back")
        backButton.alignmentX = JComponent.LEFT_ALIGNMENT
        backButton.addActionListener { cardLayout.show(mainPanel, "SETTINGS") }
        documentationPanel.add(backButton)


        // ===== Добавляем панели в CardLayout =====
        mainPanel.add(chatPanel, "CHAT")
        cardLayout.show(mainPanel, "SETTINGS")
        mainPanel.add(documentationPanel, "DOCUMENTATION")
        askButton.addActionListener { cardLayout.show(mainPanel, "CHAT") }

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }


    private fun showDocumentation(cardLayout: CardLayout, mainPanel: JPanel) {
        val documentationPanel = JPanel()


        documentationPanel.layout = BoxLayout(documentationPanel, BoxLayout.Y_AXIS)
        documentationPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val docLabel = JLabel("<html><b>Documentation</b><br>Choose a feature to view instructions:</html>")
        documentationPanel.add(docLabel)
        documentationPanel.add(Box.createVerticalStrut(10))

        val features = mapOf(
            "Generation with image" to "/gifs/withImage.gif",
            "Edit file" to "/gifs/fileUpdate.gif",
            "Generate files" to "/gifs/generateFiles.gif",
            "Describe folder" to "/gifs/explainFolder.gif",
            //"Describe file" to "/gifs/describe_file.gif",
            "Comment files" to "/gifs/fileComment.gif",
            "Select cut" to "/gifs/generateByCut.gif",
            "Enter prompt" to "/gifs/generatePrompt.gif",
            "Chat" to "/gifs/chat.gif",

            )

        features.forEach { (feature, path) ->
            val button = JButton(feature)
            button.addActionListener {
                showFeatureInstruction(feature, path, cardLayout, mainPanel)
            }
            documentationPanel.add(button)
            documentationPanel.add(Box.createVerticalStrut(5))
        }
        val backButton = JButton("← Back")
        backButton.addActionListener { cardLayout.show(mainPanel, "SETTINGS") }
        documentationPanel.add(backButton, BorderLayout.WEST)


        mainPanel.add(documentationPanel, "DOCUMENTATION")
        cardLayout.show(mainPanel, "DOCUMENTATION")
    }
    private fun showFeatureInstruction(
        feature: String,
        gifPath: String,
        cardLayout: CardLayout,
        mainPanel: JPanel
    ) {
        val featurePanel = JPanel(BorderLayout())
        featurePanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Верхняя панель: кнопка Back + заголовок
        val topPanel = JPanel(BorderLayout())
        val backButton = JButton("← Back")
        backButton.addActionListener { cardLayout.show(mainPanel, "DOCUMENTATION") }
        topPanel.add(backButton, BorderLayout.WEST)

        val title = JLabel("<html><h2>$feature</h2></html>", SwingConstants.CENTER)
        topPanel.add(title, BorderLayout.CENTER)
        featurePanel.add(topPanel, BorderLayout.NORTH)

        // Центральная панель для гифки (прилегает к верху)
        val gifContainer = JPanel(BorderLayout())
        gifContainer.border = null

        val originalIcon = ImageIcon(javaClass.getResource(gifPath))
        val gifLabel = JLabel(originalIcon)
        gifLabel.horizontalAlignment = SwingConstants.CENTER
        gifContainer.add(gifLabel, BorderLayout.NORTH) // размещаем вверху

        featurePanel.add(gifContainer, BorderLayout.CENTER) // оставляем место для прокрутки, если нужно

// Слушатель ресайза для динамического масштабирования
        featurePanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                val panelWidth = featurePanel.width - 20 // учитываем отступы
                val aspect = originalIcon.iconHeight.toDouble() / originalIcon.iconWidth
                val newHeight = (panelWidth * aspect).toInt()

                val scaledImage = originalIcon.image.getScaledInstance(panelWidth, newHeight, java.awt.Image.SCALE_DEFAULT)
                gifLabel.icon = ImageIcon(scaledImage)
                gifLabel.revalidate()
            }
        })


        mainPanel.add(featurePanel, feature.uppercase())
        cardLayout.show(mainPanel, feature.uppercase())
    }


}


class ChatPanel(
    private val project: Project,
    private val settings: PluginSettings,
    private val cardLayout: CardLayout,
    private val mainPanel: JPanel
) : JPanel(BorderLayout()) {

    private val chatArea = JPanel()  // Все сообщения
    private val scrollPane = JScrollPane(chatArea)
    private val inputArea = HintTextArea("Enter your ask")
    private val sendButton = JButton("Send")
    private val backButton = JButton("← Back")
    private val placeholderLabel = JLabel("Ask anything")

    init {
        chatArea.layout = BoxLayout(chatArea, BoxLayout.Y_AXIS)
        chatArea.isOpaque = false
        scrollPane.verticalScrollBar.unitIncrement = 16
        scrollPane.border = EmptyBorder(5, 5, 5, 5)
        scrollPane.isOpaque = false
        scrollPane.viewport.isOpaque = false

        // Плейсхолдер по центру
        placeholderLabel.foreground = Color.WHITE
        placeholderLabel.font = placeholderLabel.font.deriveFont(Font.BOLD, 18f)
        placeholderLabel.alignmentX = Component.CENTER_ALIGNMENT
        chatArea.add(Box.createVerticalGlue())
        chatArea.add(placeholderLabel)
        chatArea.add(Box.createVerticalGlue())

        // Настройка поля ввода
        inputArea.lineWrap = true
        inputArea.wrapStyleWord = true
        inputArea.border = EmptyBorder(8, 8, 8, 8)
        inputArea.background = Color(0, 0, 0, 0)
        inputArea.foreground = Color.WHITE
        inputArea.caretColor = Color.WHITE
        inputArea.selectionColor = Color(100, 100, 100, 100) // корректное выделение
        val fontMetrics = inputArea.getFontMetrics(inputArea.font)
        val rowHeight = fontMetrics.height
        val maxHeight = rowHeight * 6
        val inputScroll = JScrollPane(inputArea)
        inputScroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        inputScroll.preferredSize = Dimension(0, rowHeight * 4) // стартовая высота (2 строки)
        inputScroll.maximumSize = Dimension(Int.MAX_VALUE, maxHeight)

        // Контейнер ввода с тенью и градиентом
        // Поле ввода с закругленной серой рамкой
        // Скруглённый контейнер с тенью
        val inputWrapper = object : JPanel(BorderLayout()) {
            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val shadowColor = Color(0, 0, 0, 50)
                g2.color = shadowColor
                g2.fillRoundRect(2, 2, width - 4, height - 4, 15, 15)
                g2.color = background
                g2.fillRoundRect(0, 0, width - 4, height - 4, 15, 15)
                super.paintComponent(g)
                super.paintComponent(g)
            }
        }
        inputWrapper.isOpaque = false
        inputWrapper.border = EmptyBorder(5, 10, 5, 10)
        inputWrapper.add(inputScroll.apply {
            border = BorderFactory.createEmptyBorder() // убираем границы у скролла
            isOpaque = false
            viewport.isOpaque = false
        }, BorderLayout.CENTER)
        inputWrapper.layout = BoxLayout(inputWrapper, BoxLayout.X_AXIS)
        inputWrapper.add(Box.createRigidArea(Dimension(8, 0))) // отступ

        sendButton.preferredSize = Dimension(80, 40) // фиксированная ширина/высота
        sendButton.maximumSize = Dimension(80, 40)
        sendButton.minimumSize = Dimension(80, 40)
        inputWrapper.add(sendButton, BorderLayout.EAST)



        val topBar = JPanel(FlowLayout(FlowLayout.LEFT))
        topBar.add(backButton)

        add(topBar, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(inputWrapper, BorderLayout.SOUTH)

        backButton.addActionListener { cardLayout.show(mainPanel, "SETTINGS") }

        sendButton.addActionListener {
            val text = inputArea.text.trim()
            if (text.isNotEmpty()) {
                hidePlaceholder()
                addMessage("You", text, true)
                inputArea.text = ""

                // "AI: Generating answer..."
                val placeholder = addMessage("AI", "Generating answer...", false)

                Thread {
                    try {
                        var projectName: String? = project.name
                        if ((settings.isChatRemembered ?: false) == false) projectName = null
                        val response = ApiClient.sendPrompt(settings.apiKey ?: "", text, settings.serverUrl ?: "", projectName, null)
                        SwingUtilities.invokeLater {
                            updateMessage(placeholder, response ?: "Ошибка: пустой ответ")
                        }
                    } catch (e: Exception) {
                        SwingUtilities.invokeLater {
                            updateMessage(placeholder, "Ошибка: ${e.message}")
                        }
                    }
                }.start()
            }
        }
    }

    private fun addMessage(author: String, message: String, isUser: Boolean): JPanel {
        val outerPanel = JPanel()
        outerPanel.layout = BoxLayout(outerPanel, BoxLayout.X_AXIS)
        outerPanel.isOpaque = false

        val bubble = object : JPanel() {
            override fun getPreferredSize(): Dimension {
                // Ширина bubble = 70% ширины чата
                val width = (chatArea.width * 0.7).toInt()
                val d = super.getPreferredSize()
                d.width = width
                return d
            }

            override fun getMaximumSize(): Dimension {
                // Максимальная ширина = 70%, высота = по содержимому
                val d = preferredSize
                return d
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                val bgColor =  Color(0x3A3A3A)
                g2.color = bgColor
                g2.fillRoundRect(0, 0, width - 1, height - 1, 15, 15)
                super.paintComponent(g)
            }
        }

        bubble.layout = BoxLayout(bubble, BoxLayout.Y_AXIS)
        bubble.border = EmptyBorder(8, 10, 8, 10)
        bubble.isOpaque = false
        bubble.alignmentY = Component.TOP_ALIGNMENT
        bubble.alignmentX = Component.LEFT_ALIGNMENT
        bubble.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val authorLabel = JLabel(author)
        authorLabel.font = authorLabel.font.deriveFont(Font.BOLD)
        authorLabel.foreground = Color.LIGHT_GRAY
        authorLabel.alignmentX = Component.LEFT_ALIGNMENT
        bubble.add(authorLabel)

        renderMessageContent(bubble, message)

        if (isUser) {
            outerPanel.add(Box.createHorizontalGlue())
            outerPanel.add(bubble)
        } else {
            outerPanel.add(bubble)
            outerPanel.add(Box.createHorizontalGlue())
        }

        chatArea.add(outerPanel)
        chatArea.add(Box.createVerticalStrut(8))
        chatArea.revalidate()
        scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
        return bubble
    }

    private fun renderMessageContent(bubble: JPanel, message: String) {
        val regex = Regex("```(\\w+)?\\n([\\s\\S]*?)```", RegexOption.MULTILINE)
        var lastIndex = 0
        for (match in regex.findAll(message)) {
            if (match.range.first > lastIndex) {
                val textPart = message.substring(lastIndex, match.range.first).trim()
                if (textPart.isNotEmpty()) {
                    val textArea = JTextArea(textPart)
                    textArea.isEditable = false
                    textArea.lineWrap = true
                    textArea.wrapStyleWord = true
                    textArea.foreground = Color.WHITE
                    textArea.background = Color(0, 0, 0, 0)
                    textArea.border = null
                    textArea.alignmentX = Component.LEFT_ALIGNMENT
                    textArea.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE) // растягиваем по ширине
                    bubble.add(textArea)
                }
            }
            val lang = match.groups[1]?.value ?: "text"
            val code = match.groups[2]?.value ?: ""
            bubble.add(makeCodeBlock(code, lang))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < message.length) {
            val textPart = message.substring(lastIndex).trim()
            if (textPart.isNotEmpty()) {
                val textArea = JTextArea(textPart)
                textArea.isEditable = false
                textArea.lineWrap = true
                textArea.wrapStyleWord = true
                textArea.foreground = Color.WHITE
                textArea.background = Color(0, 0, 0, 0)
                textArea.border = null
                textArea.alignmentX = Component.LEFT_ALIGNMENT
                textArea.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE) // растягиваем по ширине
                bubble.add(textArea)
            }
        }
    }


    private fun updateMessage(bubble: JPanel, newMessage: String) {
        bubble.removeAll()
        val authorLabel = JLabel("AI")
        authorLabel.font = authorLabel.font.deriveFont(Font.BOLD)
        authorLabel.foreground = Color.LIGHT_GRAY
        authorLabel.alignmentX = Component.LEFT_ALIGNMENT
        bubble.add(authorLabel)

        renderMessageContent(bubble, newMessage)

        bubble.revalidate()
        bubble.repaint()
        scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
    }

    private fun makeCodeBlock(code: String, lang: String): JComponent {
        val textArea = JTextArea(code.trim())
        textArea.isEditable = false
        textArea.font = Font("Monospaced", Font.PLAIN, 13)
        textArea.background = Color(30, 30, 30)
        textArea.foreground = Color(230, 230, 230)
        textArea.lineWrap = true  // Включаем перенос строк
        textArea.wrapStyleWord = true  // Перенос слов
        val fm = textArea.getFontMetrics(textArea.font)
        val lineHeight = fm.height
        val lines = code.lines().size
        val height = lineHeight * (lines + 1)

        val topPanel = JPanel(BorderLayout())
        topPanel.background = Color(45, 45, 45)
        topPanel.border = EmptyBorder(3, 5, 3, 5)

        val titleLabel = JLabel(lang)
        titleLabel.foreground = Color.WHITE

        val copyButton = JButton("📋 Copy")
        copyButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(textArea.text)
            clipboard.setContents(selection, selection)
            copyButton.text = "✅ Copied"
            copyButton.isEnabled = false
            Timer(1500) {
                copyButton.text = "📋 Copy"
                copyButton.isEnabled = true
            }.start()
        }

        topPanel.add(titleLabel, BorderLayout.WEST)
        topPanel.add(copyButton, BorderLayout.EAST)

        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createLineBorder(Color(80, 80, 80))
        panel.add(topPanel, BorderLayout.NORTH)
        panel.add(textArea, BorderLayout.CENTER)
        //panel.preferredSize = Dimension(Int.MAX_VALUE, height + 30)
        panel.alignmentX = Component.LEFT_ALIGNMENT

        return panel
    }

    private fun hidePlaceholder() {
        if (chatArea.components.contains(placeholderLabel)) {
            chatArea.removeAll()
            chatArea.revalidate()
            chatArea.repaint()
        }
    }

    private fun showPlaceholder() {
        chatArea.removeAll()
        chatArea.add(Box.createVerticalGlue())
        chatArea.add(placeholderLabel)
        chatArea.add(Box.createVerticalGlue())
        chatArea.revalidate()
        chatArea.repaint()
    }
}

class HintTextArea(private val hint: String) : JTextArea() {
    init {
        isOpaque = false
        border = EmptyBorder(8, 8, 8, 8)
        lineWrap = true
        wrapStyleWord = true
        foreground = Color.WHITE
        caretColor = Color.WHITE
        selectionColor = Color(100, 100, 100, 100)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2.color = Color(255, 255, 255, 0)
        g2.fillRoundRect(0, 0, width - 1, height - 2, 15, 15)

        super.paintComponent(g)

        if (text.isEmpty()) {
            g2.color = Color(200, 200, 200)
            g2.font = font.deriveFont(Font.ITALIC)
            g2.drawString(hint, insets.left + 5, g2.fontMetrics.ascent + insets.top)
        }
    }
}

