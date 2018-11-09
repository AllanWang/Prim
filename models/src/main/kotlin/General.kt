/**
 * General response for executing requests
 */
data class GeneralResponse(
        val ok: Boolean,
        val id: Id
)

/**
 * General error response
 */
data class ErrorResponse(
        val status: Int,
        val flag: Flag,
        val extras: List<String> = emptyList()
)