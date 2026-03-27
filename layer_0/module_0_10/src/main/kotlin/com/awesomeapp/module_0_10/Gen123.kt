package com.awesomeapp.module_0_10

data class GenModel123(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService123 {
    fun process(model: GenModel123): GenModel123
    fun validate(model: GenModel123): Boolean
}

class GenServiceImpl123 : GenService123 {
    override fun process(model: GenModel123): GenModel123 = model.copy(active = true)
    override fun validate(model: GenModel123): Boolean = model.name.isNotEmpty()
}

sealed class GenResult123 {
    data class Success(val data: GenModel123) : GenResult123()
    data class Error(val message: String) : GenResult123()
    data object Loading : GenResult123()
}
