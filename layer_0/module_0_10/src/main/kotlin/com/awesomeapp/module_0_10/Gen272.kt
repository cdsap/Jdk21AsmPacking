package com.awesomeapp.module_0_10

data class GenModel272(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService272 {
    fun process(model: GenModel272): GenModel272
    fun validate(model: GenModel272): Boolean
}

class GenServiceImpl272 : GenService272 {
    override fun process(model: GenModel272): GenModel272 = model.copy(active = true)
    override fun validate(model: GenModel272): Boolean = model.name.isNotEmpty()
}

sealed class GenResult272 {
    data class Success(val data: GenModel272) : GenResult272()
    data class Error(val message: String) : GenResult272()
    data object Loading : GenResult272()
}
