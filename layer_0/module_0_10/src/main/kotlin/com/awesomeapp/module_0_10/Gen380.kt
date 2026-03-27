package com.awesomeapp.module_0_10

data class GenModel380(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService380 {
    fun process(model: GenModel380): GenModel380
    fun validate(model: GenModel380): Boolean
}

class GenServiceImpl380 : GenService380 {
    override fun process(model: GenModel380): GenModel380 = model.copy(active = true)
    override fun validate(model: GenModel380): Boolean = model.name.isNotEmpty()
}

sealed class GenResult380 {
    data class Success(val data: GenModel380) : GenResult380()
    data class Error(val message: String) : GenResult380()
    data object Loading : GenResult380()
}
