package com.awesomeapp.module_0_10

data class GenModel2(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2 {
    fun process(model: GenModel2): GenModel2
    fun validate(model: GenModel2): Boolean
}

class GenServiceImpl2 : GenService2 {
    override fun process(model: GenModel2): GenModel2 = model.copy(active = true)
    override fun validate(model: GenModel2): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2 {
    data class Success(val data: GenModel2) : GenResult2()
    data class Error(val message: String) : GenResult2()
    data object Loading : GenResult2()
}
