package com.awesomeapp.module_0_10

data class GenModel307(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService307 {
    fun process(model: GenModel307): GenModel307
    fun validate(model: GenModel307): Boolean
}

class GenServiceImpl307 : GenService307 {
    override fun process(model: GenModel307): GenModel307 = model.copy(active = true)
    override fun validate(model: GenModel307): Boolean = model.name.isNotEmpty()
}

sealed class GenResult307 {
    data class Success(val data: GenModel307) : GenResult307()
    data class Error(val message: String) : GenResult307()
    data object Loading : GenResult307()
}
