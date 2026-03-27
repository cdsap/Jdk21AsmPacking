package com.awesomeapp.module_0_10

data class GenModel26(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService26 {
    fun process(model: GenModel26): GenModel26
    fun validate(model: GenModel26): Boolean
}

class GenServiceImpl26 : GenService26 {
    override fun process(model: GenModel26): GenModel26 = model.copy(active = true)
    override fun validate(model: GenModel26): Boolean = model.name.isNotEmpty()
}

sealed class GenResult26 {
    data class Success(val data: GenModel26) : GenResult26()
    data class Error(val message: String) : GenResult26()
    data object Loading : GenResult26()
}
