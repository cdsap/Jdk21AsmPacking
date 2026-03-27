package com.awesomeapp.module_0_10

data class GenModel4650(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4650 {
    fun process(model: GenModel4650): GenModel4650
    fun validate(model: GenModel4650): Boolean
}

class GenServiceImpl4650 : GenService4650 {
    override fun process(model: GenModel4650): GenModel4650 = model.copy(active = true)
    override fun validate(model: GenModel4650): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4650 {
    data class Success(val data: GenModel4650) : GenResult4650()
    data class Error(val message: String) : GenResult4650()
    data object Loading : GenResult4650()
}
