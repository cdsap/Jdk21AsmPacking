package com.awesomeapp.module_0_10

data class GenModel500(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService500 {
    fun process(model: GenModel500): GenModel500
    fun validate(model: GenModel500): Boolean
}

class GenServiceImpl500 : GenService500 {
    override fun process(model: GenModel500): GenModel500 = model.copy(active = true)
    override fun validate(model: GenModel500): Boolean = model.name.isNotEmpty()
}

sealed class GenResult500 {
    data class Success(val data: GenModel500) : GenResult500()
    data class Error(val message: String) : GenResult500()
    data object Loading : GenResult500()
}
