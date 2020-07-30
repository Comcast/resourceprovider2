package com.xfinity.resourceprovider

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.StringTokenizer
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.tasks.SourceTask
import java.io.ByteArrayOutputStream

class ResourceProviderPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<ResourceProviderPluginExtension>(RP_PLUGIN_NAME, ResourceProviderPluginExtension::class.java)
        project.afterEvaluate {
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            appExtension?.applicationVariants?.all { variant ->
                setupTasksForVariant(extension, it, variant)
            }
            addTestSourceSet(project, appExtension)

            val libraryExtension = project.extensions.findByType(LibraryExtension::class.java)
            libraryExtension?.libraryVariants?.all { variant ->
                setupTasksForVariant(extension, it, variant, true)
            }
            addTestSourceSet(project, libraryExtension)
        }
    }

    private fun addTestSourceSet(project: Project, extension: BaseExtension?) {
        val testOutputDir = "${project.buildDir}/generated/test/resourceprovider"
        extension?.sourceSets?.forEach {
            if (it.name == "test") {
                val srcDirs = mutableListOf<File>()
                srcDirs.addAll(it.java.srcDirs)
                srcDirs.add(File(testOutputDir))
                it.java.setSrcDirs(srcDirs)
            }
        }
    }

    private fun setupTasksForVariant(extension: ResourceProviderPluginExtension, project: Project, variant: BaseVariant,
                                     isLibrary: Boolean = false) {
        if (extension.packageName == null) {
            extension.packageName = variant.applicationId
        }

        val processResourcesTask = if (isLibrary) {
            project.tasks.getByName("generate${variant.name.capitalize()}RFile")
        } else {
            project.tasks.getByName("process${variant.name.capitalize()}Resources")
        }

        val rpTask = project.task("generate${variant.name.capitalize()}ResourceProvider") {
            it.doLast {
                generateResourceProviderForVariant(project, extension, variant.name.decapitalize(), isLibrary)
            }
        }.dependsOn(processResourcesTask)


        val variantNamePathComponent = variant.name.decapitalize()
        val outputDir = "${project.buildDir}/generated/source/resourceprovider/${variantNamePathComponent}"
        val testOutputDir = "${project.buildDir}/generated/test/resourceprovider"

        val kotlinUnitTestCompileTask = project.tasks.findByName("compile${variant.name.capitalize()}UnitTestKotlin") as? SourceTask
        val javaUnitTestCompileTask = project.tasks.findByName("compile${variant.name.capitalize()}UnitTestSources") as? SourceTask
        val testUtilsTask = project.task("generate${variant.name.capitalize()}ResourceProviderTestUtils") {
            it.doLast {
                extension.packageName?.let { pName ->
                    RpKtCodeGenerator().generateTestUtils(pName, RpDirectives.fromExtention(extension), testOutputDir)
                }
            }
        }.dependsOn(rpTask)

        kotlinUnitTestCompileTask?.let { compileTask ->
            compileTask.dependsOn(testUtilsTask)
            val srcSet = project.objects.sourceDirectorySet("resourceprovider", "resourceprovider").srcDir(testOutputDir)
            compileTask.source(srcSet)
        }

        javaUnitTestCompileTask?.let { compileTask ->
            compileTask.dependsOn(testUtilsTask)
            val srcSet = project.objects.sourceDirectorySet("resourceprovider", "resourceprovider").srcDir(testOutputDir)
            compileTask.source(srcSet)
        }

        variant.registerJavaGeneratingTask(rpTask, File(outputDir))

        val kotlinCompileTask = project.tasks.findByName("compile${variant.name.capitalize()}Kotlin") as? SourceTask
        if (kotlinCompileTask != null) {
            kotlinCompileTask.dependsOn(rpTask)
            val srcSet = project.objects.sourceDirectorySet("resourceprovider", "resourceprovider").srcDir(outputDir)
            kotlinCompileTask.source(srcSet)
        }
    }

    private fun generateResourceProviderForVariant(project: Project, extension: ResourceProviderPluginExtension,
                                                   variantName: String, isLibrary: Boolean = false) {
        val rClassParentDir = if (isLibrary) "compile_only_not_namespaced_r_class_jar" else
            "compile_and_runtime_not_namespaced_r_class_jar"

        val rClassDir = File(project.buildDir.toString() + "/intermediates/$rClassParentDir/$variantName/")
        project.logger.info("\n\nResourceProvider: Inflating R.jar\n")
        project.exec {
            it.workingDir = rClassDir
            it.executable = "jar"
            it.args = listOf("xf", "R.jar")
            it.isIgnoreExitValue = true
        }

        val rclassTxtFile = File("${rClassDir.absolutePath}/rclass.txt")
        val outputFileWriter = rclassTxtFile.writer()

        var packageResourcesPath = rClassDir.absolutePath
        StringTokenizer(extension.packageName, ".").toList().forEach {
            packageResourcesPath += "/$it"
        }

        val inputDir = if (extension.generateForDependencies) rClassDir else File(packageResourcesPath)

        project.logger.info("\nResourceProvider: Building Resource List\n")
        inputDir.walk().sortedBy { it.isFile }.forEach { file ->
            project.logger.info(file.name)
            project.logger.info("ResourceProvider: Ingesting ${file.absolutePath}\n")
            if (file.name.endsWith(".class")) {
                val outputStream =  ByteArrayOutputStream()
                project.exec {
                    it.workingDir = inputDir
                    it.commandLine = listOf("javap", "-p", file.absolutePath)
                    it.standardOutput = outputStream
                }

                outputFileWriter.write(outputStream.toString())
            }
        }

        outputFileWriter.close()

        extension.packageName?.let {
            val directives = RpDirectives.fromExtention(extension)
            val resourceProviderFactory = ResourceProviderFactory()
            val outputDir = "${project.buildDir}/generated/source/resourceprovider/$variantName"

            try {
                Files.createDirectories(Paths.get(outputDir))
                project.logger.info("ResourceProvider: Created directory $outputDir successfully")
            } catch (e: Exception) {
                project.logger.info("ResourceProvider: Creating directory $outputDir failed with ${e.message}")
            }
            resourceProviderFactory.buildResourceProvider(it, variantName, project.buildDir.toString(), rClassParentDir,
                    outputDir, directives)
        }
    }

    companion object {
        const val RP_PLUGIN_NAME = "resourceprovider"
    }
}

