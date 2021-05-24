package wu.seal.jsontokotlin.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import extensions.lee.leo.ClassNameFordSuffixSupport
import wu.seal.jsontokotlin.filetype.KotlinFileType
import wu.seal.jsontokotlin.model.ConfigManager
import wu.seal.jsontokotlin.model.classscodestruct.*

class KotlinClassFileGenerator {

    private val gson = Gson()

    fun generateSingleKotlinClassFile(
            packageDeclare: String,
            kotlinClass: KotlinClass,
            project: Project?,
            psiFileFactory: PsiFileFactory,
            directory: PsiDirectory
    ) {
        val fileNamesWithoutSuffix = currentDirExistsFileNamesWithoutKTSuffix(directory)
        var kotlinClassForGenerateFile = kotlinClass
        while (fileNamesWithoutSuffix.contains(kotlinClass.name)) {
            kotlinClassForGenerateFile =
                    kotlinClassForGenerateFile.rename(newName = kotlinClassForGenerateFile.name + "X")
        }
        generateKotlinClassFile(
                kotlinClassForGenerateFile.name,
                packageDeclare,
                kotlinClassForGenerateFile.getCode(),
                project,
                psiFileFactory,
                directory
        )
        val notifyMessage = "Kotlin Data Class file generated successful"
        showNotify(notifyMessage, project)
    }

    fun generateSingleKotlinFileWithMultipleClass(
        packageDeclare: String,
        kotlinClass: KotlinClass,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory
    ) {
        val fileNamesWithoutSuffix = currentDirExistsFileNamesWithoutKTSuffix(directory)
        var kotlinClassForGenerateFile = kotlinClass
        while (fileNamesWithoutSuffix.contains(kotlinClass.name)) {
            kotlinClassForGenerateFile =
                kotlinClassForGenerateFile.rename(newName = kotlinClassForGenerateFile.name + "X")
        }

        val existsKotlinFileNames = IgnoreCaseStringSet().also { it.addAll(fileNamesWithoutSuffix) }
        val splitClasses = kotlinClass.resolveNameConflicts(existsKotlinFileNames).getAllModifiableClassesRecursivelyIncludeSelf()

        val classCodeContentSb = StringBuilder()
        splitClasses.forEach { splitDataClass ->
            classCodeContentSb.append(splitDataClass.getOnlyCurrentCode())
            classCodeContentSb.append("\n\n")
        }

        generateKotlinClassFile(
            kotlinClassForGenerateFile.name,
            packageDeclare,
            classCodeContentSb.toString(),
            project,
            psiFileFactory,
            directory
        )
        val notifyMessage = "Kotlin Data Class file generated successful"
        showNotify(notifyMessage, project)
    }

    fun generateFordClasses(
        kotlinClass: KotlinClass,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory
    ) {
        generateFordClasses(kotlinClass, project, psiFileFactory, directory, "repository/response", "Response")
        generateFordClasses(kotlinClass, project, psiFileFactory, directory, "domain", "Entity")
        generateFordClasses(kotlinClass, project, psiFileFactory, directory, "component/models", "DTO")
        val notifyMessage = "Kotlin Data Class file generated successful"
        showNotify(notifyMessage, project)
    }

    private fun generateFordClasses(
        kotlinClass: KotlinClass,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory,
        subDir: String,
        suffix: String
    ) {
        // repository/response/**Response.kt
        // domain/**Entity.kt
        // component/models/**DTO.kt
        val directoryFactory = PsiDirectoryFactory.getInstance(directory.project)
        val splitDir = subDir.split("/")
        var subDirectory = directory
        for (dir in splitDir) {
            var isExist = false
            subDirectory.virtualFile.children.forEach {
                if (it.isDirectory && it.name == dir) {
                    isExist = true
                    subDirectory = directoryFactory.createDirectory(it)
                    return@forEach
                }
            }
            if (!isExist) {
                subDirectory = directoryFactory.createDirectory(subDirectory.virtualFile.createChildDirectory(this, dir))
            }
        }

        val renamedKotlinClass = makeProgressiveName(kotlinClass).applyInterceptor(ClassNameFordSuffixSupport(suffix))
        val packageName = directoryFactory.getQualifiedName(subDirectory, false)
        val packageDeclare = if (packageName.isNotEmpty()) "package $packageName" else ""
        val fileNamesWithoutSuffix = currentDirExistsFileNamesWithoutKTSuffix(subDirectory)
        var kotlinClassForGenerateFile = renamedKotlinClass
        while (fileNamesWithoutSuffix.contains(renamedKotlinClass.name)) {
            kotlinClassForGenerateFile =
                kotlinClassForGenerateFile.rename(newName = kotlinClassForGenerateFile.name + "X")
        }

        val existsKotlinFileNames = IgnoreCaseStringSet().also { it.addAll(fileNamesWithoutSuffix) }

        val splitClasses = renamedKotlinClass.resolveNameConflicts(existsKotlinFileNames).getAllModifiableClassesRecursivelyIncludeSelf()

        val classCodeContentSb = StringBuilder()
        splitClasses.forEach { splitDataClass ->
            classCodeContentSb.append(splitDataClass.getOnlyCurrentCode())
            classCodeContentSb.append("\n\n")
        }
        classCodeContentSb.deleteCharAt(classCodeContentSb.lastIndex)

        generateKotlinClassFile(
            kotlinClassForGenerateFile.name,
            packageDeclare,
            classCodeContentSb.toString(),
            project,
            psiFileFactory,
            subDirectory
        )
    }

