package com.xfinity.resourceprovider

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import java.io.File

class RpKtCodeGenerator {
    fun generateTestUtils(receiverPackageName: String, directives: RpDirectives, outputDirectoryName: String) {
        val resourceProviderClassName = ClassName(receiverPackageName, "ResourceProvider")
        val stringProviderClassName = ClassName(receiverPackageName, "StringProvider")
        val stringProviderAnswerClassName = ClassName("com.xfinity.resourceprovider.testutils", "StringProviderAnswer")
        val wheneverMemberName = MemberName("com.nhaarman.mockitokotlin2", "whenever")
        val mockMemberName = MemberName("org.mockito.Mockito", "mock")

        val mockStringsFunSpec = FunSpec.builder("mockStrings")
                .receiver(resourceProviderClassName)
                .addStatement("%M(this.strings).thenReturn(" +
                        "%M(%T::class.java, " +
                        "%T()))", wheneverMemberName, mockMemberName, stringProviderClassName, stringProviderAnswerClassName).build()

        val drawableProviderClassName = ClassName(receiverPackageName, "DrawableProvider")
        val drawableProviderAnswerClassName: ClassName = ClassName("com.xfinity.resourceprovider.testutils", "DrawableProviderAnswer")
        val mockDrawablesFunSpec = FunSpec.builder("mockDrawables")
                .receiver(resourceProviderClassName)
                .addStatement(
                        "%M(this.drawables).thenReturn(" +
                                "%M(%T::class.java, " +
                                "%T()))", wheneverMemberName, mockMemberName, drawableProviderClassName, drawableProviderAnswerClassName).build()

        val colorProviderClassName = ClassName(receiverPackageName, "ColorProvider")
        val integerProviderAnswerClassName: ClassName = ClassName("com.xfinity.resourceprovider.testutils", "IntegerProviderAnswer")
        val mockColorsFunSpec = FunSpec.builder("mockColors")
                .receiver(resourceProviderClassName)
                .addStatement(
                        "%M(this.colors).thenReturn(" +
                                "%M(%T::class.java, " +
                                "%T()))", wheneverMemberName, mockMemberName, colorProviderClassName, integerProviderAnswerClassName).build()

        val dimensProviderClassName = ClassName(receiverPackageName, "DimensionProvider")
        val mockDimensFunSpec = FunSpec.builder("mockDimens")
                .receiver(resourceProviderClassName)
                .addStatement(
                        "%M(this.dimens).thenReturn(" +
                                "%M(%T::class.java, " +
                                "%T()))", wheneverMemberName, mockMemberName, dimensProviderClassName, integerProviderAnswerClassName).build()

        val integerProviderClassName = ClassName(receiverPackageName, "IntegerProvider")
        val mockIntegersFunSpec = FunSpec.builder("mockIntegers")
                .receiver(resourceProviderClassName)
                .addStatement(
                        "%M(this.integers).thenReturn(" +
                                "%M(%T::class.java, " +
                                "%T()))", wheneverMemberName, mockMemberName, integerProviderClassName, integerProviderAnswerClassName)
                .build()

        val idProviderClassName = ClassName(receiverPackageName, "IdProvider")
        val mockIdsFunSpec = FunSpec.builder("mockIds")
                .receiver(resourceProviderClassName)
                .addStatement(
                        "%M(this.ids).thenReturn(" +
                                "%M(%T::class.java, " +
                                "%T()))", wheneverMemberName, mockMemberName, idProviderClassName, integerProviderAnswerClassName)
                .build()

        val mockFunSpecBuilder = FunSpec.builder("mock")
                .receiver(resourceProviderClassName)

        if (directives.generateStringProvider) {
            mockFunSpecBuilder.addStatement("this.mockStrings()")
        }

        if (directives.generateDrawableProvider) {
            mockFunSpecBuilder.addStatement("this.mockDrawables()")
        }

        if (directives.generateColorProvider) {
            mockFunSpecBuilder.addStatement("this.mockColors()")
        }

        if (directives.generateDimenProvider) {
            mockFunSpecBuilder.addStatement("this.mockDimens()")
        }

        if (directives.generateIntegerProvider) {
            mockFunSpecBuilder.addStatement("this.mockIntegers()")
        }

        if (directives.generateIdProvider) {
            mockFunSpecBuilder.addStatement("this.mockIds()")
        }

        val mockFunSpec = mockFunSpecBuilder.build()

        val kotlinFileBuilder = FileSpec.builder("com.xfinity.resourceprovider", "ResourceProviderTestUtils")

        if (directives.generateStringProvider) {
            kotlinFileBuilder.addFunction(mockStringsFunSpec)
        }

        if (directives.generateDrawableProvider) {
            kotlinFileBuilder.addFunction(mockDrawablesFunSpec)
        }

        if (directives.generateColorProvider) {
            kotlinFileBuilder.addFunction(mockColorsFunSpec)
        }

        if (directives.generateDimenProvider) {
            kotlinFileBuilder.addFunction(mockDimensFunSpec)
        }

        if (directives.generateIntegerProvider) {
            kotlinFileBuilder .addFunction(mockIntegersFunSpec)
        }

        if (directives.generateIdProvider) {
            kotlinFileBuilder.addFunction(mockIdsFunSpec)
        }

        kotlinFileBuilder.addFunction(mockFunSpec)
        val kotlinFile = kotlinFileBuilder.build()

        kotlinFile.writeTo(File(outputDirectoryName))
    }
}