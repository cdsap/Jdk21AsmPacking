package com.awesomeapp.module_0_10

data class GenModel201(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService201 {
    fun process(model: GenModel201): GenModel201
    fun validate(model: GenModel201): Boolean
}

class GenServiceImpl201 : GenService201 {
    override fun process(model: GenModel201): GenModel201 = model.copy(active = true)
    override fun validate(model: GenModel201): Boolean = model.name.isNotEmpty()
}

sealed class GenResult201 {
    data class Success(val data: GenModel201) : GenResult201()
    data class Error(val message: String) : GenResult201()
    data object Loading : GenResult201()
}
