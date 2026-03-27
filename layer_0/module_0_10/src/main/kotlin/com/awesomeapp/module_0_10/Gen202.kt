package com.awesomeapp.module_0_10

data class GenModel202(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService202 {
    fun process(model: GenModel202): GenModel202
    fun validate(model: GenModel202): Boolean
}

class GenServiceImpl202 : GenService202 {
    override fun process(model: GenModel202): GenModel202 = model.copy(active = true)
    override fun validate(model: GenModel202): Boolean = model.name.isNotEmpty()
}

sealed class GenResult202 {
    data class Success(val data: GenModel202) : GenResult202()
    data class Error(val message: String) : GenResult202()
    data object Loading : GenResult202()
}
