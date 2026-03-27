package com.awesomeapp.module_0_10

data class GenModel4111(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4111 {
    fun process(model: GenModel4111): GenModel4111
    fun validate(model: GenModel4111): Boolean
}

class GenServiceImpl4111 : GenService4111 {
    override fun process(model: GenModel4111): GenModel4111 = model.copy(active = true)
    override fun validate(model: GenModel4111): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4111 {
    data class Success(val data: GenModel4111) : GenResult4111()
    data class Error(val message: String) : GenResult4111()
    data object Loading : GenResult4111()
}
