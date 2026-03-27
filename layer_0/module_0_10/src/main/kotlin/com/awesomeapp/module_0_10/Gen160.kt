package com.awesomeapp.module_0_10

data class GenModel160(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService160 {
    fun process(model: GenModel160): GenModel160
    fun validate(model: GenModel160): Boolean
}

class GenServiceImpl160 : GenService160 {
    override fun process(model: GenModel160): GenModel160 = model.copy(active = true)
    override fun validate(model: GenModel160): Boolean = model.name.isNotEmpty()
}

sealed class GenResult160 {
    data class Success(val data: GenModel160) : GenResult160()
    data class Error(val message: String) : GenResult160()
    data object Loading : GenResult160()
}
