package com.awesomeapp.module_0_10

data class GenModel366(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService366 {
    fun process(model: GenModel366): GenModel366
    fun validate(model: GenModel366): Boolean
}

class GenServiceImpl366 : GenService366 {
    override fun process(model: GenModel366): GenModel366 = model.copy(active = true)
    override fun validate(model: GenModel366): Boolean = model.name.isNotEmpty()
}

sealed class GenResult366 {
    data class Success(val data: GenModel366) : GenResult366()
    data class Error(val message: String) : GenResult366()
    data object Loading : GenResult366()
}
