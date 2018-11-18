package ca.allanwang.prim.printer

import java.math.BigInteger
import java.security.SecureRandom

private val random = SecureRandom()

/**
 * Generates a random new id
 */
fun newId() = BigInteger(130, random).toString(32)