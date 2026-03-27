package com.awesomeapp.module_0_10

data class GenModel221(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService221 {
    fun process(model: GenModel221): GenModel221
    fun validate(model: GenModel221): Boolean
}

class GenServiceImpl221 : GenService221 {
    override fun process(model: GenModel221): GenModel221 = model.copy(active = true)
    override fun validate(model: GenModel221): Boolean = model.name.isNotEmpty()
}

sealed class GenResult221 {
    data class Success(val data: GenModel221) : GenResult221()
    data class Error(val message: String) : GenResult221()
    data object Loading : GenResult221()
}
