package com.awesomeapp.module_0_10

data class GenModel11(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService11 {
    fun process(model: GenModel11): GenModel11
    fun validate(model: GenModel11): Boolean
}

class GenServiceImpl11 : GenService11 {
    override fun process(model: GenModel11): GenModel11 = model.copy(active = true)
    override fun validate(model: GenModel11): Boolean = model.name.isNotEmpty()
}

sealed class GenResult11 {
    data class Success(val data: GenModel11) : GenResult11()
    data class Error(val message: String) : GenResult11()
    data object Loading : GenResult11()
}
