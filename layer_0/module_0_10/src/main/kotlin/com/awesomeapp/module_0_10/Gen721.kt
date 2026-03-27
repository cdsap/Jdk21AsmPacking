package com.awesomeapp.module_0_10

data class GenModel721(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService721 {
    fun process(model: GenModel721): GenModel721
    fun validate(model: GenModel721): Boolean
}

class GenServiceImpl721 : GenService721 {
    override fun process(model: GenModel721): GenModel721 = model.copy(active = true)
    override fun validate(model: GenModel721): Boolean = model.name.isNotEmpty()
}

sealed class GenResult721 {
    data class Success(val data: GenModel721) : GenResult721()
    data class Error(val message: String) : GenResult721()
    data object Loading : GenResult721()
}
