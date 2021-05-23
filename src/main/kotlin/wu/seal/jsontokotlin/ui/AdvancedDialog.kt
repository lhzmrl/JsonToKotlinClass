package wu.seal.jsontokotlin.ui

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBDimension
import extensions.wu.seal.ClassNameSuffixSupport
import wu.seal.jsontokotlin.PropertyGlobalMode
import wu.seal.jsontokotlin.model.ConfigManager
import wu.seal.jsontokotlin.model.DefaultValueStrategy
import wu.seal.jsontokotlin.model.PropertyTypeStrategy
import wu.seal.jsontokotlin.model.TargetJsonConverter
import java.awt.Component
import javax.swing.Action
import javax.swing.JComponent

/**
 *
 * Created by Seal.Wu on 2017/9/13.
 */

class AdvancedDialog(canBeParent: Boolean) : DialogWrapper(canBeParent) {

    private val gson = Gson()
    lateinit var jbTabbedPane: JBTabbedPane
    private var advancedPropertyTab: AdvancedPropertyTab? = null
    private var advancedAnnotationTab: AdvancedAnnotationTab? = null
    private var advancedOtherTab: AdvancedOtherTab? = null
    private var extensionsTab: ExtensionsTab? = null

    init {
        init()
        title = "Advanced"
    }


    override fun createCenterPanel(): JComponent {
        jbTabbedPane = JBTabbedPane()
        jbTabbedPane.apply {
            add("Mode", createModeTab())
            if(ConfigManager.mode == PropertyGlobalMode.Custom) {
                add("Property", createPropertyTab())
                add("Annotation", createAnnotationTab())
                add("Other", createOtherSettingTab())
                add("Extensions", createExtensionTab())
            }
            minimumSize = JBDimension(500, 300)
        }
        return jbTabbedPane
    }

    private fun createModeTab() = AdvancedModeTab(object: AdvancedModeTab.OnModeChangeListener {
        override fun onModeChange(mode: PropertyGlobalMode) {
            if(mode == PropertyGlobalMode.Ford) {
                jbTabbedPane.remove(advancedPropertyTab)
                jbTabbedPane.remove(advancedAnnotationTab)
                jbTabbedPane.remove(advancedOtherTab)
                jbTabbedPane.remove(extensionsTab)
            } else {
                jbTabbedPane.apply {
                    add("Property", createPropertyTab())
                    add("Annotation", createAnnotationTab())
                    add("Other", createOtherSettingTab())
                    add("Extensions", createExtensionTab())
                }
            }
            resolveModeProperty(mode)
        }
    }, true)

    private fun createPropertyTab(): Component {
        advancedPropertyTab = AdvancedPropertyTab(true)
        return advancedPropertyTab!!
    }

    private fun createAnnotationTab(): Component {
        advancedAnnotationTab = AdvancedAnnotationTab(true)
        return advancedAnnotationTab!!
    }

    private fun createOtherSettingTab(): Component {
        advancedOtherTab = AdvancedOtherTab(true)
        return advancedOtherTab!!
    }

    private fun createExtensionTab(): Component {
        extensionsTab = ExtensionsTab(true)
        return extensionsTab!!
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction)
    }

    private fun resolveModeProperty(mode: PropertyGlobalMode) {
        when(mode) {
            PropertyGlobalMode.Ford -> {
                ConfigManager.isPropertiesVar = true
                ConfigManager.propertyTypeStrategy = PropertyTypeStrategy.JavaStyle
                ConfigManager.defaultValueStrategy = DefaultValueStrategy.AllowNull
                ConfigManager.targetJsonConverterLib = TargetJsonConverter.None
                ConfigManager.isInnerClassModel = false
                setConfig(ClassNameSuffixSupport.suffixKeyEnable, "true")
                setConfig(ClassNameSuffixSupport.suffixKey, "")
            }
            else -> {

            }
        }
    }

    private fun setConfig(key: String, value: String) {
        val configs = gson.fromJson(ConfigManager.extensionsConfig, JsonObject::class.java) ?: JsonObject()
        configs.addProperty(key, value)
        ConfigManager.extensionsConfig = gson.toJson(configs)
    }

}
