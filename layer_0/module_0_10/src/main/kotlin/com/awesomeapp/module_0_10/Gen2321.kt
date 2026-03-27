package com.awesomeapp.module_0_10

data class GenModel2321(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2321 {
    fun process(model: GenModel2321): GenModel2321
    fun validate(model: GenModel2321): Boolean
}

class GenServiceImpl2321 : GenService2321 {
    override fun process(model: GenModel2321): GenModel2321 = model.copy(active = true)
    override fun validate(model: GenModel2321): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2321 {
    data class Success(val data: GenModel2321) : GenResult2321()
    data class Error(val message: String) : GenResult2321()
    data object Loading : GenResult2321()
}
