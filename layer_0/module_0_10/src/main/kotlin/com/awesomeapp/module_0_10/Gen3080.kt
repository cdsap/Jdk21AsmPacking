package com.awesomeapp.module_0_10

data class GenModel3080(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3080 {
    fun process(model: GenModel3080): GenModel3080
    fun validate(model: GenModel3080): Boolean
}

class GenServiceImpl3080 : GenService3080 {
    override fun process(model: GenModel3080): GenModel3080 = model.copy(active = true)
    override fun validate(model: GenModel3080): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3080 {
    data class Success(val data: GenModel3080) : GenResult3080()
    data class Error(val message: String) : GenResult3080()
    data object Loading : GenResult3080()
}
