package com.awesomeapp.module_0_10

data class GenModel31(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService31 {
    fun process(model: GenModel31): GenModel31
    fun validate(model: GenModel31): Boolean
}

class GenServiceImpl31 : GenService31 {
    override fun process(model: GenModel31): GenModel31 = model.copy(active = true)
    override fun validate(model: GenModel31): Boolean = model.name.isNotEmpty()
}

sealed class GenResult31 {
    data class Success(val data: GenModel31) : GenResult31()
    data class Error(val message: String) : GenResult31()
    data object Loading : GenResult31()
}
