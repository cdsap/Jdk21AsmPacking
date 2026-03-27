package com.awesomeapp.module_0_10

data class GenModel509(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService509 {
    fun process(model: GenModel509): GenModel509
    fun validate(model: GenModel509): Boolean
}

class GenServiceImpl509 : GenService509 {
    override fun process(model: GenModel509): GenModel509 = model.copy(active = true)
    override fun validate(model: GenModel509): Boolean = model.name.isNotEmpty()
}

sealed class GenResult509 {
    data class Success(val data: GenModel509) : GenResult509()
    data class Error(val message: String) : GenResult509()
    data object Loading : GenResult509()
}
