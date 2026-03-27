package com.awesomeapp.module_0_10

data class GenModel8(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService8 {
    fun process(model: GenModel8): GenModel8
    fun validate(model: GenModel8): Boolean
}

class GenServiceImpl8 : GenService8 {
    override fun process(model: GenModel8): GenModel8 = model.copy(active = true)
    override fun validate(model: GenModel8): Boolean = model.name.isNotEmpty()
}

sealed class GenResult8 {
    data class Success(val data: GenModel8) : GenResult8()
    data class Error(val message: String) : GenResult8()
    data object Loading : GenResult8()
}
