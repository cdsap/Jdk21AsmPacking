package com.awesomeapp.module_0_10

data class GenModel3011(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3011 {
    fun process(model: GenModel3011): GenModel3011
    fun validate(model: GenModel3011): Boolean
}

class GenServiceImpl3011 : GenService3011 {
    override fun process(model: GenModel3011): GenModel3011 = model.copy(active = true)
    override fun validate(model: GenModel3011): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3011 {
    data class Success(val data: GenModel3011) : GenResult3011()
    data class Error(val message: String) : GenResult3011()
    data object Loading : GenResult3011()
}
