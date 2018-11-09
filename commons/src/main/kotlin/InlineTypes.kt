/**
 * Unique identifier for any user
 */
inline class User(val value: String) {
    val sam get() = Sam(value)
}

/**
 * Ldap type; general identifier for a user.
 * Unique per user, but must be converted before used as a key
 */
inline class Sam(val value: String)

/**
 * Unique id for various models
 */
inline class Id(val value: String)

/**
 * Date, backed by iso format
 */
inline class DateTime(val date: String)

/**
 * Unique flag per category.
 * This is intentionally not an enum because implementations
 * should be able to have their own flags.
 * Flags are almost always associated with a dictionary storage,
 * so flags may not always associate to existing data.
 *
 * TODO check if necessary; effectively same as [Id]
 */
inline class Flag(val flag: String)