package com.awesomeapp.module_0_10

data class GenModel4600(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4600 {
    fun process(model: GenModel4600): GenModel4600
    fun validate(model: GenModel4600): Boolean
}

class GenServiceImpl4600 : GenService4600 {
    override fun process(model: GenModel4600): GenModel4600 = model.copy(active = true)
    override fun validate(model: GenModel4600): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4600 {
    data class Success(val data: GenModel4600) : GenResult4600()
    data class Error(val message: String) : GenResult4600()
    data object Loading : GenResult4600()
}
