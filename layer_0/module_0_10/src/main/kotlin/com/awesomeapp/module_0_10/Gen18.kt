package com.awesomeapp.module_0_10

data class GenModel18(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService18 {
    fun process(model: GenModel18): GenModel18
    fun validate(model: GenModel18): Boolean
}

class GenServiceImpl18 : GenService18 {
    override fun process(model: GenModel18): GenModel18 = model.copy(active = true)
    override fun validate(model: GenModel18): Boolean = model.name.isNotEmpty()
}

sealed class GenResult18 {
    data class Success(val data: GenModel18) : GenResult18()
    data class Error(val message: String) : GenResult18()
    data object Loading : GenResult18()
}
