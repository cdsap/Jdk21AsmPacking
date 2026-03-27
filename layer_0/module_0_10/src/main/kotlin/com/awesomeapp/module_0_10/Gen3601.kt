package com.awesomeapp.module_0_10

data class GenModel3601(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3601 {
    fun process(model: GenModel3601): GenModel3601
    fun validate(model: GenModel3601): Boolean
}

class GenServiceImpl3601 : GenService3601 {
    override fun process(model: GenModel3601): GenModel3601 = model.copy(active = true)
    override fun validate(model: GenModel3601): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3601 {
    data class Success(val data: GenModel3601) : GenResult3601()
    data class Error(val message: String) : GenResult3601()
    data object Loading : GenResult3601()
}
