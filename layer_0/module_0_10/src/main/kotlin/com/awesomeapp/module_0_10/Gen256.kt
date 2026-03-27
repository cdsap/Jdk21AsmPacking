package com.awesomeapp.module_0_10

data class GenModel256(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService256 {
    fun process(model: GenModel256): GenModel256
    fun validate(model: GenModel256): Boolean
}

class GenServiceImpl256 : GenService256 {
    override fun process(model: GenModel256): GenModel256 = model.copy(active = true)
    override fun validate(model: GenModel256): Boolean = model.name.isNotEmpty()
}

sealed class GenResult256 {
    data class Success(val data: GenModel256) : GenResult256()
    data class Error(val message: String) : GenResult256()
    data object Loading : GenResult256()
}
