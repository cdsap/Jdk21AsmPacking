package com.awesomeapp.module_0_10

data class GenModel3410(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3410 {
    fun process(model: GenModel3410): GenModel3410
    fun validate(model: GenModel3410): Boolean
}

class GenServiceImpl3410 : GenService3410 {
    override fun process(model: GenModel3410): GenModel3410 = model.copy(active = true)
    override fun validate(model: GenModel3410): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3410 {
    data class Success(val data: GenModel3410) : GenResult3410()
    data class Error(val message: String) : GenResult3410()
    data object Loading : GenResult3410()
}
