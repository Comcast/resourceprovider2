package com.xfinity.resourceprovider

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.lang.Exception
import java.util.ArrayList
import java.util.Arrays
import javax.lang.model.element.Modifier

class RpCodeGenerator(private val packageName: String, private val rClassInfo: RClassInfo, private val outputDirectoryName: String) {
    private val contextClassName = ClassName.get("android.content", "Context")
    private val contextField = FieldSpec.builder(contextClassName, "context", Modifier.PRIVATE).build()
    private val suppressLint = AnnotationSpec.builder(ClassName.get("android.annotation", "SuppressLint"))
            .addMember("value", "\$L", "{\"StringFormatInvalid\", \"StringFormatMatches\"}")
            .build()
    private val contextCompatClassName: TypeName = ClassName.get("androidx.core.content", "ContextCompat")

    fun generateCode(directives: RpDirectives) {
        val outputDirectory = File(outputDirectoryName)
        try {
            val generateIdProvider = rClassInfo.rClassIdVars.isNotEmpty() && directives.generateIdProvider
            if (generateIdProvider) {
                val idProviderClass: TypeSpec = generateIdProviderClass()
                val idProviderJavaFile = JavaFile.builder(packageName, idProviderClass).build()
                idProviderJavaFile.writeTo(outputDirectory)
            }
            val generateIntegerProvider = rClassInfo.rClassIntegerVars.isNotEmpty() && directives.generateIntegerProvider
            if (generateIntegerProvider) {
                val integerProviderClass: TypeSpec = generateIntegerProviderClass()
                val integerProviderJavaFile = JavaFile.builder(packageName, integerProviderClass).build()
                integerProviderJavaFile.writeTo(outputDirectory)
            }
            val generateDimensionProvider = rClassInfo.rClassDimenVars.isNotEmpty() && directives.generateDimenProvider
            if (generateDimensionProvider) {
                val dimensionProviderClass: TypeSpec = generateDimensionProviderClass()
                val dimensionProviderJavaFile = JavaFile.builder(packageName, dimensionProviderClass).build()
                dimensionProviderJavaFile.writeTo(outputDirectory)
            }
            val generateColorProvider = rClassInfo.rClassColorVars.isNotEmpty() && directives.generateColorProvider
            if (generateColorProvider) {
                val colorProviderClass: TypeSpec = generateColorProviderClass()
                val colorProviderJavaFile = JavaFile.builder(packageName, colorProviderClass).build()
                colorProviderJavaFile.writeTo(outputDirectory)
            }
            val generateDrawableProvider =
                    rClassInfo.rClassDrawableVars.isNotEmpty() && directives.generateDrawableProvider
            if (generateDrawableProvider) {
                val drawableProviderClass: TypeSpec = generateDrawableProviderClass()
                val drawableProviderJavaFile = JavaFile.builder(packageName, drawableProviderClass).build()
                drawableProviderJavaFile.writeTo(outputDirectory)
            }
            val generateStringProvider = rClassInfo.rClassStringVars.isNotEmpty() && directives.generateStringProvider
            if (generateStringProvider) {
                val stringProviderClass: TypeSpec = generateStringProviderClass()
                val stringProviderJavaFile = JavaFile.builder(packageName, stringProviderClass).build()
                stringProviderJavaFile.writeTo(outputDirectory)
            }
            val resourceProviderClass: TypeSpec = generateResourceProviderClass(
                    generateIdProvider,
                    generateIntegerProvider,
                    generateDimensionProvider,
                    generateColorProvider,
                    generateDrawableProvider,
                    generateStringProvider)

            val resourceProviderJavaFile = JavaFile.builder(packageName, resourceProviderClass).build()
            resourceProviderJavaFile.writeTo(outputDirectory)

        } catch (e: Exception) {
            println("\n\n\nException occurred: ${e.message}\n\n\n")
        }
    }

