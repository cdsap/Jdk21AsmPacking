package com.awesomeapp.module_0_10

data class GenModel35(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService35 {
    fun process(model: GenModel35): GenModel35
    fun validate(model: GenModel35): Boolean
}

class GenServiceImpl35 : GenService35 {
    override fun process(model: GenModel35): GenModel35 = model.copy(active = true)
    override fun validate(model: GenModel35): Boolean = model.name.isNotEmpty()
}

sealed class GenResult35 {
    data class Success(val data: GenModel35) : GenResult35()
    data class Error(val message: String) : GenResult35()
    data object Loading : GenResult35()
}
