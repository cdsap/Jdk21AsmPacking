package com.awesomeapp.module_0_10

data class GenModel275(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService275 {
    fun process(model: GenModel275): GenModel275
    fun validate(model: GenModel275): Boolean
}

class GenServiceImpl275 : GenService275 {
    override fun process(model: GenModel275): GenModel275 = model.copy(active = true)
    override fun validate(model: GenModel275): Boolean = model.name.isNotEmpty()
}

sealed class GenResult275 {
    data class Success(val data: GenModel275) : GenResult275()
    data class Error(val message: String) : GenResult275()
    data object Loading : GenResult275()
}
