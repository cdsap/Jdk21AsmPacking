package com.awesomeapp.module_0_10

data class GenModel3000(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3000 {
    fun process(model: GenModel3000): GenModel3000
    fun validate(model: GenModel3000): Boolean
}

class GenServiceImpl3000 : GenService3000 {
    override fun process(model: GenModel3000): GenModel3000 = model.copy(active = true)
    override fun validate(model: GenModel3000): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3000 {
    data class Success(val data: GenModel3000) : GenResult3000()
    data class Error(val message: String) : GenResult3000()
    data object Loading : GenResult3000()
}
