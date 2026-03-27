package com.awesomeapp.module_0_10

data class GenModel406(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService406 {
    fun process(model: GenModel406): GenModel406
    fun validate(model: GenModel406): Boolean
}

class GenServiceImpl406 : GenService406 {
    override fun process(model: GenModel406): GenModel406 = model.copy(active = true)
    override fun validate(model: GenModel406): Boolean = model.name.isNotEmpty()
}

sealed class GenResult406 {
    data class Success(val data: GenModel406) : GenResult406()
    data class Error(val message: String) : GenResult406()
    data object Loading : GenResult406()
}
