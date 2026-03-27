package com.awesomeapp.module_0_10

data class GenModel270(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService270 {
    fun process(model: GenModel270): GenModel270
    fun validate(model: GenModel270): Boolean
}

class GenServiceImpl270 : GenService270 {
    override fun process(model: GenModel270): GenModel270 = model.copy(active = true)
    override fun validate(model: GenModel270): Boolean = model.name.isNotEmpty()
}

sealed class GenResult270 {
    data class Success(val data: GenModel270) : GenResult270()
    data class Error(val message: String) : GenResult270()
    data object Loading : GenResult270()
}
