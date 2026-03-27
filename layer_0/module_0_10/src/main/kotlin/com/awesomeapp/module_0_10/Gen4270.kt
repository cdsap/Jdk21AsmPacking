package com.awesomeapp.module_0_10

data class GenModel4270(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4270 {
    fun process(model: GenModel4270): GenModel4270
    fun validate(model: GenModel4270): Boolean
}

class GenServiceImpl4270 : GenService4270 {
    override fun process(model: GenModel4270): GenModel4270 = model.copy(active = true)
    override fun validate(model: GenModel4270): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4270 {
    data class Success(val data: GenModel4270) : GenResult4270()
    data class Error(val message: String) : GenResult4270()
    data object Loading : GenResult4270()
}
