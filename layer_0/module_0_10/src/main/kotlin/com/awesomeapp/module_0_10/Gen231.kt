package com.awesomeapp.module_0_10

data class GenModel231(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService231 {
    fun process(model: GenModel231): GenModel231
    fun validate(model: GenModel231): Boolean
}

class GenServiceImpl231 : GenService231 {
    override fun process(model: GenModel231): GenModel231 = model.copy(active = true)
    override fun validate(model: GenModel231): Boolean = model.name.isNotEmpty()
}

sealed class GenResult231 {
    data class Success(val data: GenModel231) : GenResult231()
    data class Error(val message: String) : GenResult231()
    data object Loading : GenResult231()
}
