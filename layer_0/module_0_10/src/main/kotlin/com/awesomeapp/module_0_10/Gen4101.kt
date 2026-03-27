package com.awesomeapp.module_0_10

data class GenModel4101(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4101 {
    fun process(model: GenModel4101): GenModel4101
    fun validate(model: GenModel4101): Boolean
}

class GenServiceImpl4101 : GenService4101 {
    override fun process(model: GenModel4101): GenModel4101 = model.copy(active = true)
    override fun validate(model: GenModel4101): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4101 {
    data class Success(val data: GenModel4101) : GenResult4101()
    data class Error(val message: String) : GenResult4101()
    data object Loading : GenResult4101()
}
