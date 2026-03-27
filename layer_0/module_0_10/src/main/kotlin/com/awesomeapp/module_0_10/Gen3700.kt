package com.awesomeapp.module_0_10

data class GenModel3700(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3700 {
    fun process(model: GenModel3700): GenModel3700
    fun validate(model: GenModel3700): Boolean
}

class GenServiceImpl3700 : GenService3700 {
    override fun process(model: GenModel3700): GenModel3700 = model.copy(active = true)
    override fun validate(model: GenModel3700): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3700 {
    data class Success(val data: GenModel3700) : GenResult3700()
    data class Error(val message: String) : GenResult3700()
    data object Loading : GenResult3700()
}
