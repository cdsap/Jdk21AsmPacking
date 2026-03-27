package com.awesomeapp.module_0_10

data class GenModel411(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService411 {
    fun process(model: GenModel411): GenModel411
    fun validate(model: GenModel411): Boolean
}

class GenServiceImpl411 : GenService411 {
    override fun process(model: GenModel411): GenModel411 = model.copy(active = true)
    override fun validate(model: GenModel411): Boolean = model.name.isNotEmpty()
}

sealed class GenResult411 {
    data class Success(val data: GenModel411) : GenResult411()
    data class Error(val message: String) : GenResult411()
    data object Loading : GenResult411()
}
