package com.awesomeapp.module_0_10

data class GenModel180(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService180 {
    fun process(model: GenModel180): GenModel180
    fun validate(model: GenModel180): Boolean
}

class GenServiceImpl180 : GenService180 {
    override fun process(model: GenModel180): GenModel180 = model.copy(active = true)
    override fun validate(model: GenModel180): Boolean = model.name.isNotEmpty()
}

sealed class GenResult180 {
    data class Success(val data: GenModel180) : GenResult180()
    data class Error(val message: String) : GenResult180()
    data object Loading : GenResult180()
}
