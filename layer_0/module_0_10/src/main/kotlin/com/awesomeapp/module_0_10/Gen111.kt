package com.awesomeapp.module_0_10

data class GenModel111(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService111 {
    fun process(model: GenModel111): GenModel111
    fun validate(model: GenModel111): Boolean
}

class GenServiceImpl111 : GenService111 {
    override fun process(model: GenModel111): GenModel111 = model.copy(active = true)
    override fun validate(model: GenModel111): Boolean = model.name.isNotEmpty()
}

sealed class GenResult111 {
    data class Success(val data: GenModel111) : GenResult111()
    data class Error(val message: String) : GenResult111()
    data object Loading : GenResult111()
}
