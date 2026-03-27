package com.awesomeapp.module_0_10

data class GenModel2103(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2103 {
    fun process(model: GenModel2103): GenModel2103
    fun validate(model: GenModel2103): Boolean
}

class GenServiceImpl2103 : GenService2103 {
    override fun process(model: GenModel2103): GenModel2103 = model.copy(active = true)
    override fun validate(model: GenModel2103): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2103 {
    data class Success(val data: GenModel2103) : GenResult2103()
    data class Error(val message: String) : GenResult2103()
    data object Loading : GenResult2103()
}
