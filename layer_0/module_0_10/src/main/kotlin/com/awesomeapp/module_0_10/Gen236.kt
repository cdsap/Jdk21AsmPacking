package com.awesomeapp.module_0_10

data class GenModel236(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService236 {
    fun process(model: GenModel236): GenModel236
    fun validate(model: GenModel236): Boolean
}

class GenServiceImpl236 : GenService236 {
    override fun process(model: GenModel236): GenModel236 = model.copy(active = true)
    override fun validate(model: GenModel236): Boolean = model.name.isNotEmpty()
}

sealed class GenResult236 {
    data class Success(val data: GenModel236) : GenResult236()
    data class Error(val message: String) : GenResult236()
    data object Loading : GenResult236()
}
