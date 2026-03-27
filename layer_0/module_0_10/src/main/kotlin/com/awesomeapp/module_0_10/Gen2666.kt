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

data class GenModel_2666_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2666_ {
    data class Load(val id: Long) : GenEvent_2666_()
    data class Update(val model: GenModel_2666_) : GenEvent_2666_()
    data class Delete(val id: Long) : GenEvent_2666_()
    data object Refresh : GenEvent_2666_()
    data class Search(val query: String) : GenEvent_2666_()
    data class Filter(val predicate: String) : GenEvent_2666_()
}

sealed class GenState_2666_ {
    data object Idle : GenState_2666_()
    data object Loading : GenState_2666_()
    data class Success(val items: List<GenModel_2666_>) : GenState_2666_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2666_()
    data class Partial(val items: List<GenModel_2666_>, val hasMore: Boolean) : GenState_2666_()
}

interface GenRepository_2666_ {
    suspend fun getAll(): List<GenModel_2666_>
    suspend fun getById(id: Long): GenModel_2666_?
    suspend fun save(model: GenModel_2666_): GenModel_2666_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2666_>
}

@Singleton
class GenRepositoryImpl_2666_ @Inject constructor() : GenRepository_2666_ {
    private val store = mutableMapOf<Long, GenModel_2666_>()
    override suspend fun getAll(): List<GenModel_2666_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2666_? = store[id]
    override suspend fun save(model: GenModel_2666_): GenModel_2666_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2666_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2666_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2666_ @Inject constructor(
    private val repository: GenRepositoryImpl_2666_
) : GenUseCase_2666_<Unit, List<GenModel_2666_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2666_> = repository.getAll()
}

class GenSaveUseCase_2666_ @Inject constructor(
    private val repository: GenRepositoryImpl_2666_
) : GenUseCase_2666_<GenModel_2666_, GenModel_2666_> {
    override suspend fun invoke(params: GenModel_2666_): GenModel_2666_ = repository.save(params)
}

class GenDeleteUseCase_2666_ @Inject constructor(
    private val repository: GenRepositoryImpl_2666_
) : GenUseCase_2666_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2666_ @Inject constructor(
    private val repository: GenRepositoryImpl_2666_
) : GenUseCase_2666_<String, List<GenModel_2666_>> {
    override suspend fun invoke(params: String): List<GenModel_2666_> = repository.search(params)
}

abstract class GenMapper_2666_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2666_ : GenMapper_2666_<GenModel_2666_, String>() {
    override fun map(input: GenModel_2666_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2666_ : GenMapper_2666_<String, GenModel_2666_>() {
    override fun map(input: String): GenModel_2666_ {
        val parts = input.split(":")
        return GenModel_2666_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2666_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2666_,
    private val saveUseCase: GenSaveUseCase_2666_,
    private val deleteUseCase: GenDeleteUseCase_2666_,
    private val searchUseCase: GenSearchUseCase_2666_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2666_>(GenState_2666_.Idle)
    val state: StateFlow<GenState_2666_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2666_) {
        when (event) {
            is GenEvent_2666_.Load -> loadAll()
            is GenEvent_2666_.Update -> save(event.model)
            is GenEvent_2666_.Delete -> delete(event.id)
            is GenEvent_2666_.Refresh -> loadAll()
            is GenEvent_2666_.Search -> search(event.query)
            is GenEvent_2666_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2666_.Loading; _state.value = GenState_2666_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2666_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2666_.Success(searchUseCase(query)) } }
}
