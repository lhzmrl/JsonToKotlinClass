package extensions.lee.leo

import wu.seal.jsontokotlin.interceptor.IKotlinClassInterceptor
import wu.seal.jsontokotlin.model.classscodestruct.DataClass
import wu.seal.jsontokotlin.model.classscodestruct.KotlinClass
import wu.seal.jsontokotlin.utils.getChildType
import wu.seal.jsontokotlin.utils.getRawType

class ClassNameFordSuffixSupport : IKotlinClassInterceptor<KotlinClass> {

    companion object {
        var SUFFIX = ""
    }

    override fun intercept(kotlinClass: KotlinClass): KotlinClass {
        return if (kotlinClass is DataClass) {
            val suffix = SUFFIX
            val standTypes = listOf("Int", "Double", "Long", "String", "Boolean")
            val originName = kotlinClass.name
            val newPropertyTypes =
                kotlinClass.properties.map {
                    val rawSubType = getChildType(getRawType(it.type))
                    when {
                        it.type.isMapType() -> {
                            it.type//currently don't support map type
                        }
                        standTypes.contains(rawSubType) -> it.type
                        else -> it.type.replace(rawSubType, rawSubType + suffix)
                    }
                }

            val newPropertyDefaultValues = kotlinClass.properties.map {
                val rawSubType = getChildType(getRawType(it.type))
                when {
                    it.value.isEmpty() -> it.value
                    it.type.isMapType() -> {
                        it.value//currently don't support map type
                    }
                    standTypes.contains(rawSubType) -> it.value
                    else -> it.value.replace(rawSubType, rawSubType + suffix)
                }
            }

            val newProperties = kotlinClass.properties.mapIndexed { index, property ->

                val newType = newPropertyTypes[index]

                val newValue = newPropertyDefaultValues[index]

                property.copy(type = newType, value = newValue)
            }
            kotlinClass.copy(name = originName + suffix, properties = newProperties)

        } else {
            kotlinClass
        }
    }

    private fun String.isMapType(): Boolean {

        return matches(Regex("Map<.+,.+>"))
    }
}