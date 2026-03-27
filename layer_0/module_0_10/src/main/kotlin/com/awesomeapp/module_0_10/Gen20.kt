package com.awesomeapp.module_0_10

data class GenModel20(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService20 {
    fun process(model: GenModel20): GenModel20
    fun validate(model: GenModel20): Boolean
}

class GenServiceImpl20 : GenService20 {
    override fun process(model: GenModel20): GenModel20 = model.copy(active = true)
    override fun validate(model: GenModel20): Boolean = model.name.isNotEmpty()
}

sealed class GenResult20 {
    data class Success(val data: GenModel20) : GenResult20()
    data class Error(val message: String) : GenResult20()
    data object Loading : GenResult20()
}
