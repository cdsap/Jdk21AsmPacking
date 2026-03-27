package com.awesomeapp.module_0_10

data class GenModel365(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService365 {
    fun process(model: GenModel365): GenModel365
    fun validate(model: GenModel365): Boolean
}

class GenServiceImpl365 : GenService365 {
    override fun process(model: GenModel365): GenModel365 = model.copy(active = true)
    override fun validate(model: GenModel365): Boolean = model.name.isNotEmpty()
}

sealed class GenResult365 {
    data class Success(val data: GenModel365) : GenResult365()
    data class Error(val message: String) : GenResult365()
    data object Loading : GenResult365()
}
