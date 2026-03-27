package com.awesomeapp.module_0_10

data class GenModel4120(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4120 {
    fun process(model: GenModel4120): GenModel4120
    fun validate(model: GenModel4120): Boolean
}

class GenServiceImpl4120 : GenService4120 {
    override fun process(model: GenModel4120): GenModel4120 = model.copy(active = true)
    override fun validate(model: GenModel4120): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4120 {
    data class Success(val data: GenModel4120) : GenResult4120()
    data class Error(val message: String) : GenResult4120()
    data object Loading : GenResult4120()
}
