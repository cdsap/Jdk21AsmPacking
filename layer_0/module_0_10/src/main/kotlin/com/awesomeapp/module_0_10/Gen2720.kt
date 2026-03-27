package com.awesomeapp.module_0_10

data class GenModel2720(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2720 {
    fun process(model: GenModel2720): GenModel2720
    fun validate(model: GenModel2720): Boolean
}

class GenServiceImpl2720 : GenService2720 {
    override fun process(model: GenModel2720): GenModel2720 = model.copy(active = true)
    override fun validate(model: GenModel2720): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2720 {
    data class Success(val data: GenModel2720) : GenResult2720()
    data class Error(val message: String) : GenResult2720()
    data object Loading : GenResult2720()
}
