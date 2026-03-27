package com.awesomeapp.module_0_10

data class GenModel17(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService17 {
    fun process(model: GenModel17): GenModel17
    fun validate(model: GenModel17): Boolean
}

class GenServiceImpl17 : GenService17 {
    override fun process(model: GenModel17): GenModel17 = model.copy(active = true)
    override fun validate(model: GenModel17): Boolean = model.name.isNotEmpty()
}

sealed class GenResult17 {
    data class Success(val data: GenModel17) : GenResult17()
    data class Error(val message: String) : GenResult17()
    data object Loading : GenResult17()
}
