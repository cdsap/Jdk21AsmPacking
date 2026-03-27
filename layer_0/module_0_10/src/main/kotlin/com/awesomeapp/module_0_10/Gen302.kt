package com.awesomeapp.module_0_10

data class GenModel302(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService302 {
    fun process(model: GenModel302): GenModel302
    fun validate(model: GenModel302): Boolean
}

class GenServiceImpl302 : GenService302 {
    override fun process(model: GenModel302): GenModel302 = model.copy(active = true)
    override fun validate(model: GenModel302): Boolean = model.name.isNotEmpty()
}

sealed class GenResult302 {
    data class Success(val data: GenModel302) : GenResult302()
    data class Error(val message: String) : GenResult302()
    data object Loading : GenResult302()
}
