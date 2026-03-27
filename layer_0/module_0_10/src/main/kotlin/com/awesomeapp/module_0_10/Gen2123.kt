package com.awesomeapp.module_0_10

data class GenModel2123(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2123 {
    fun process(model: GenModel2123): GenModel2123
    fun validate(model: GenModel2123): Boolean
}

class GenServiceImpl2123 : GenService2123 {
    override fun process(model: GenModel2123): GenModel2123 = model.copy(active = true)
    override fun validate(model: GenModel2123): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2123 {
    data class Success(val data: GenModel2123) : GenResult2123()
    data class Error(val message: String) : GenResult2123()
    data object Loading : GenResult2123()
}
