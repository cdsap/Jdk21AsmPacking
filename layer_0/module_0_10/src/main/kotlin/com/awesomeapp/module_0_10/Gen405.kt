package com.awesomeapp.module_0_10

data class GenModel405(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService405 {
    fun process(model: GenModel405): GenModel405
    fun validate(model: GenModel405): Boolean
}

class GenServiceImpl405 : GenService405 {
    override fun process(model: GenModel405): GenModel405 = model.copy(active = true)
    override fun validate(model: GenModel405): Boolean = model.name.isNotEmpty()
}

sealed class GenResult405 {
    data class Success(val data: GenModel405) : GenResult405()
    data class Error(val message: String) : GenResult405()
    data object Loading : GenResult405()
}
