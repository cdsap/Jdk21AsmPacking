package com.awesomeapp.module_0_10

data class GenModel4020(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4020 {
    fun process(model: GenModel4020): GenModel4020
    fun validate(model: GenModel4020): Boolean
}

class GenServiceImpl4020 : GenService4020 {
    override fun process(model: GenModel4020): GenModel4020 = model.copy(active = true)
    override fun validate(model: GenModel4020): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4020 {
    data class Success(val data: GenModel4020) : GenResult4020()
    data class Error(val message: String) : GenResult4020()
    data object Loading : GenResult4020()
}
