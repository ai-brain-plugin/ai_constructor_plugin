package settings

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Component // Импортируем Component для управления элементами интерфейса
import java.awt.Dimension // Импортируем Dimension для задания размеров компонентов
import javax.swing.* // Импортируем все классы из javax.swing для создания графического интерфейса

class SelectActionDialog(
    private val actions: Array<String> // Массив доступных действий
) : DialogWrapper(true) {

    private var selectedAction: String? = null // Хранит выбранное действие

    init {
        title = "Select action" // Устанавливаем заголовок диалога
        init() // Инициализируем диалог
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout()) // Создаём панель с размещением по границам
        val buttonsPanel = JPanel() // Создаём панель для кнопок
        buttonsPanel.layout = BoxLayout(buttonsPanel, BoxLayout.Y_AXIS) // Устанавливаем вертикальное размещение кнопок

        actions.forEach { action -> // Проходим по массиву действий
            val button = JButton(action) // Создаём кнопку для каждого действия
            button.addActionListener {
                selectedAction = action // Запоминаем выбранное действие
                close(OK_EXIT_CODE) // Закрываем диалог с кодом успеха
            }
            button.alignmentX = Component.CENTER_ALIGNMENT // Центрируем кнопки по ширине
            buttonsPanel.add(button) // Добавляем кнопку на панель кнопок
        }

        panel.add(buttonsPanel, BorderLayout.CENTER) // Добавляем панель кнопок в центр


        return panel // Возвращаем собранную панель
    }

    fun showAndGetSelection(): String? {
        return if (showAndGet()) { // Показать диалог и проверить результат
            selectedAction // Возвращаем выбранное действие, если диалог закрыт успешно
        } else {
            null // Возвращаем null, если диалог закрыт без выбора
        }
    }
}

