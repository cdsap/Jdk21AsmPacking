package com.awesomeapp.module_0_10

data class GenModel720(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService720 {
    fun process(model: GenModel720): GenModel720
    fun validate(model: GenModel720): Boolean
}

class GenServiceImpl720 : GenService720 {
    override fun process(model: GenModel720): GenModel720 = model.copy(active = true)
    override fun validate(model: GenModel720): Boolean = model.name.isNotEmpty()
}

sealed class GenResult720 {
    data class Success(val data: GenModel720) : GenResult720()
    data class Error(val message: String) : GenResult720()
    data object Loading : GenResult720()
}
