package com.awesomeapp.module_0_10

data class GenModel6(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService6 {
    fun process(model: GenModel6): GenModel6
    fun validate(model: GenModel6): Boolean
}

class GenServiceImpl6 : GenService6 {
    override fun process(model: GenModel6): GenModel6 = model.copy(active = true)
    override fun validate(model: GenModel6): Boolean = model.name.isNotEmpty()
}

sealed class GenResult6 {
    data class Success(val data: GenModel6) : GenResult6()
    data class Error(val message: String) : GenResult6()
    data object Loading : GenResult6()
}
