package com.awesomeapp.module_0_10

data class GenModel760(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService760 {
    fun process(model: GenModel760): GenModel760
    fun validate(model: GenModel760): Boolean
}

class GenServiceImpl760 : GenService760 {
    override fun process(model: GenModel760): GenModel760 = model.copy(active = true)
    override fun validate(model: GenModel760): Boolean = model.name.isNotEmpty()
}

sealed class GenResult760 {
    data class Success(val data: GenModel760) : GenResult760()
    data class Error(val message: String) : GenResult760()
    data object Loading : GenResult760()
}
