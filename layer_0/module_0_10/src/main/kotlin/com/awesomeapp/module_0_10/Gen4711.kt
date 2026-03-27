package com.awesomeapp.module_0_10

data class GenModel4711(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4711 {
    fun process(model: GenModel4711): GenModel4711
    fun validate(model: GenModel4711): Boolean
}

class GenServiceImpl4711 : GenService4711 {
    override fun process(model: GenModel4711): GenModel4711 = model.copy(active = true)
    override fun validate(model: GenModel4711): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4711 {
    data class Success(val data: GenModel4711) : GenResult4711()
    data class Error(val message: String) : GenResult4711()
    data object Loading : GenResult4711()
}
