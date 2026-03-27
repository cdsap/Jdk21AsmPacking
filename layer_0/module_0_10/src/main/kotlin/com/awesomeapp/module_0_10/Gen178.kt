package com.awesomeapp.module_0_10

data class GenModel178(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService178 {
    fun process(model: GenModel178): GenModel178
    fun validate(model: GenModel178): Boolean
}

class GenServiceImpl178 : GenService178 {
    override fun process(model: GenModel178): GenModel178 = model.copy(active = true)
    override fun validate(model: GenModel178): Boolean = model.name.isNotEmpty()
}

sealed class GenResult178 {
    data class Success(val data: GenModel178) : GenResult178()
    data class Error(val message: String) : GenResult178()
    data object Loading : GenResult178()
}
