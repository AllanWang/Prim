package ca.allanwang.ktor.models

/**
 * Represents a json model that
 * can be converted into a more specific model.
 * These models are independent of all non stdlib dependencies.
 * The new model likely removes impossible states.
 */
interface JsonModel<T> {
    fun specific(): T
}

/**
 * Represents a specific model that
 * can be converted into a json model.
 * The json model contains all possible fields,
 * and can likely be converted back to the specific model
 * after serialization.
 */
interface SpecificModel<T> {
    fun json(): T
}