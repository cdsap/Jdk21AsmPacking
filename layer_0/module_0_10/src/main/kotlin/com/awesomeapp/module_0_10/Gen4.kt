package com.awesomeapp.module_0_10

data class GenModel4(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4 {
    fun process(model: GenModel4): GenModel4
    fun validate(model: GenModel4): Boolean
}

class GenServiceImpl4 : GenService4 {
    override fun process(model: GenModel4): GenModel4 = model.copy(active = true)
    override fun validate(model: GenModel4): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4 {
    data class Success(val data: GenModel4) : GenResult4()
    data class Error(val message: String) : GenResult4()
    data object Loading : GenResult4()
}
