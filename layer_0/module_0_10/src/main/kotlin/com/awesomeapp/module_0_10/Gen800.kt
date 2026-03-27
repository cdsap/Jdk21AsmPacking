package com.awesomeapp.module_0_10

data class GenModel800(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService800 {
    fun process(model: GenModel800): GenModel800
    fun validate(model: GenModel800): Boolean
}

class GenServiceImpl800 : GenService800 {
    override fun process(model: GenModel800): GenModel800 = model.copy(active = true)
    override fun validate(model: GenModel800): Boolean = model.name.isNotEmpty()
}

sealed class GenResult800 {
    data class Success(val data: GenModel800) : GenResult800()
    data class Error(val message: String) : GenResult800()
    data object Loading : GenResult800()
}
