package com.awesomeapp.module_0_10

data class GenModel4170(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4170 {
    fun process(model: GenModel4170): GenModel4170
    fun validate(model: GenModel4170): Boolean
}

class GenServiceImpl4170 : GenService4170 {
    override fun process(model: GenModel4170): GenModel4170 = model.copy(active = true)
    override fun validate(model: GenModel4170): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4170 {
    data class Success(val data: GenModel4170) : GenResult4170()
    data class Error(val message: String) : GenResult4170()
    data object Loading : GenResult4170()
}
