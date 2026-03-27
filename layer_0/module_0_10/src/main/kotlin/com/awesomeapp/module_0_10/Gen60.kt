package com.awesomeapp.module_0_10

data class GenModel60(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService60 {
    fun process(model: GenModel60): GenModel60
    fun validate(model: GenModel60): Boolean
}

class GenServiceImpl60 : GenService60 {
    override fun process(model: GenModel60): GenModel60 = model.copy(active = true)
    override fun validate(model: GenModel60): Boolean = model.name.isNotEmpty()
}

sealed class GenResult60 {
    data class Success(val data: GenModel60) : GenResult60()
    data class Error(val message: String) : GenResult60()
    data object Loading : GenResult60()
}
