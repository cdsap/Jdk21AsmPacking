package com.awesomeapp.module_0_10

data class GenModel233(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService233 {
    fun process(model: GenModel233): GenModel233
    fun validate(model: GenModel233): Boolean
}

class GenServiceImpl233 : GenService233 {
    override fun process(model: GenModel233): GenModel233 = model.copy(active = true)
    override fun validate(model: GenModel233): Boolean = model.name.isNotEmpty()
}

sealed class GenResult233 {
    data class Success(val data: GenModel233) : GenResult233()
    data class Error(val message: String) : GenResult233()
    data object Loading : GenResult233()
}
