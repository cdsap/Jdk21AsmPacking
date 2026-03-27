package com.awesomeapp.module_0_10

data class GenModel100(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService100 {
    fun process(model: GenModel100): GenModel100
    fun validate(model: GenModel100): Boolean
}

class GenServiceImpl100 : GenService100 {
    override fun process(model: GenModel100): GenModel100 = model.copy(active = true)
    override fun validate(model: GenModel100): Boolean = model.name.isNotEmpty()
}

sealed class GenResult100 {
    data class Success(val data: GenModel100) : GenResult100()
    data class Error(val message: String) : GenResult100()
    data object Loading : GenResult100()
}
