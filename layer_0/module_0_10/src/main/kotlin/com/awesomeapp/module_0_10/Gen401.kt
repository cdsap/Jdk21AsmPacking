package com.awesomeapp.module_0_10

data class GenModel401(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService401 {
    fun process(model: GenModel401): GenModel401
    fun validate(model: GenModel401): Boolean
}

class GenServiceImpl401 : GenService401 {
    override fun process(model: GenModel401): GenModel401 = model.copy(active = true)
    override fun validate(model: GenModel401): Boolean = model.name.isNotEmpty()
}

sealed class GenResult401 {
    data class Success(val data: GenModel401) : GenResult401()
    data class Error(val message: String) : GenResult401()
    data object Loading : GenResult401()
}
