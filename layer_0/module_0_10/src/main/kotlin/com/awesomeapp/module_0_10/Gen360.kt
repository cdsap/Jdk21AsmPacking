package com.awesomeapp.module_0_10

data class GenModel360(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService360 {
    fun process(model: GenModel360): GenModel360
    fun validate(model: GenModel360): Boolean
}

class GenServiceImpl360 : GenService360 {
    override fun process(model: GenModel360): GenModel360 = model.copy(active = true)
    override fun validate(model: GenModel360): Boolean = model.name.isNotEmpty()
}

sealed class GenResult360 {
    data class Success(val data: GenModel360) : GenResult360()
    data class Error(val message: String) : GenResult360()
    data object Loading : GenResult360()
}
