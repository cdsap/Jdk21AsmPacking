package com.awesomeapp.module_0_10

data class GenModel304(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService304 {
    fun process(model: GenModel304): GenModel304
    fun validate(model: GenModel304): Boolean
}

class GenServiceImpl304 : GenService304 {
    override fun process(model: GenModel304): GenModel304 = model.copy(active = true)
    override fun validate(model: GenModel304): Boolean = model.name.isNotEmpty()
}

sealed class GenResult304 {
    data class Success(val data: GenModel304) : GenResult304()
    data class Error(val message: String) : GenResult304()
    data object Loading : GenResult304()
}
