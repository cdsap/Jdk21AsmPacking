package com.awesomeapp.module_0_10

data class GenModel3111(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3111 {
    fun process(model: GenModel3111): GenModel3111
    fun validate(model: GenModel3111): Boolean
}

class GenServiceImpl3111 : GenService3111 {
    override fun process(model: GenModel3111): GenModel3111 = model.copy(active = true)
    override fun validate(model: GenModel3111): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3111 {
    data class Success(val data: GenModel3111) : GenResult3111()
    data class Error(val message: String) : GenResult3111()
    data object Loading : GenResult3111()
}
