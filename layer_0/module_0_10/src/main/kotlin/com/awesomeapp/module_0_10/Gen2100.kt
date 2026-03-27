package com.awesomeapp.module_0_10

data class GenModel2100(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2100 {
    fun process(model: GenModel2100): GenModel2100
    fun validate(model: GenModel2100): Boolean
}

class GenServiceImpl2100 : GenService2100 {
    override fun process(model: GenModel2100): GenModel2100 = model.copy(active = true)
    override fun validate(model: GenModel2100): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2100 {
    data class Success(val data: GenModel2100) : GenResult2100()
    data class Error(val message: String) : GenResult2100()
    data object Loading : GenResult2100()
}
