package com.awesomeapp.module_0_10

data class GenModel23(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService23 {
    fun process(model: GenModel23): GenModel23
    fun validate(model: GenModel23): Boolean
}

class GenServiceImpl23 : GenService23 {
    override fun process(model: GenModel23): GenModel23 = model.copy(active = true)
    override fun validate(model: GenModel23): Boolean = model.name.isNotEmpty()
}

sealed class GenResult23 {
    data class Success(val data: GenModel23) : GenResult23()
    data class Error(val message: String) : GenResult23()
    data object Loading : GenResult23()
}
