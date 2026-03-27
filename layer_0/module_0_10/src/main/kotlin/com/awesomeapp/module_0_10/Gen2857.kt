package com.awesomeapp.module_0_10

data class GenModel2857(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList()
)

interface GenService2857 {
    fun process(model: GenModel2857): GenModel2857
    fun validate(model: GenModel2857): Boolean
}

class GenServiceImpl2857 : GenService2857 {
    override fun process(model: GenModel2857): GenModel2857 = model.copy(active = true)
    override fun validate(model: GenModel2857): Boolean = model.name.isNotEmpty()
}

sealed class GenResult2857 {
    data class Success(val data: GenModel2857) : GenResult2857()
    data class Error(val message: String) : GenResult2857()
    data object Loading : GenResult2857()
}
