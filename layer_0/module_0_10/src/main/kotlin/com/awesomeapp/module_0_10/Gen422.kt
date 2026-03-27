package com.awesomeapp.module_0_10

data class GenModel422(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService422 {
    fun process(model: GenModel422): GenModel422
    fun validate(model: GenModel422): Boolean
}

class GenServiceImpl422 : GenService422 {
    override fun process(model: GenModel422): GenModel422 = model.copy(active = true)
    override fun validate(model: GenModel422): Boolean = model.name.isNotEmpty()
}

sealed class GenResult422 {
    data class Success(val data: GenModel422) : GenResult422()
    data class Error(val message: String) : GenResult422()
    data object Loading : GenResult422()
}
