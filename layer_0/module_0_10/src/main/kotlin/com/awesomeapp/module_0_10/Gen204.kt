package com.awesomeapp.module_0_10

data class GenModel204(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService204 {
    fun process(model: GenModel204): GenModel204
    fun validate(model: GenModel204): Boolean
}

class GenServiceImpl204 : GenService204 {
    override fun process(model: GenModel204): GenModel204 = model.copy(active = true)
    override fun validate(model: GenModel204): Boolean = model.name.isNotEmpty()
}

sealed class GenResult204 {
    data class Success(val data: GenModel204) : GenResult204()
    data class Error(val message: String) : GenResult204()
    data object Loading : GenResult204()
}
