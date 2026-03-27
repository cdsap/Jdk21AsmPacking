package com.awesomeapp.module_0_10

data class GenModel271(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService271 {
    fun process(model: GenModel271): GenModel271
    fun validate(model: GenModel271): Boolean
}

class GenServiceImpl271 : GenService271 {
    override fun process(model: GenModel271): GenModel271 = model.copy(active = true)
    override fun validate(model: GenModel271): Boolean = model.name.isNotEmpty()
}

sealed class GenResult271 {
    data class Success(val data: GenModel271) : GenResult271()
    data class Error(val message: String) : GenResult271()
    data object Loading : GenResult271()
}
