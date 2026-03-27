package com.awesomeapp.module_0_10

data class GenModel170(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService170 {
    fun process(model: GenModel170): GenModel170
    fun validate(model: GenModel170): Boolean
}

class GenServiceImpl170 : GenService170 {
    override fun process(model: GenModel170): GenModel170 = model.copy(active = true)
    override fun validate(model: GenModel170): Boolean = model.name.isNotEmpty()
}

sealed class GenResult170 {
    data class Success(val data: GenModel170) : GenResult170()
    data class Error(val message: String) : GenResult170()
    data object Loading : GenResult170()
}
