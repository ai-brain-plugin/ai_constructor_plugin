package settings

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel

class ConfirmChangesDialog(
    private val project: Project,
    private val fileName: String,
    private val fileContent: String,
    private val manyFiles: Boolean

) : DialogWrapper(true) {
    enum class ResultType {
        APPLY_ONE, APPLY_ALL, SKIP_ONE, SKIP_ALL
    }

    var resultType: ResultType = ResultType.SKIP_ONE

    var applyToAll: Boolean = false
        private set
    var skipAll: Boolean = false
        private set
    var confirmed: Boolean = false
        private set

    init {
        title = "Confirmation of changes for the file ${fileName}"
        init()
    }


    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Создаём редактор с подсветкой синтаксиса
        val fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName)
        val document = EditorFactory.getInstance().createDocument(fileContent)
        val editor = EditorFactory.getInstance().createEditor(document, project, fileType, true)

        val settings = editor.settings
        settings.isLineNumbersShown = true
        settings.isFoldingOutlineShown = true
        settings.isRightMarginShown = false

        panel.add(editor.component, BorderLayout.CENTER)
        return panel
    }

    override fun createActions(): Array<Action> {
        val applyAction = object : DialogWrapperAction("Apply") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                confirmed = true
                resultType = ResultType.APPLY_ONE

                close(OK_EXIT_CODE)
            }
        }

        val skipAction = object : DialogWrapperAction("Skip") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                confirmed = false
                resultType = ResultType.SKIP_ONE

                close(CANCEL_EXIT_CODE)
            }
        }

        val applyAllAction = object : DialogWrapperAction("Apply all") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                confirmed = true
                applyToAll = true
                resultType = ResultType.APPLY_ALL

                close(OK_EXIT_CODE)
            }
        }

        val skipAllAction = object : DialogWrapperAction("Skip all") {
            override fun doAction(e: java.awt.event.ActionEvent?) {
                confirmed = false
                skipAll = true
                resultType = ResultType.SKIP_ALL
                close(OK_EXIT_CODE)
            }
        }

        if(manyFiles)
            return arrayOf(applyAction, skipAction, applyAllAction, skipAllAction)
        return arrayOf(applyAction, skipAction)

    }
}
