package com.awesomeapp.module_0_10

data class GenModel777(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService777 {
    fun process(model: GenModel777): GenModel777
    fun validate(model: GenModel777): Boolean
}

class GenServiceImpl777 : GenService777 {
    override fun process(model: GenModel777): GenModel777 = model.copy(active = true)
    override fun validate(model: GenModel777): Boolean = model.name.isNotEmpty()
}

sealed class GenResult777 {
    data class Success(val data: GenModel777) : GenResult777()
    data class Error(val message: String) : GenResult777()
    data object Loading : GenResult777()
}
