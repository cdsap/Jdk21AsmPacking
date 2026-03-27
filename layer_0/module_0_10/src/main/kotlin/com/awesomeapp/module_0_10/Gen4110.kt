package com.awesomeapp.module_0_10

data class GenModel4110(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4110 {
    fun process(model: GenModel4110): GenModel4110
    fun validate(model: GenModel4110): Boolean
}

class GenServiceImpl4110 : GenService4110 {
    override fun process(model: GenModel4110): GenModel4110 = model.copy(active = true)
    override fun validate(model: GenModel4110): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4110 {
    data class Success(val data: GenModel4110) : GenResult4110()
    data class Error(val message: String) : GenResult4110()
    data object Loading : GenResult4110()
}
