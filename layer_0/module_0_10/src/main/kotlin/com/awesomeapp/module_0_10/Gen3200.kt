package com.awesomeapp.module_0_10

data class GenModel3200(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3200 {
    fun process(model: GenModel3200): GenModel3200
    fun validate(model: GenModel3200): Boolean
}

class GenServiceImpl3200 : GenService3200 {
    override fun process(model: GenModel3200): GenModel3200 = model.copy(active = true)
    override fun validate(model: GenModel3200): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3200 {
    data class Success(val data: GenModel3200) : GenResult3200()
    data class Error(val message: String) : GenResult3200()
    data object Loading : GenResult3200()
}
