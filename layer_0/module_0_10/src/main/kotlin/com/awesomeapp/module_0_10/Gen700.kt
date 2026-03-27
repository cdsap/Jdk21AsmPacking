package com.awesomeapp.module_0_10

data class GenModel700(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService700 {
    fun process(model: GenModel700): GenModel700
    fun validate(model: GenModel700): Boolean
}

class GenServiceImpl700 : GenService700 {
    override fun process(model: GenModel700): GenModel700 = model.copy(active = true)
    override fun validate(model: GenModel700): Boolean = model.name.isNotEmpty()
}

sealed class GenResult700 {
    data class Success(val data: GenModel700) : GenResult700()
    data class Error(val message: String) : GenResult700()
    data object Loading : GenResult700()
}
