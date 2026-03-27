package com.awesomeapp.module_0_10

data class GenModel128(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService128 {
    fun process(model: GenModel128): GenModel128
    fun validate(model: GenModel128): Boolean
}

class GenServiceImpl128 : GenService128 {
    override fun process(model: GenModel128): GenModel128 = model.copy(active = true)
    override fun validate(model: GenModel128): Boolean = model.name.isNotEmpty()
}

sealed class GenResult128 {
    data class Success(val data: GenModel128) : GenResult128()
    data class Error(val message: String) : GenResult128()
    data object Loading : GenResult128()
}
