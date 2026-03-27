package com.awesomeapp.module_0_10

data class GenModel3306(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService3306 {
    fun process(model: GenModel3306): GenModel3306
    fun validate(model: GenModel3306): Boolean
}

class GenServiceImpl3306 : GenService3306 {
    override fun process(model: GenModel3306): GenModel3306 = model.copy(active = true)
    override fun validate(model: GenModel3306): Boolean = model.name.isNotEmpty()
}

sealed class GenResult3306 {
    data class Success(val data: GenModel3306) : GenResult3306()
    data class Error(val message: String) : GenResult3306()
    data object Loading : GenResult3306()
}
