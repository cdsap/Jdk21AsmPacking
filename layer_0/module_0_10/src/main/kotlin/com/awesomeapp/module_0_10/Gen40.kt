package com.awesomeapp.module_0_10

data class GenModel40(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService40 {
    fun process(model: GenModel40): GenModel40
    fun validate(model: GenModel40): Boolean
}

class GenServiceImpl40 : GenService40 {
    override fun process(model: GenModel40): GenModel40 = model.copy(active = true)
    override fun validate(model: GenModel40): Boolean = model.name.isNotEmpty()
}

sealed class GenResult40 {
    data class Success(val data: GenModel40) : GenResult40()
    data class Error(val message: String) : GenResult40()
    data object Loading : GenResult40()
}
