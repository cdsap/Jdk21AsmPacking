package com.awesomeapp.module_0_10

data class GenModel4321(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4321 {
    fun process(model: GenModel4321): GenModel4321
    fun validate(model: GenModel4321): Boolean
}

class GenServiceImpl4321 : GenService4321 {
    override fun process(model: GenModel4321): GenModel4321 = model.copy(active = true)
    override fun validate(model: GenModel4321): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4321 {
    data class Success(val data: GenModel4321) : GenResult4321()
    data class Error(val message: String) : GenResult4321()
    data object Loading : GenResult4321()
}
