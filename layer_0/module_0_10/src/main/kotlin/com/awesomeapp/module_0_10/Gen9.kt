package com.awesomeapp.module_0_10

data class GenModel9(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService9 {
    fun process(model: GenModel9): GenModel9
    fun validate(model: GenModel9): Boolean
}

class GenServiceImpl9 : GenService9 {
    override fun process(model: GenModel9): GenModel9 = model.copy(active = true)
    override fun validate(model: GenModel9): Boolean = model.name.isNotEmpty()
}

sealed class GenResult9 {
    data class Success(val data: GenModel9) : GenResult9()
    data class Error(val message: String) : GenResult9()
    data object Loading : GenResult9()
}
