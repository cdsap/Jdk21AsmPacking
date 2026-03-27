package com.awesomeapp.module_0_10

data class GenModel3402(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3402 {
    fun process(model: GenModel3402): GenModel3402
    fun validate(model: GenModel3402): Boolean
}

class GenServiceImpl3402 : GenService3402 {
    override fun process(model: GenModel3402): GenModel3402 = model.copy(active = true)
    override fun validate(model: GenModel3402): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3402 {
    data class Success(val data: GenModel3402) : GenResult3402()
    data class Error(val message: String) : GenResult3402()
    data object Loading : GenResult3402()
}
