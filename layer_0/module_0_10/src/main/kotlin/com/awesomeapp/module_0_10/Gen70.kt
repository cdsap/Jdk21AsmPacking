package com.awesomeapp.module_0_10

data class GenModel70(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService70 {
    fun process(model: GenModel70): GenModel70
    fun validate(model: GenModel70): Boolean
}

class GenServiceImpl70 : GenService70 {
    override fun process(model: GenModel70): GenModel70 = model.copy(active = true)
    override fun validate(model: GenModel70): Boolean = model.name.isNotEmpty()
}

sealed class GenResult70 {
    data class Success(val data: GenModel70) : GenResult70()
    data class Error(val message: String) : GenResult70()
    data object Loading : GenResult70()
}