    fun generateResourceProviderClass(generateIdProvider: Boolean,
                                      generateIntegerProvider: Boolean,
                                      generateDimensionProvider: Boolean,
                                      generateColorProvider: Boolean,
                                      generateDrawableProvider: Boolean,
                                      generateStringProvider: Boolean): TypeSpec {
        val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
        if (generateIdProvider) {
            constructorBuilder.addStatement("this.idProvider = new IdProvider(context)")
        }
        if (generateIntegerProvider) {
            constructorBuilder.addStatement("this.integerProvider = new IntegerProvider(context)")
        }
        if (generateDrawableProvider) {
            constructorBuilder.addStatement("this.drawableProvider = new DrawableProvider(context)")
        }
        if (generateColorProvider) {
            constructorBuilder.addStatement("this.colorProvider = new ColorProvider(context)")
        }
        if (generateDimensionProvider) {
            constructorBuilder.addStatement("this.dimenProvider = new DimensionProvider(context)")
        }
        if (generateStringProvider) {
            constructorBuilder.addStatement("this.stringProvider = new StringProvider(context)")
        }
        val constructor = constructorBuilder.build()
        val stringProviderClassName = ClassName.get(packageName, "StringProvider")
        val stringProvider = FieldSpec.builder(stringProviderClassName, "stringProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val drawableProviderClassName = ClassName.get(packageName, "DrawableProvider")
        val drawableProvider = FieldSpec.builder(drawableProviderClassName, "drawableProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val colorProviderClassName = ClassName.get(packageName, "ColorProvider")
        val colorProvider = FieldSpec.builder(colorProviderClassName, "colorProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val dimenProviderClassName = ClassName.get(packageName, "DimensionProvider")
        val dimenProvider = FieldSpec.builder(dimenProviderClassName, "dimenProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val integerProviderClassName = ClassName.get(packageName, "IntegerProvider")
        val integerProvider = FieldSpec.builder(integerProviderClassName, "integerProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val idProviderClassName = ClassName.get(packageName, "IdProvider")
        val idProvider = FieldSpec.builder(idProviderClassName, "idProvider")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        val getStringMethodSpec = MethodSpec.methodBuilder("getStrings")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return stringProvider")
                .returns(stringProviderClassName)
                .build()
        val getColorMethodSpec = MethodSpec.methodBuilder("getColors")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return colorProvider")
                .returns(colorProviderClassName)
                .build()
        val getDrawableMethodSpec = MethodSpec.methodBuilder("getDrawables")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return drawableProvider")
                .returns(drawableProviderClassName)
                .build()
        val getDimenMethodSpec = MethodSpec.methodBuilder("getDimens")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return dimenProvider")
                .returns(dimenProviderClassName)
                .build()
        val getIntegerMethodSpec = MethodSpec.methodBuilder("getIntegers")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return integerProvider")
                .returns(integerProviderClassName)
                .build()
        var getIdMethodSpec: MethodSpec? = null
        if (generateIdProvider) {
            getIdMethodSpec = MethodSpec.methodBuilder("getIds")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return idProvider")
                    .returns(idProviderClassName)
                    .build()
        }
        val classBuilder = TypeSpec.classBuilder("ResourceProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
        if (generateIdProvider) {
            classBuilder.addMethod(getIdMethodSpec)
            classBuilder.addField(idProvider)
        }
        if (generateIntegerProvider) {
            classBuilder.addMethod(getIntegerMethodSpec)
            classBuilder.addField(integerProvider)
        }
        if (generateDrawableProvider) {
            classBuilder.addMethod(getDrawableMethodSpec)
            classBuilder.addField(drawableProvider)
        }
        if (generateColorProvider) {
            classBuilder.addMethod(getColorMethodSpec)
            classBuilder.addField(colorProvider)
        }
        if (generateDimensionProvider) {
            classBuilder.addMethod(getDimenMethodSpec)
            classBuilder.addField(dimenProvider)
        }
        if (generateStringProvider) {
            classBuilder.addMethod(getStringMethodSpec)
            classBuilder.addField(stringProvider)
        }
        return classBuilder.build()
    }

    fun generateStringProviderClass(): TypeSpec {
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("StringProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val stringGetterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassStringVars) {
            val objectVarArgsType = ArrayTypeName.get(Array<Any>::class.java)
            val parameterSpec = ParameterSpec.builder(objectVarArgsType, "formatArgs").build()
            try {
                val getterSuffix = getterSuffix(`var`, stringGetterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get$getterSuffix")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(parameterSpec)
                        .returns(String::class.java)
                        .addStatement("return context.getString(R.string.$`var`, formatArgs)")
                        .varargs(true)
                        .build())
                stringGetterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.string." + `var` + "\n\n")
            }
        }
        val pluralsGetterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassPluralVars) {
            val objectVarArgsType = ArrayTypeName.get(Array<Any>::class.java)
            val formatArgsParameterSpec = ParameterSpec.builder(objectVarArgsType, "formatArgs").build()
            val quantityParameterSpec = ParameterSpec.builder(TypeName.INT, "quantity").build()
            try {
                val getterSuffix = getterSuffix(`var`, pluralsGetterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get" + getterSuffix + "QuantityString")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(quantityParameterSpec)
                        .addParameter(formatArgsParameterSpec)
                        .returns(String::class.java)
                        .addStatement("return context.getResources().getQuantityString(R.plurals.$`var`, quantity, formatArgs)")
                        .varargs(true)
                        .build())
                pluralsGetterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.plurals." + `var` + "\n\n")
            }
        }
        return classBuilder.build()
    }

    fun generateDrawableProviderClass(): TypeSpec {
        val drawableClassName = ClassName.get("android.graphics.drawable", "Drawable")
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("DrawableProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val getterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassDrawableVars) {
            try {
                val getterSuffix = getterSuffix(`var`, getterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get$getterSuffix")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(drawableClassName)
                        .addStatement("return \$T.getDrawable(context, R.drawable.$`var`)", contextCompatClassName)
                        .varargs(false)
                        .build())
                getterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.drawable." + `var` + "\n\n")
            }
        }
        return classBuilder.build()
    }

    fun generateColorProviderClass(): TypeSpec {
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("ColorProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val getterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassColorVars) {
            try {
                val getterSuffix = getterSuffix(`var`, getterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get$getterSuffix")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.INT)
                        .addStatement("return \$T.getColor(context, R.color.$`var`)", contextCompatClassName)
                        .varargs(false)
                        .build())
                getterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.color." + `var` + "\n\n")
            }
        }
        return classBuilder.build()
    }

    fun generateIntegerProviderClass(): TypeSpec {
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("IntegerProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val getterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassIntegerVars) {
            try {
                val getterSuffix = getterSuffix(`var`, getterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get$getterSuffix")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.INT)
                        .addStatement("return context.getResources().getInteger(R.integer.$`var`)")
                        .varargs(false)
                        .build())
                getterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.int." + `var` + "\n\n")
            }
        }
        return classBuilder.build()
    }

    fun generateIdProviderClass(): TypeSpec {
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("IdProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val idInfoList = Arrays.asList(IdInfo("R.id.", "", rClassInfo.rClassIdVars),
                IdInfo("R.string.", "String", rClassInfo.rClassStringVars),
                IdInfo("R.plurals.", "Plural", rClassInfo.rClassPluralVars),
                IdInfo("R.drawable.", "Drawable", rClassInfo.rClassDrawableVars),
                IdInfo("R.dimen.", "Dimen", rClassInfo.rClassDimenVars),
                IdInfo("R.integer.", "Integer", rClassInfo.rClassIntegerVars),
                IdInfo("R.color.", "Color", rClassInfo.rClassColorVars))
        val getterSuffixes: MutableList<String> = ArrayList()
        for (info in idInfoList) {
            for (`var` in info.classVars) {
                try {
                    val getterSuffix = getterSuffix(`var`, getterSuffixes)
                    classBuilder.addMethod(MethodSpec.methodBuilder("get" + getterSuffix + info.resType + "Id")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeName.INT)
                            .addStatement("return " + info.idResPrefix + `var`)
                            .varargs(false)
                            .build())
                    getterSuffixes.add(getterSuffix)
                } catch (e: IllegalArgumentException) {
                    println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for " + info.idResPrefix + `var` + "\n\n")
                }
            }
        }
        return classBuilder.build()
    }

    fun generateDimensionProviderClass(): TypeSpec {
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextClassName, "context")
                .addStatement("this.context = context")
                .build()
        val classBuilder = TypeSpec.classBuilder("DimensionProvider")
                .addModifiers(Modifier.PUBLIC)
                .addField(contextField)
                .addMethod(constructor)
                .addAnnotation(suppressLint)
        val getterSuffixes: MutableList<String> = ArrayList()
        for (`var` in rClassInfo.rClassDimenVars) {
            try {
                val getterSuffix = getterSuffix(`var`, getterSuffixes)
                classBuilder.addMethod(MethodSpec.methodBuilder("get" + getterSuffix + "PixelSize")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.INT)
                        .addStatement("return context.getResources().getDimensionPixelSize(R.dimen.$`var`)")
                        .varargs(false)
                        .addJavadoc("Returns the dimension R.dimen.$`var` in pixels")
                        .build())
                getterSuffixes.add(getterSuffix)
            } catch (e: IllegalArgumentException) {
                println("\n\nResourceProvider Compiler Error: " + e.message + ".\n\nUnable to generate API for R.dimen." + `var` + "\n\n")
            }
        }
        return classBuilder.build()
    }

    private fun getterSuffix(varName: String, getterSuffixes: List<String>): String {
        val getterSuffix = StringBuilder(varName)
        getterSuffix.setCharAt(0, Character.toUpperCase(getterSuffix[0]))
        var i: Int
        while (getterSuffix.indexOf("_").also { i = it } != -1) {
            val old = getterSuffix[i + 1]
            val newChar = Character.toUpperCase(old)
            getterSuffix.setCharAt(i + 1, newChar)
            getterSuffix.deleteCharAt(i)
        }
        val suffix = getterSuffix.toString()
        var adjustedSuffix = suffix
        var count = 1
        while (getterSuffixes.contains(adjustedSuffix)) {
            adjustedSuffix = suffix + count
            count++
        }
        return adjustedSuffix
    }

    internal class IdInfo(var idResPrefix: String, var resType: String, var classVars: List<String>)
}