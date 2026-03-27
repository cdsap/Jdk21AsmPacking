package com.awesomeapp.module_0_10

data class GenModel567(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService567 {
    fun process(model: GenModel567): GenModel567
    fun validate(model: GenModel567): Boolean
}

class GenServiceImpl567 : GenService567 {
    override fun process(model: GenModel567): GenModel567 = model.copy(active = true)
    override fun validate(model: GenModel567): Boolean = model.name.isNotEmpty()
}

sealed class GenResult567 {
    data class Success(val data: GenModel567) : GenResult567()
    data class Error(val message: String) : GenResult567()
    data object Loading : GenResult567()
}
