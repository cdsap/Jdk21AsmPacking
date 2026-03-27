package com.awesomeapp.module_0_10

data class GenModel650(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService650 {
    fun process(model: GenModel650): GenModel650
    fun validate(model: GenModel650): Boolean
}

class GenServiceImpl650 : GenService650 {
    override fun process(model: GenModel650): GenModel650 = model.copy(active = true)
    override fun validate(model: GenModel650): Boolean = model.name.isNotEmpty()
}

sealed class GenResult650 {
    data class Success(val data: GenModel650) : GenResult650()
    data class Error(val message: String) : GenResult650()
    data object Loading : GenResult650()
}
