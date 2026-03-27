package com.awesomeapp.module_0_10

data class GenModel4326(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService4326 {
    fun process(model: GenModel4326): GenModel4326
    fun validate(model: GenModel4326): Boolean
}

class GenServiceImpl4326 : GenService4326 {
    override fun process(model: GenModel4326): GenModel4326 = model.copy(active = true)
    override fun validate(model: GenModel4326): Boolean = model.name.isNotEmpty()
}

sealed class GenResult4326 {
    data class Success(val data: GenModel4326) : GenResult4326()
    data class Error(val message: String) : GenResult4326()
    data object Loading : GenResult4326()
}
