package com.xfinity.resourceprovider

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.StringTokenizer
import com.android.build.gradle.AppExtension
import org.gradle.api.tasks.SourceTask

class ResourceProviderPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<ResourceProviderPluginExtension>(RP_PLUGIN_NAME, ResourceProviderPluginExtension::class.java)
        project.afterEvaluate {
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            appExtension?.applicationVariants?.all { variant ->
                if (extension.packageName == null) {
                    extension.packageName = variant.applicationId
                }
                
                val processResourcesTask = project.tasks.getByName("process${variant.name.capitalize()}Resources")
                val rpTask = it.task("generate${variant.name.capitalize()}ResourceProvider") {
                    it.doLast {
                        generateResourceProviderForVariant(project, extension, variant.name.capitalize())
                    }
                }.dependsOn(processResourcesTask)

                val outputDir = "${project.buildDir}/generated/source/resourceprovider/${variant.name}"
                variant.registerJavaGeneratingTask(rpTask, File(outputDir))
                val kotlinCompileTask = it.tasks.findByName("compile${variant.name.capitalize()}Kotlin") as? SourceTask
                if (kotlinCompileTask != null) {
                    kotlinCompileTask.dependsOn(rpTask)
                    val srcSet = it.objects.sourceDirectorySet("resourceprovider", "resourceprovider").srcDir(outputDir)
                    kotlinCompileTask.source(srcSet)
                }
            }
        }
    }

    private fun generateResourceProviderForVariant(project: Project, extension: ResourceProviderPluginExtension, variantName: String) {
        val rClassDir = File(project.buildDir.toString() + "/intermediates/compile_and_runtime_not_namespaced_r_class_jar/$variantName/")
        project.exec {
            it.workingDir = rClassDir
            it.executable = "unzip"
            it.args = listOf("R.jar")
            it.isIgnoreExitValue = true
        }

        project.exec {
            it.workingDir = rClassDir
            it.commandLine = listOf("bash", "-c", "find ${rClassDir.absolutePath} -name '*.class' | xargs javap -p > rclass.txt")
        }

        extension.packageName?.let {
            val directives = RpDirectives(extension.generateStringProvider, extension.generateDrawableProvider,
                    extension.generateIntegerProvider, extension.generateDimenProvider,
                    extension.generateColorProvider, extension.generateIdProvider)
            val resourceProviderFactory = ResourceProviderFactory()
            val outputDir = "${project.buildDir}/generated/source/resourceprovider/$variantName"

            try {
                Files.createDirectories(Paths.get(outputDir))
                System.out.println("Created directory $outputDir successfully")
            } catch (e: Exception) {
                System.out.println("Creating directory $outputDir failed with ${e.message}")
            }
            resourceProviderFactory.buildResourceProvider(it, variantName, project.buildDir.toString(), outputDir,
                    directives)
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
}

class ResourceProviderFactory {
    fun buildResourceProvider(packageName: String, variantName: String, buildDirectory: String, outputDirectory: String,
                              directives: RpDirectives) {
        val rpCodeGenerator = RpCodeGenerator(packageName, parseRClassInfoFile(buildDirectory, variantName), outputDirectory)
        rpCodeGenerator.generateCode(directives)
    }

    private fun parseRClassInfoFile(buildDirectory: String, variantName: String): RClassInfo {
        val rClassInfo = File("$buildDirectory/intermediates/compile_and_runtime_not_namespaced_r_class_jar/${variantName}/rclass.txt").readText()
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

//        println("Strings vars: ${rClassStringVars.toString()}")
        return RClassInfo(rClassStringVars, rClassPluralVars, rClassDrawableVars, rClassDimenVars, rClassIntegerVars, rClassColorVars, rClassIdVars)
    }

    private fun parseClass(classString: String, varsList: MutableList<String>) {
        val varTokenizer = StringTokenizer(classString, ";")
        val rawVarsList = mutableListOf<String>()
        while (varTokenizer.hasMoreTokens()) {
            val varToken = varTokenizer.nextToken()
            val varName = varToken.substringAfter(VAR_PREFIX, MISSING).trim(';')
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
        const val VAR_PREFIX = "public static final int "
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
                        var generatePluralProvider: Boolean = true,
                        var generateDrawableProvider: Boolean = true,
                        var generateIntegerProvider: Boolean = true,
                        var generateDimenProvider: Boolean = true,
                        var generateColorProvider: Boolean = true,
                        var generateIdProvider: Boolean = true)