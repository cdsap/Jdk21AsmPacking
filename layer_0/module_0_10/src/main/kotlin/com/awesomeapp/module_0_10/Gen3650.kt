package com.awesomeapp.module_0_10

data class GenModel3650(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3650 {
    fun process(model: GenModel3650): GenModel3650
    fun validate(model: GenModel3650): Boolean
}

class GenServiceImpl3650 : GenService3650 {
    override fun process(model: GenModel3650): GenModel3650 = model.copy(active = true)
    override fun validate(model: GenModel3650): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3650 {
    data class Success(val data: GenModel3650) : GenResult3650()
    data class Error(val message: String) : GenResult3650()
    data object Loading : GenResult3650()
}
