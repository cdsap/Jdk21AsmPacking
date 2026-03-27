package com.awesomeapp.module_0_10

data class GenModel3123(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3123 {
    fun process(model: GenModel3123): GenModel3123
    fun validate(model: GenModel3123): Boolean
}

class GenServiceImpl3123 : GenService3123 {
    override fun process(model: GenModel3123): GenModel3123 = model.copy(active = true)
    override fun validate(model: GenModel3123): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3123 {
    data class Success(val data: GenModel3123) : GenResult3123()
    data class Error(val message: String) : GenResult3123()
    data object Loading : GenResult3123()
}
