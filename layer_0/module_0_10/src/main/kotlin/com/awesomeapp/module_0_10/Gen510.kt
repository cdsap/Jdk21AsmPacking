package com.awesomeapp.module_0_10

data class GenModel510(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService510 {
    fun process(model: GenModel510): GenModel510
    fun validate(model: GenModel510): Boolean
}

class GenServiceImpl510 : GenService510 {
    override fun process(model: GenModel510): GenModel510 = model.copy(active = true)
    override fun validate(model: GenModel510): Boolean = model.name.isNotEmpty()
}

sealed class GenResult510 {
    data class Success(val data: GenModel510) : GenResult510()
    data class Error(val message: String) : GenResult510()
    data object Loading : GenResult510()
}
