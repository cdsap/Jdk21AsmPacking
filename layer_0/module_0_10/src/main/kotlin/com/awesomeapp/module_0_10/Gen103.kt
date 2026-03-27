package com.awesomeapp.module_0_10

data class GenModel103(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService103 {
    fun process(model: GenModel103): GenModel103
    fun validate(model: GenModel103): Boolean
}

class GenServiceImpl103 : GenService103 {
    override fun process(model: GenModel103): GenModel103 = model.copy(active = true)
    override fun validate(model: GenModel103): Boolean = model.name.isNotEmpty()
}

sealed class GenResult103 {
    data class Success(val data: GenModel103) : GenResult103()
    data class Error(val message: String) : GenResult103()
    data object Loading : GenResult103()
}
