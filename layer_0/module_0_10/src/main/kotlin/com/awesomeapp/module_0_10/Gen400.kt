package com.awesomeapp.module_0_10

data class GenModel400(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService400 {
    fun process(model: GenModel400): GenModel400
    fun validate(model: GenModel400): Boolean
}

class GenServiceImpl400 : GenService400 {
    override fun process(model: GenModel400): GenModel400 = model.copy(active = true)
    override fun validate(model: GenModel400): Boolean = model.name.isNotEmpty()
}

sealed class GenResult400 {
    data class Success(val data: GenModel400) : GenResult400()
    data class Error(val message: String) : GenResult400()
    data object Loading : GenResult400()
}
