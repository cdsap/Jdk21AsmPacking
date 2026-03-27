package com.awesomeapp.module_0_10

data class GenModel409(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService409 {
    fun process(model: GenModel409): GenModel409
    fun validate(model: GenModel409): Boolean
}

class GenServiceImpl409 : GenService409 {
    override fun process(model: GenModel409): GenModel409 = model.copy(active = true)
    override fun validate(model: GenModel409): Boolean = model.name.isNotEmpty()
}

sealed class GenResult409 {
    data class Success(val data: GenModel409) : GenResult409()
    data class Error(val message: String) : GenResult409()
    data object Loading : GenResult409()
}
