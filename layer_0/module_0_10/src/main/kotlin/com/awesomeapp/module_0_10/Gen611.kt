package com.awesomeapp.module_0_10

data class GenModel611(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService611 {
    fun process(model: GenModel611): GenModel611
    fun validate(model: GenModel611): Boolean
}

class GenServiceImpl611 : GenService611 {
    override fun process(model: GenModel611): GenModel611 = model.copy(active = true)
    override fun validate(model: GenModel611): Boolean = model.name.isNotEmpty()
}

sealed class GenResult611 {
    data class Success(val data: GenModel611) : GenResult611()
    data class Error(val message: String) : GenResult611()
    data object Loading : GenResult611()
}
