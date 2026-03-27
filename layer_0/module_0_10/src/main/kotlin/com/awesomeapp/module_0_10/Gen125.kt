package com.awesomeapp.module_0_10

data class GenModel125(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService125 {
    fun process(model: GenModel125): GenModel125
    fun validate(model: GenModel125): Boolean
}

class GenServiceImpl125 : GenService125 {
    override fun process(model: GenModel125): GenModel125 = model.copy(active = true)
    override fun validate(model: GenModel125): Boolean = model.name.isNotEmpty()
}

sealed class GenResult125 {
    data class Success(val data: GenModel125) : GenResult125()
    data class Error(val message: String) : GenResult125()
    data object Loading : GenResult125()
}
