package ca.allanwang.prim.models

sealed class Validation

object Valid : Validation()

data class Invalid(val flag: Flag) : Validation()