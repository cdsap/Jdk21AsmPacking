package com.awesomeapp.module_0_10

data class GenModel115(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService115 {
    fun process(model: GenModel115): GenModel115
    fun validate(model: GenModel115): Boolean
}

class GenServiceImpl115 : GenService115 {
    override fun process(model: GenModel115): GenModel115 = model.copy(active = true)
    override fun validate(model: GenModel115): Boolean = model.name.isNotEmpty()
}

sealed class GenResult115 {
    data class Success(val data: GenModel115) : GenResult115()
    data class Error(val message: String) : GenResult115()
    data object Loading : GenResult115()
}
