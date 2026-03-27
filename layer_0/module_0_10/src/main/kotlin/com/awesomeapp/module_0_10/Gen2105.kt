package com.awesomeapp.module_0_10

data class GenModel2105(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2105 {
    fun process(model: GenModel2105): GenModel2105
    fun validate(model: GenModel2105): Boolean
}

class GenServiceImpl2105 : GenService2105 {
    override fun process(model: GenModel2105): GenModel2105 = model.copy(active = true)
    override fun validate(model: GenModel2105): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2105 {
    data class Success(val data: GenModel2105) : GenResult2105()
    data class Error(val message: String) : GenResult2105()
    data object Loading : GenResult2105()
}
