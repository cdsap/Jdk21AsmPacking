package com.awesomeapp.module_0_10

data class GenModel4121(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4121 {
    fun process(model: GenModel4121): GenModel4121
    fun validate(model: GenModel4121): Boolean
}

class GenServiceImpl4121 : GenService4121 {
    override fun process(model: GenModel4121): GenModel4121 = model.copy(active = true)
    override fun validate(model: GenModel4121): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4121 {
    data class Success(val data: GenModel4121) : GenResult4121()
    data class Error(val message: String) : GenResult4121()
    data object Loading : GenResult4121()
}
