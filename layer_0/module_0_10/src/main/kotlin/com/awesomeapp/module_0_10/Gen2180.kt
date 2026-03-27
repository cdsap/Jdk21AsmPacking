package com.awesomeapp.module_0_10

data class GenModel2180(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2180 {
    fun process(model: GenModel2180): GenModel2180
    fun validate(model: GenModel2180): Boolean
}

class GenServiceImpl2180 : GenService2180 {
    override fun process(model: GenModel2180): GenModel2180 = model.copy(active = true)
    override fun validate(model: GenModel2180): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2180 {
    data class Success(val data: GenModel2180) : GenResult2180()
    data class Error(val message: String) : GenResult2180()
    data object Loading : GenResult2180()
}
