package com.awesomeapp.module_0_10

data class GenModel4857(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4857 {
    fun process(model: GenModel4857): GenModel4857
    fun validate(model: GenModel4857): Boolean
}

class GenServiceImpl4857 : GenService4857 {
    override fun process(model: GenModel4857): GenModel4857 = model.copy(active = true)
    override fun validate(model: GenModel4857): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4857 {
    data class Success(val data: GenModel4857) : GenResult4857()
    data class Error(val message: String) : GenResult4857()
    data object Loading : GenResult4857()
}
