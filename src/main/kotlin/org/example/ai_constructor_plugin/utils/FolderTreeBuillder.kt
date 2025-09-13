package utils

import com.intellij.openapi.vfs.VirtualFile

object FolderTreeBuilder {
    fun buildTree(folder: VirtualFile?): String {
        val sb: java.lang.StringBuilder = java.lang.StringBuilder()
        FolderTreeBuilder.buildTreeRecursive(folder, sb, 0)
        return sb.toString()
    }

    private fun buildTreeRecursive(file: VirtualFile?, sb: java.lang.StringBuilder, depth: Int) {
        if (file == null) return

        // отступы для красивого форматирования
        sb.append("   ".repeat(depth))
            .append("- ")
            .append(file.name)
            .append("\n")

        if (file.isDirectory) {
            for (child in file.children) {
                buildTreeRecursive(child, sb, depth + 1)
            }
        }
    }
}