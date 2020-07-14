package com.xfinity.resourceprovider

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import java.io.File

class RpKtCodeGenerator {
    fun generateTestUtils(receiverPackageName: String, generateIdsMock: Boolean, outputDirectoryName: String) {
        val stringProviderAnswerClassName: ClassName = ClassName("com.xfinity.resourceprovider.testutils", "StringProviderAnswer")
        val mockStringsFunSpec = FunSpec.builder("mockStrings")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.strings).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.StringProvider::class.java, " +
                                "%T()))", stringProviderAnswerClassName).build()

        val drawableProviderAnswerClassName: ClassName = ClassName("com.xfinity.resourceprovider.testutils", "DrawableProviderAnswer")
        val mockDrawablesFunSpec = FunSpec.builder("mockDrawables")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.drawables).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.DrawableProvider::class.java, " +
                                "%T()))", drawableProviderAnswerClassName).build()

        val integerProviderAnswerClassName: ClassName = ClassName("com.xfinity.resourceprovider.testutils", "IntegerProviderAnswer")
        val mockColorsFunSpec = FunSpec.builder("mockColors")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.colors).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.ColorProvider::class.java, " +
                                "%T()))", integerProviderAnswerClassName).build()

        val mockDimensFunSpec = FunSpec.builder("mockDimens")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.dimens).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.DimensionProvider::class.java, " +
                                "%T()))", integerProviderAnswerClassName).build()

        val mockIntegersFunSpec = FunSpec.builder("mockIntegers")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.integers).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.IntegerProvider::class.java, " +
                                "%T()))", integerProviderAnswerClassName)
                .build()

        val mockIdsFunSpec = FunSpec.builder("mockIds")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement(
                        "com.nhaarman.mockito_kotlin.whenever(this.ids).thenReturn(" +
                                "org.mockito.Mockito.mock($receiverPackageName.IdProvider::class.java, " +
                                "%T()))", integerProviderAnswerClassName)
                .build()

        val mockFunSpecBuilder = FunSpec.builder("mock")
                .receiver(ClassName(receiverPackageName, "ResourceProvider"))
                .addStatement("this.mockStrings()")
                .addStatement("this.mockDrawables()")
                .addStatement("this.mockColors()")
                .addStatement("this.mockDimens()")
                .addStatement("this.mockIntegers()")

        if (generateIdsMock) {
            mockFunSpecBuilder.addStatement("this.mockIds()")
        }

        val mockFunSpec = mockFunSpecBuilder.build()

        val kotlinFileBuilder = FileSpec.builder("com.xfinity.resourceprovider", "ResourceProviderTestUtils")
                .addFunction(mockStringsFunSpec)
                .addFunction(mockColorsFunSpec)
                .addFunction(mockDrawablesFunSpec)
                .addFunction(mockDimensFunSpec)
                .addFunction(mockIntegersFunSpec)

        if (generateIdsMock) {
            kotlinFileBuilder.addFunction(mockIdsFunSpec)
        }

        kotlinFileBuilder.addFunction(mockFunSpec)
        val kotlinFile = kotlinFileBuilder.build()

        kotlinFile.writeTo(File(outputDirectoryName))
    }
}