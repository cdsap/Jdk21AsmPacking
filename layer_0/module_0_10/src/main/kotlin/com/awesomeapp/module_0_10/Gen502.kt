package com.awesomeapp.module_0_10

data class GenModel502(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService502 {
    fun process(model: GenModel502): GenModel502
    fun validate(model: GenModel502): Boolean
}

class GenServiceImpl502 : GenService502 {
    override fun process(model: GenModel502): GenModel502 = model.copy(active = true)
    override fun validate(model: GenModel502): Boolean = model.name.isNotEmpty()
}

sealed class GenResult502 {
    data class Success(val data: GenModel502) : GenResult502()
    data class Error(val message: String) : GenResult502()
    data object Loading : GenResult502()
}
