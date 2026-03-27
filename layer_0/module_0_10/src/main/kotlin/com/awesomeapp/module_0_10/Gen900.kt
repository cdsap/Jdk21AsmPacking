package com.awesomeapp.module_0_10

data class GenModel900(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService900 {
    fun process(model: GenModel900): GenModel900
    fun validate(model: GenModel900): Boolean
}

class GenServiceImpl900 : GenService900 {
    override fun process(model: GenModel900): GenModel900 = model.copy(active = true)
    override fun validate(model: GenModel900): Boolean = model.name.isNotEmpty()
}

sealed class GenResult900 {
    data class Success(val data: GenModel900) : GenResult900()
    data class Error(val message: String) : GenResult900()
    data object Loading : GenResult900()
}
