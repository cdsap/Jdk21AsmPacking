package com.awesomeapp.module_0_10

data class GenModel303(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService303 {
    fun process(model: GenModel303): GenModel303
    fun validate(model: GenModel303): Boolean
}

class GenServiceImpl303 : GenService303 {
    override fun process(model: GenModel303): GenModel303 = model.copy(active = true)
    override fun validate(model: GenModel303): Boolean = model.name.isNotEmpty()
}

sealed class GenResult303 {
    data class Success(val data: GenModel303) : GenResult303()
    data class Error(val message: String) : GenResult303()
    data object Loading : GenResult303()
}
