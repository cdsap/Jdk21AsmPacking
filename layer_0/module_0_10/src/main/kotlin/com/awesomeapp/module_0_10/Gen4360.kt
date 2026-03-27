package com.awesomeapp.module_0_10

data class GenModel4360(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4360 {
    fun process(model: GenModel4360): GenModel4360
    fun validate(model: GenModel4360): Boolean
}

class GenServiceImpl4360 : GenService4360 {
    override fun process(model: GenModel4360): GenModel4360 = model.copy(active = true)
    override fun validate(model: GenModel4360): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4360 {
    data class Success(val data: GenModel4360) : GenResult4360()
    data class Error(val message: String) : GenResult4360()
    data object Loading : GenResult4360()
}
