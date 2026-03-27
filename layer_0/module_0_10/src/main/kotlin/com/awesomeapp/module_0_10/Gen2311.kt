package com.awesomeapp.module_0_10

data class GenModel2311(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2311 {
    fun process(model: GenModel2311): GenModel2311
    fun validate(model: GenModel2311): Boolean
}

class GenServiceImpl2311 : GenService2311 {
    override fun process(model: GenModel2311): GenModel2311 = model.copy(active = true)
    override fun validate(model: GenModel2311): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2311 {
    data class Success(val data: GenModel2311) : GenResult2311()
    data class Error(val message: String) : GenResult2311()
    data object Loading : GenResult2311()
}
