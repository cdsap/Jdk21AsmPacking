package com.awesomeapp.module_0_10

data class GenModel215(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService215 {
    fun process(model: GenModel215): GenModel215
    fun validate(model: GenModel215): Boolean
}

class GenServiceImpl215 : GenService215 {
    override fun process(model: GenModel215): GenModel215 = model.copy(active = true)
    override fun validate(model: GenModel215): Boolean = model.name.isNotEmpty()
}

sealed class GenResult215 {
    data class Success(val data: GenModel215) : GenResult215()
    data class Error(val message: String) : GenResult215()
    data object Loading : GenResult215()
}
