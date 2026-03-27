package com.awesomeapp.module_0_10

data class GenModel580(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService580 {
    fun process(model: GenModel580): GenModel580
    fun validate(model: GenModel580): Boolean
}

class GenServiceImpl580 : GenService580 {
    override fun process(model: GenModel580): GenModel580 = model.copy(active = true)
    override fun validate(model: GenModel580): Boolean = model.name.isNotEmpty()
}

sealed class GenResult580 {
    data class Success(val data: GenModel580) : GenResult580()
    data class Error(val message: String) : GenResult580()
    data object Loading : GenResult580()
}
