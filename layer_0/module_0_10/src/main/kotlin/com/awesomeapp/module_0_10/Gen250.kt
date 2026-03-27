package com.awesomeapp.module_0_10

data class GenModel250(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService250 {
    fun process(model: GenModel250): GenModel250
    fun validate(model: GenModel250): Boolean
}

class GenServiceImpl250 : GenService250 {
    override fun process(model: GenModel250): GenModel250 = model.copy(active = true)
    override fun validate(model: GenModel250): Boolean = model.name.isNotEmpty()
}

sealed class GenResult250 {
    data class Success(val data: GenModel250) : GenResult250()
    data class Error(val message: String) : GenResult250()
    data object Loading : GenResult250()
}
