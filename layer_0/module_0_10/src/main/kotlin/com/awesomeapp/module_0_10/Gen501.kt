package com.awesomeapp.module_0_10

data class GenModel501(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService501 {
    fun process(model: GenModel501): GenModel501
    fun validate(model: GenModel501): Boolean
}

class GenServiceImpl501 : GenService501 {
    override fun process(model: GenModel501): GenModel501 = model.copy(active = true)
    override fun validate(model: GenModel501): Boolean = model.name.isNotEmpty()
}

sealed class GenResult501 {
    data class Success(val data: GenModel501) : GenResult501()
    data class Error(val message: String) : GenResult501()
    data object Loading : GenResult501()
}
