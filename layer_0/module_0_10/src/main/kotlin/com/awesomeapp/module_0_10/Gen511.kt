package com.awesomeapp.module_0_10

data class GenModel511(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService511 {
    fun process(model: GenModel511): GenModel511
    fun validate(model: GenModel511): Boolean
}

class GenServiceImpl511 : GenService511 {
    override fun process(model: GenModel511): GenModel511 = model.copy(active = true)
    override fun validate(model: GenModel511): Boolean = model.name.isNotEmpty()
}

sealed class GenResult511 {
    data class Success(val data: GenModel511) : GenResult511()
    data class Error(val message: String) : GenResult511()
    data object Loading : GenResult511()
}
