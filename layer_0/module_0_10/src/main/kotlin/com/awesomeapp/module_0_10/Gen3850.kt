package com.awesomeapp.module_0_10

data class GenModel3850(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3850 {
    fun process(model: GenModel3850): GenModel3850
    fun validate(model: GenModel3850): Boolean
}

class GenServiceImpl3850 : GenService3850 {
    override fun process(model: GenModel3850): GenModel3850 = model.copy(active = true)
    override fun validate(model: GenModel3850): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3850 {
    data class Success(val data: GenModel3850) : GenResult3850()
    data class Error(val message: String) : GenResult3850()
    data object Loading : GenResult3850()
}