    private fun makeProgressiveName(kotlinClass: KotlinClass): KotlinClass {
        return when(kotlinClass) {
            is DataClass -> {
                val newProperties = ArrayList<Property>()
                kotlinClass.properties.forEach {
                    if(!it.typeObject.modifiable) {
                        newProperties.add(it)
                    } else {
                        val newTypeObject = makeProgressiveName(it.typeObject.rename(kotlinClass.name + it.typeObject.name))
                        newProperties.add(it.copy(typeObject = newTypeObject))
                    }
                }
                kotlinClass.copy(properties = newProperties)
            }
            else -> {
                kotlinClass
            }
        }
    }

    fun generateMultipleKotlinClassFiles(
            kotlinClass: KotlinClass,
            packageDeclare: String,
            project: Project?,
            psiFileFactory: PsiFileFactory,
            directory: PsiDirectory
    ) {
        val fileNamesWithoutSuffix = currentDirExistsFileNamesWithoutKTSuffix(directory)
        val existsKotlinFileNames = IgnoreCaseStringSet().also { it.addAll(fileNamesWithoutSuffix) }
        val splitClasses = kotlinClass.resolveNameConflicts(existsKotlinFileNames).getAllModifiableClassesRecursivelyIncludeSelf()
        val renameClassMap = getRenameClassMap(originNames = kotlinClass.getAllModifiableClassesRecursivelyIncludeSelf().map { it.name },
                currentNames = splitClasses.map { it.name })
        splitClasses.forEach { splitDataClass ->
            generateKotlinClassFile(
                    splitDataClass.name,
                    packageDeclare,
                    splitDataClass.getOnlyCurrentCode(),
                    project,
                    psiFileFactory,
                    directory
            )
            val notifyMessage = buildString {
                append("${splitClasses.size} Kotlin Data Class files generated successful")
                if (renameClassMap.isNotEmpty()) {
                    append("\n")
                    append("These class names has been auto renamed to new names:\n ${renameClassMap.map { it.first + " -> " + it.second }.toList()}")
                }
            }
            showNotify(notifyMessage, project)
        }
    }

    private fun currentDirExistsFileNamesWithoutKTSuffix(directory: PsiDirectory): List<String> {
        val kotlinFileSuffix = ".kt"
        return directory.files.filter { it.name.endsWith(kotlinFileSuffix) }
                .map { it.name.dropLast(kotlinFileSuffix.length) }
    }

    private fun getRenameClassMap(originNames: List<String>, currentNames: List<String>): List<Pair<String, String>> {
        if (originNames.size != currentNames.size) {
            throw IllegalArgumentException("two names list must have the same size!")
        }
        val renameMap = mutableListOf<Pair<String, String>>()
        originNames.forEachIndexed { index, originName ->
            if (originName != currentNames[index]) {
                renameMap.add(Pair(originName, currentNames[index]))
            }
        }
        return renameMap
    }

    private fun generateKotlinClassFile(
            fileName: String,
            packageDeclare: String,
            classCodeContent: String,
            project: Project?,
            psiFileFactory: PsiFileFactory,
            directory: PsiDirectory
    ) {
        val kotlinFileContent = buildString {
            if (packageDeclare.isNotEmpty()) {
                append(packageDeclare)
                append("\n\n")
            }
            val importClassDeclaration = ClassImportDeclaration.getImportClassDeclaration()
            if (importClassDeclaration.isNotBlank()) {
                append(importClassDeclaration)
                append("\n\n")
            }
            append(classCodeContent)
        }
        executeCouldRollBackAction(project) {
            val file =
                    psiFileFactory.createFileFromText("${fileName.trim('`')}.kt", KotlinFileType(), kotlinFileContent)
            directory.add(file)
        }
    }

    private fun setConfig(key: String, value: String) {
        val configs = gson.fromJson(ConfigManager.extensionsConfig, JsonObject::class.java) ?: JsonObject()
        configs.addProperty(key, value)
        ConfigManager.extensionsConfig = gson.toJson(configs)
    }

}
