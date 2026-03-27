package com.awesomeapp.module_0_10

data class GenModel4123(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4123 {
    fun process(model: GenModel4123): GenModel4123
    fun validate(model: GenModel4123): Boolean
}

class GenServiceImpl4123 : GenService4123 {
    override fun process(model: GenModel4123): GenModel4123 = model.copy(active = true)
    override fun validate(model: GenModel4123): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4123 {
    data class Success(val data: GenModel4123) : GenResult4123()
    data class Error(val message: String) : GenResult4123()
    data object Loading : GenResult4123()
}
