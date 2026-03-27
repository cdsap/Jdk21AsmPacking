package com.awesomeapp.module_0_10

data class GenModel200(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService200 {
    fun process(model: GenModel200): GenModel200
    fun validate(model: GenModel200): Boolean
}

class GenServiceImpl200 : GenService200 {
    override fun process(model: GenModel200): GenModel200 = model.copy(active = true)
    override fun validate(model: GenModel200): Boolean = model.name.isNotEmpty()
}

sealed class GenResult200 {
    data class Success(val data: GenModel200) : GenResult200()
    data class Error(val message: String) : GenResult200()
    data object Loading : GenResult200()
}
