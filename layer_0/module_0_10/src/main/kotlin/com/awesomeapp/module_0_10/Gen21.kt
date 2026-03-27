package com.awesomeapp.module_0_10

data class GenModel21(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService21 {
    fun process(model: GenModel21): GenModel21
    fun validate(model: GenModel21): Boolean
}

class GenServiceImpl21 : GenService21 {
    override fun process(model: GenModel21): GenModel21 = model.copy(active = true)
    override fun validate(model: GenModel21): Boolean = model.name.isNotEmpty()
}

sealed class GenResult21 {
    data class Success(val data: GenModel21) : GenResult21()
    data class Error(val message: String) : GenResult21()
    data object Loading : GenResult21()
}
