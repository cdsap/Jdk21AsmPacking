package com.awesomeapp.module_0_10

data class GenModel3020(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3020 {
    fun process(model: GenModel3020): GenModel3020
    fun validate(model: GenModel3020): Boolean
}

class GenServiceImpl3020 : GenService3020 {
    override fun process(model: GenModel3020): GenModel3020 = model.copy(active = true)
    override fun validate(model: GenModel3020): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3020 {
    data class Success(val data: GenModel3020) : GenResult3020()
    data class Error(val message: String) : GenResult3020()
    data object Loading : GenResult3020()
}
