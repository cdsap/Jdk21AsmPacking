package com.awesomeapp.module_0_10

data class GenModel280(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService280 {
    fun process(model: GenModel280): GenModel280
    fun validate(model: GenModel280): Boolean
}

class GenServiceImpl280 : GenService280 {
    override fun process(model: GenModel280): GenModel280 = model.copy(active = true)
    override fun validate(model: GenModel280): Boolean = model.name.isNotEmpty()
}

sealed class GenResult280 {
    data class Success(val data: GenModel280) : GenResult280()
    data class Error(val message: String) : GenResult280()
    data object Loading : GenResult280()
}
