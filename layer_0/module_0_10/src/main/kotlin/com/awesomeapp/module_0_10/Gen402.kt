package com.awesomeapp.module_0_10

data class GenModel402(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService402 {
    fun process(model: GenModel402): GenModel402
    fun validate(model: GenModel402): Boolean
}

class GenServiceImpl402 : GenService402 {
    override fun process(model: GenModel402): GenModel402 = model.copy(active = true)
    override fun validate(model: GenModel402): Boolean = model.name.isNotEmpty()
}

sealed class GenResult402 {
    data class Success(val data: GenModel402) : GenResult402()
    data class Error(val message: String) : GenResult402()
    data object Loading : GenResult402()
}
