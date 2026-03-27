package com.awesomeapp.module_0_10

data class GenModel3017(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3017 {
    fun process(model: GenModel3017): GenModel3017
    fun validate(model: GenModel3017): Boolean
}

class GenServiceImpl3017 : GenService3017 {
    override fun process(model: GenModel3017): GenModel3017 = model.copy(active = true)
    override fun validate(model: GenModel3017): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3017 {
    data class Success(val data: GenModel3017) : GenResult3017()
    data class Error(val message: String) : GenResult3017()
    data object Loading : GenResult3017()
}
