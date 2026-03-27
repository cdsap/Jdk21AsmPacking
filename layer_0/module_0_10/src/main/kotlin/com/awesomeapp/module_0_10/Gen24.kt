package com.awesomeapp.module_0_10

data class GenModel24(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService24 {
    fun process(model: GenModel24): GenModel24
    fun validate(model: GenModel24): Boolean
}

class GenServiceImpl24 : GenService24 {
    override fun process(model: GenModel24): GenModel24 = model.copy(active = true)
    override fun validate(model: GenModel24): Boolean = model.name.isNotEmpty()
}

sealed class GenResult24 {
    data class Success(val data: GenModel24) : GenResult24()
    data class Error(val message: String) : GenResult24()
    data object Loading : GenResult24()
}
