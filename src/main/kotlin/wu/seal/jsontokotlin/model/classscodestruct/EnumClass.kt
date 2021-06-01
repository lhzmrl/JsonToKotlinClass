package wu.seal.jsontokotlin.model.classscodestruct

import wu.seal.jsontokotlin.model.builder.*

/**
 * Created by ted on 2020/3/14 18:14.
 */
data class EnumClass(
        override val name: String,
        val xEnumNames: List<String>?,
        override val generic: KotlinClass,
        override val referencedClasses: List<KotlinClass> = listOf(generic),
        val enum: List<Any>,
        val comments: String = "",
        override val modifiable: Boolean = true
) : ModifiableKotlinClass, NoGenericKotlinClass {

    private val codeBuilder: ICodeBuilder by lazy { CodeBuilderFactory.get(TYPE_ENUM, this) }

    override fun getOnlyCurrentCode(): String {
        return codeBuilder.getOnlyCurrentCode()
    }

    override fun getFactoryCode(target: String, source: String, from: String, to: String): String {
        return codeBuilder.getFactoryCode(target, source, from, to)
    }

    override fun rename(newName: String): KotlinClass = copy(name = newName)

    override fun getCode(): String {
        return codeBuilder.getCode()
    }

    override fun replaceReferencedClasses(replaceRule: Map<KotlinClass, KotlinClass>): EnumClass {
        return if (replaceRule.isEmpty()) this else copy(generic = replaceRule.values.toList()[0], referencedClasses = replaceRule.values.toList())
    }
}