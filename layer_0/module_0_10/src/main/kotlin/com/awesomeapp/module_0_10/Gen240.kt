package com.awesomeapp.module_0_10

data class GenModel240(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService240 {
    fun process(model: GenModel240): GenModel240
    fun validate(model: GenModel240): Boolean
}

class GenServiceImpl240 : GenService240 {
    override fun process(model: GenModel240): GenModel240 = model.copy(active = true)
    override fun validate(model: GenModel240): Boolean = model.name.isNotEmpty()
}

sealed class GenResult240 {
    data class Success(val data: GenModel240) : GenResult240()
    data class Error(val message: String) : GenResult240()
    data object Loading : GenResult240()
}
