package com.awesomeapp.module_0_10

data class GenModel3603(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3603 {
    fun process(model: GenModel3603): GenModel3603
    fun validate(model: GenModel3603): Boolean
}

class GenServiceImpl3603 : GenService3603 {
    override fun process(model: GenModel3603): GenModel3603 = model.copy(active = true)
    override fun validate(model: GenModel3603): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3603 {
    data class Success(val data: GenModel3603) : GenResult3603()
    data class Error(val message: String) : GenResult3603()
    data object Loading : GenResult3603()
}
