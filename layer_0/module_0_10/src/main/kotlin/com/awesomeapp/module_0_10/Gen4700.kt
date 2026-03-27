package com.awesomeapp.module_0_10

data class GenModel4700(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4700 {
    fun process(model: GenModel4700): GenModel4700
    fun validate(model: GenModel4700): Boolean
}

class GenServiceImpl4700 : GenService4700 {
    override fun process(model: GenModel4700): GenModel4700 = model.copy(active = true)
    override fun validate(model: GenModel4700): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4700 {
    data class Success(val data: GenModel4700) : GenResult4700()
    data class Error(val message: String) : GenResult4700()
    data object Loading : GenResult4700()
}
