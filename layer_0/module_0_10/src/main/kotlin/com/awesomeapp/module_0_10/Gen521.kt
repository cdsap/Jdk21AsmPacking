package com.awesomeapp.module_0_10

data class GenModel521(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService521 {
    fun process(model: GenModel521): GenModel521
    fun validate(model: GenModel521): Boolean
}

class GenServiceImpl521 : GenService521 {
    override fun process(model: GenModel521): GenModel521 = model.copy(active = true)
    override fun validate(model: GenModel521): Boolean = model.name.isNotEmpty()
}

sealed class GenResult521 {
    data class Success(val data: GenModel521) : GenResult521()
    data class Error(val message: String) : GenResult521()
    data object Loading : GenResult521()
}
