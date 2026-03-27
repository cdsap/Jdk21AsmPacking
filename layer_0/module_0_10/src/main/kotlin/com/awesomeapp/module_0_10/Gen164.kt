package com.awesomeapp.module_0_10

data class GenModel164(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService164 {
    fun process(model: GenModel164): GenModel164
    fun validate(model: GenModel164): Boolean
}

class GenServiceImpl164 : GenService164 {
    override fun process(model: GenModel164): GenModel164 = model.copy(active = true)
    override fun validate(model: GenModel164): Boolean = model.name.isNotEmpty()
}

sealed class GenResult164 {
    data class Success(val data: GenModel164) : GenResult164()
    data class Error(val message: String) : GenResult164()
    data object Loading : GenResult164()
}
