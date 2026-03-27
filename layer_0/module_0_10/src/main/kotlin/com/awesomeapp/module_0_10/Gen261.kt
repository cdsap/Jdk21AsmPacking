package com.awesomeapp.module_0_10

data class GenModel261(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService261 {
    fun process(model: GenModel261): GenModel261
    fun validate(model: GenModel261): Boolean
}

class GenServiceImpl261 : GenService261 {
    override fun process(model: GenModel261): GenModel261 = model.copy(active = true)
    override fun validate(model: GenModel261): Boolean = model.name.isNotEmpty()
}

sealed class GenResult261 {
    data class Success(val data: GenModel261) : GenResult261()
    data class Error(val message: String) : GenResult261()
    data object Loading : GenResult261()
}
