package com.awesomeapp.module_0_10

data class GenModel508(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService508 {
    fun process(model: GenModel508): GenModel508
    fun validate(model: GenModel508): Boolean
}

class GenServiceImpl508 : GenService508 {
    override fun process(model: GenModel508): GenModel508 = model.copy(active = true)
    override fun validate(model: GenModel508): Boolean = model.name.isNotEmpty()
}

sealed class GenResult508 {
    data class Success(val data: GenModel508) : GenResult508()
    data class Error(val message: String) : GenResult508()
    data object Loading : GenResult508()
}
