package com.awesomeapp.module_0_10

data class GenModel3101(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3101 {
    fun process(model: GenModel3101): GenModel3101
    fun validate(model: GenModel3101): Boolean
}

class GenServiceImpl3101 : GenService3101 {
    override fun process(model: GenModel3101): GenModel3101 = model.copy(active = true)
    override fun validate(model: GenModel3101): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3101 {
    data class Success(val data: GenModel3101) : GenResult3101()
    data class Error(val message: String) : GenResult3101()
    data object Loading : GenResult3101()
}
