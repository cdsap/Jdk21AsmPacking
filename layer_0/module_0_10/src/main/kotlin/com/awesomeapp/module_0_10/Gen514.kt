package com.awesomeapp.module_0_10

data class GenModel514(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService514 {
    fun process(model: GenModel514): GenModel514
    fun validate(model: GenModel514): Boolean
}

class GenServiceImpl514 : GenService514 {
    override fun process(model: GenModel514): GenModel514 = model.copy(active = true)
    override fun validate(model: GenModel514): Boolean = model.name.isNotEmpty()
}

sealed class GenResult514 {
    data class Success(val data: GenModel514) : GenResult514()
    data class Error(val message: String) : GenResult514()
    data object Loading : GenResult514()
}
