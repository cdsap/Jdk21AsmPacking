package com.awesomeapp.module_0_10

data class GenModel260(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService260 {
    fun process(model: GenModel260): GenModel260
    fun validate(model: GenModel260): Boolean
}

class GenServiceImpl260 : GenService260 {
    override fun process(model: GenModel260): GenModel260 = model.copy(active = true)
    override fun validate(model: GenModel260): Boolean = model.name.isNotEmpty()
}

sealed class GenResult260 {
    data class Success(val data: GenModel260) : GenResult260()
    data class Error(val message: String) : GenResult260()
    data object Loading : GenResult260()
}
