package com.awesomeapp.module_0_10

import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenModel_381_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_381_ {
    data class Load(val id: Long) : GenEvent_381_()
    data class Update(val model: GenModel_381_) : GenEvent_381_()
    data class Delete(val id: Long) : GenEvent_381_()
    data object Refresh : GenEvent_381_()
    data class Search(val query: String) : GenEvent_381_()
    data class Filter(val predicate: String) : GenEvent_381_()
}

sealed class GenState_381_ {
    data object Idle : GenState_381_()
    data object Loading : GenState_381_()
    data class Success(val items: List<GenModel_381_>) : GenState_381_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_381_()
    data class Partial(val items: List<GenModel_381_>, val hasMore: Boolean) : GenState_381_()
}

interface GenRepository_381_ {
    suspend fun getAll(): List<GenModel_381_>
    suspend fun getById(id: Long): GenModel_381_?
    suspend fun save(model: GenModel_381_): GenModel_381_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_381_>
}

@Singleton
class GenRepositoryImpl_381_ @Inject constructor() : GenRepository_381_ {
    private val store = mutableMapOf<Long, GenModel_381_>()
    override suspend fun getAll(): List<GenModel_381_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_381_? = store[id]
    override suspend fun save(model: GenModel_381_): GenModel_381_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_381_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_381_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_381_ @Inject constructor(
    private val repository: GenRepositoryImpl_381_
) : GenUseCase_381_<Unit, List<GenModel_381_>> {
    override suspend fun invoke(params: Unit): List<GenModel_381_> = repository.getAll()
}

class GenSaveUseCase_381_ @Inject constructor(
    private val repository: GenRepositoryImpl_381_
) : GenUseCase_381_<GenModel_381_, GenModel_381_> {
    override suspend fun invoke(params: GenModel_381_): GenModel_381_ = repository.save(params)
}

class GenDeleteUseCase_381_ @Inject constructor(
    private val repository: GenRepositoryImpl_381_
) : GenUseCase_381_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_381_ @Inject constructor(
    private val repository: GenRepositoryImpl_381_
) : GenUseCase_381_<String, List<GenModel_381_>> {
    override suspend fun invoke(params: String): List<GenModel_381_> = repository.search(params)
}

abstract class GenMapper_381_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_381_ : GenMapper_381_<GenModel_381_, String>() {
    override fun map(input: GenModel_381_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_381_ : GenMapper_381_<String, GenModel_381_>() {
    override fun map(input: String): GenModel_381_ {
        val parts = input.split(":")
        return GenModel_381_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_381_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_381_,
    private val saveUseCase: GenSaveUseCase_381_,
    private val deleteUseCase: GenDeleteUseCase_381_,
    private val searchUseCase: GenSearchUseCase_381_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_381_>(GenState_381_.Idle)
    val state: StateFlow<GenState_381_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_381_) {
        when (event) {
            is GenEvent_381_.Load -> loadAll()
            is GenEvent_381_.Update -> save(event.model)
            is GenEvent_381_.Delete -> delete(event.id)
            is GenEvent_381_.Refresh -> loadAll()
            is GenEvent_381_.Search -> search(event.query)
            is GenEvent_381_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_381_.Loading; _state.value = GenState_381_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_381_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_381_.Success(searchUseCase(query)) } }
}
