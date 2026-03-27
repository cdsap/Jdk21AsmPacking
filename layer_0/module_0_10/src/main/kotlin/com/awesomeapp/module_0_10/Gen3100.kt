package com.awesomeapp.module_0_10

data class GenModel3100(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3100 {
    fun process(model: GenModel3100): GenModel3100
    fun validate(model: GenModel3100): Boolean
}

class GenServiceImpl3100 : GenService3100 {
    override fun process(model: GenModel3100): GenModel3100 = model.copy(active = true)
    override fun validate(model: GenModel3100): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3100 {
    data class Success(val data: GenModel3100) : GenResult3100()
    data class Error(val message: String) : GenResult3100()
    data object Loading : GenResult3100()
}
