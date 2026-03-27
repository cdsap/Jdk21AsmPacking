package com.awesomeapp.module_0_10

data class GenModel3112(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3112 {
    fun process(model: GenModel3112): GenModel3112
    fun validate(model: GenModel3112): Boolean
}

class GenServiceImpl3112 : GenService3112 {
    override fun process(model: GenModel3112): GenModel3112 = model.copy(active = true)
    override fun validate(model: GenModel3112): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3112 {
    data class Success(val data: GenModel3112) : GenResult3112()
    data class Error(val message: String) : GenResult3112()
    data object Loading : GenResult3112()
}