open class ResourceProviderPluginExtension {
    var packageName: String? = null
    var generateStringProvider: Boolean = true
    var generateDrawableProvider: Boolean = true
    var generateIntegerProvider: Boolean = true
    var generateDimenProvider: Boolean = true
    var generateColorProvider: Boolean = true
    var generateIdProvider: Boolean = true
    var generateForDependencies: Boolean = false
}

class ResourceProviderFactory {
    fun buildResourceProvider(packageName: String, variantName: String, buildDirectory: String, parentDir: String,
                              outputDirectory: String, directives: RpDirectives) {
        val rpCodeGenerator = RpCodeGenerator(packageName, parseRClassInfoFile(buildDirectory, parentDir, variantName), outputDirectory)
        rpCodeGenerator.generateCode(directives)
    }

    private fun parseRClassInfoFile(buildDirectory: String, parentDir: String, variantName: String): RClassInfo {
        val rClassInfo = File("$buildDirectory/intermediates/$parentDir/${variantName}/rclass.txt").readText()
        val tokenizer = StringTokenizer(rClassInfo, "$")

        val rClassStringVars = mutableListOf<String>()
        val rClassPluralVars = mutableListOf<String>()
        val rClassDrawableVars = mutableListOf<String>()
        val rClassDimenVars = mutableListOf<String>()
        val rClassIntegerVars = mutableListOf<String>()
        val rClassColorVars = mutableListOf<String>()
        val rClassIdVars = mutableListOf<String>()

        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken();
            if (token.startsWith(STRING_PREFIX)) {
                parseClass(token, rClassStringVars)
            } else if (token.startsWith(PLURAL_PREFIX)) {
                parseClass(token, rClassPluralVars)
            } else if (token.startsWith(DRAWABLE_PREFIX)) {
                parseClass(token, rClassDrawableVars)
            } else if (token.startsWith(DIMEN_PREFIX)) {
                parseClass(token, rClassDimenVars)
            } else if (token.startsWith(INT_PREFIX)) {
                parseClass(token, rClassIntegerVars)
            } else if (token.startsWith(COLOR_PREFIX)) {
                parseClass(token, rClassColorVars)
            } else if (token.startsWith(ID_PREFIX)) {
                parseClass(token, rClassIdVars)
            }
        }

        return RClassInfo(rClassStringVars, rClassPluralVars, rClassDrawableVars, rClassDimenVars, rClassIntegerVars, rClassColorVars, rClassIdVars)
    }

    private fun parseClass(classString: String, varsList: MutableList<String>) {
        val isLibrary = classString.contains(LIB_VAR_PREFIX)
        val varTokenizer = StringTokenizer(classString, ";")
        val rawVarsList = mutableListOf<String>()
        val varPrefix = if (isLibrary) LIB_VAR_PREFIX else APP_VAR_PREFIX

        while (varTokenizer.hasMoreTokens()) {
            val varToken = varTokenizer.nextToken()
            val varName = varToken.substringAfter(varPrefix, MISSING).trim(';')
            if (varName != MISSING) {
                rawVarsList.add(varName)
            }
        }
        varsList.addAll(rawVarsList.distinct())
    }

    companion object {
        const val STRING_PREFIX = "string {"
        const val PLURAL_PREFIX = "plurals {"
        const val DRAWABLE_PREFIX = "drawable {"
        const val DIMEN_PREFIX = "dimen {"
        const val INT_PREFIX = "integer {"
        const val COLOR_PREFIX = "color {"
        const val ID_PREFIX = "id {"
        const val APP_VAR_PREFIX = "public static final int "
        const val LIB_VAR_PREFIX = "public static int "
        const val MISSING = "missing"
    }
}

data class RClassInfo(val rClassStringVars: List<String>,
                      val rClassPluralVars: List<String>,
                      val rClassDrawableVars: List<String>,
                      val rClassDimenVars: List<String>,
                      val rClassIntegerVars: List<String>,
                      val rClassColorVars: List<String>,
                      val rClassIdVars: List<String>)

data class RpDirectives(var generateStringProvider: Boolean = true,
                        var generateDrawableProvider: Boolean = true,
                        var generateIntegerProvider: Boolean = true,
                        var generateDimenProvider: Boolean = true,
                        var generateColorProvider: Boolean = true,
                        var generateIdProvider: Boolean = true) {

    companion object {
        fun fromExtention(extension: ResourceProviderPluginExtension): RpDirectives =
            RpDirectives(extension.generateStringProvider, extension.generateDrawableProvider,
                    extension.generateIntegerProvider, extension.generateDimenProvider,
                    extension.generateColorProvider, extension.generateIdProvider)
    }
}