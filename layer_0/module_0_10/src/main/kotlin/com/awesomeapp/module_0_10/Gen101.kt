package com.awesomeapp.module_0_10

data class GenModel101(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService101 {
    fun process(model: GenModel101): GenModel101
    fun validate(model: GenModel101): Boolean
}

class GenServiceImpl101 : GenService101 {
    override fun process(model: GenModel101): GenModel101 = model.copy(active = true)
    override fun validate(model: GenModel101): Boolean = model.name.isNotEmpty()
}

sealed class GenResult101 {
    data class Success(val data: GenModel101) : GenResult101()
    data class Error(val message: String) : GenResult101()
    data object Loading : GenResult101()
}
