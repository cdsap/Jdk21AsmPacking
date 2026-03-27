package com.awesomeapp.module_0_10

data class GenModel14(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService14 {
    fun process(model: GenModel14): GenModel14
    fun validate(model: GenModel14): Boolean
}

class GenServiceImpl14 : GenService14 {
    override fun process(model: GenModel14): GenModel14 = model.copy(active = true)
    override fun validate(model: GenModel14): Boolean = model.name.isNotEmpty()
}

sealed class GenResult14 {
    data class Success(val data: GenModel14) : GenResult14()
    data class Error(val message: String) : GenResult14()
    data object Loading : GenResult14()
}
