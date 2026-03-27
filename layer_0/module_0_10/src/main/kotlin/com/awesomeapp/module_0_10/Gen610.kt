package com.awesomeapp.module_0_10

data class GenModel610(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService610 {
    fun process(model: GenModel610): GenModel610
    fun validate(model: GenModel610): Boolean
}

class GenServiceImpl610 : GenService610 {
    override fun process(model: GenModel610): GenModel610 = model.copy(active = true)
    override fun validate(model: GenModel610): Boolean = model.name.isNotEmpty()
}

sealed class GenResult610 {
    data class Success(val data: GenModel610) : GenResult610()
    data class Error(val message: String) : GenResult610()
    data object Loading : GenResult610()
}
