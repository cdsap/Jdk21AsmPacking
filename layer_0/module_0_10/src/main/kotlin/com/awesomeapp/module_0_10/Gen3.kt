package com.awesomeapp.module_0_10

data class GenModel3(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3 {
    fun process(model: GenModel3): GenModel3
    fun validate(model: GenModel3): Boolean
}

class GenServiceImpl3 : GenService3 {
    override fun process(model: GenModel3): GenModel3 = model.copy(active = true)
    override fun validate(model: GenModel3): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3 {
    data class Success(val data: GenModel3) : GenResult3()
    data class Error(val message: String) : GenResult3()
    data object Loading : GenResult3()
}
