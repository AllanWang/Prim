package ca.allanwang.prim.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class ConfigGenerator : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {
        generateClass()
        return true
    }

    fun generateClass() {
        val fileName = "GeneratedConfigs"
        val configs = TypeSpec.objectBuilder(fileName)
                .addProperty(PropertySpec.builder("creationTime", String::class)
                        .initializer("Hello")
                        .build())
                .build()

        val file = FileSpec.builder("ca.allanwang.prim.generated", fileName)
                .addType(configs)
                .build()


        file.writeTo(System.out)

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}