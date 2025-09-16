package settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.Service

@State(name = "MyAIPluginSettings", storages = [Storage("my_ai_plugin.xml")])
@Service
class PluginSettings : PersistentStateComponent<PluginSettings> {
    var apiKey: String? = null
    var serverUrl: String = "http://5.129.234.234:5000/"
    var isChatRemembered: Boolean? = null

    override fun getState(): PluginSettings = this
    override fun loadState(state: PluginSettings) {
        this.apiKey = state.apiKey
        this.serverUrl = state.serverUrl
        this.isChatRemembered = state.isChatRemembered
    }

    companion object {
        fun getInstance(): PluginSettings =
            com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(PluginSettings::class.java)
    }
}