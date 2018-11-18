package ca.allanwang.prim.models

/**
 * Unique identifier for any user
 */
typealias User = String

typealias Role = String

/**
 * Human readable string.
 * Typically unique per category.
 */
typealias Name = String

/**
 * Unique id for various models
 */
typealias Id = String

/**
 * Unique flag per category.
 * This is intentionally not an enum because implementations
 * should be able to have their own flags.
 * Flags are almost always associated with a dictionary storage,
 * so flags may not always associate to existing data.
 *
 * TODO check if necessary; effectively same as [Id]
 */
typealias Flag = String