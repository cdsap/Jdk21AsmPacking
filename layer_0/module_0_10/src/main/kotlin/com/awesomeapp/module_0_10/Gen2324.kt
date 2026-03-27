package com.awesomeapp.module_0_10

data class GenModel2324(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2324 {
    fun process(model: GenModel2324): GenModel2324
    fun validate(model: GenModel2324): Boolean
}

class GenServiceImpl2324 : GenService2324 {
    override fun process(model: GenModel2324): GenModel2324 = model.copy(active = true)
    override fun validate(model: GenModel2324): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2324 {
    data class Success(val data: GenModel2324) : GenResult2324()
    data class Error(val message: String) : GenResult2324()
    data object Loading : GenResult2324()
}
