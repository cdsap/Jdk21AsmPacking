package com.awesomeapp.module_0_10

data class GenModel150(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService150 {
    fun process(model: GenModel150): GenModel150
    fun validate(model: GenModel150): Boolean
}

class GenServiceImpl150 : GenService150 {
    override fun process(model: GenModel150): GenModel150 = model.copy(active = true)
    override fun validate(model: GenModel150): Boolean = model.name.isNotEmpty()
}

sealed class GenResult150 {
    data class Success(val data: GenModel150) : GenResult150()
    data class Error(val message: String) : GenResult150()
    data object Loading : GenResult150()
}
