package wu.seal.jsontokotlin.ui

import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBDimension
import wu.seal.jsontokotlin.PropertyGlobalMode
import wu.seal.jsontokotlin.model.ConfigManager
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel

/**
 *
 * Created by Seal.Wu on 2018/2/7.
 */
class AdvancedModeTab(var onModeChangeListener: OnModeChangeListener, isDoubleBuffered: Boolean) :
    JPanel(BorderLayout(), isDoubleBuffered) {

    init {
        jScrollPanel(JBDimension(500, 300)) {
            jVerticalLinearLayout {
                jLabel("Mode")
                jButtonGroup {
                    jRadioButton("Ford", ConfigManager.mode == PropertyGlobalMode.Ford, {
                        ConfigManager.mode = PropertyGlobalMode.Ford
                        onModeChangeListener.onModeChange(PropertyGlobalMode.Ford)
                    })
                    jRadioButton(
                        "Custom", ConfigManager.mode == PropertyGlobalMode.Custom, {
                            ConfigManager.mode = PropertyGlobalMode.Custom
                            onModeChangeListener.onModeChange(PropertyGlobalMode.Custom)
                        })
                }
            }
        }
    }

    interface OnModeChangeListener {
        fun onModeChange(mode: PropertyGlobalMode)
    }
}


