package com.awesomeapp.module_0_10

data class GenModel520(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService520 {
    fun process(model: GenModel520): GenModel520
    fun validate(model: GenModel520): Boolean
}

class GenServiceImpl520 : GenService520 {
    override fun process(model: GenModel520): GenModel520 = model.copy(active = true)
    override fun validate(model: GenModel520): Boolean = model.name.isNotEmpty()
}

sealed class GenResult520 {
    data class Success(val data: GenModel520) : GenResult520()
    data class Error(val message: String) : GenResult520()
    data object Loading : GenResult520()
}
