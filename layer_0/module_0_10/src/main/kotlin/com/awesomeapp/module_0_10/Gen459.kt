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

data class GenModel_459_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_459_ {
    data class Load(val id: Long) : GenEvent_459_()
    data class Update(val model: GenModel_459_) : GenEvent_459_()
    data class Delete(val id: Long) : GenEvent_459_()
    data object Refresh : GenEvent_459_()
    data class Search(val query: String) : GenEvent_459_()
    data class Filter(val predicate: String) : GenEvent_459_()
}

sealed class GenState_459_ {
    data object Idle : GenState_459_()
    data object Loading : GenState_459_()
    data class Success(val items: List<GenModel_459_>) : GenState_459_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_459_()
    data class Partial(val items: List<GenModel_459_>, val hasMore: Boolean) : GenState_459_()
}

interface GenRepository_459_ {
    suspend fun getAll(): List<GenModel_459_>
    suspend fun getById(id: Long): GenModel_459_?
    suspend fun save(model: GenModel_459_): GenModel_459_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_459_>
}

@Singleton
class GenRepositoryImpl_459_ @Inject constructor() : GenRepository_459_ {
    private val store = mutableMapOf<Long, GenModel_459_>()
    override suspend fun getAll(): List<GenModel_459_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_459_? = store[id]
    override suspend fun save(model: GenModel_459_): GenModel_459_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_459_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_459_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_459_ @Inject constructor(
    private val repository: GenRepositoryImpl_459_
) : GenUseCase_459_<Unit, List<GenModel_459_>> {
    override suspend fun invoke(params: Unit): List<GenModel_459_> = repository.getAll()
}

class GenSaveUseCase_459_ @Inject constructor(
    private val repository: GenRepositoryImpl_459_
) : GenUseCase_459_<GenModel_459_, GenModel_459_> {
    override suspend fun invoke(params: GenModel_459_): GenModel_459_ = repository.save(params)
}

class GenDeleteUseCase_459_ @Inject constructor(
    private val repository: GenRepositoryImpl_459_
) : GenUseCase_459_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_459_ @Inject constructor(
    private val repository: GenRepositoryImpl_459_
) : GenUseCase_459_<String, List<GenModel_459_>> {
    override suspend fun invoke(params: String): List<GenModel_459_> = repository.search(params)
}

abstract class GenMapper_459_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_459_ : GenMapper_459_<GenModel_459_, String>() {
    override fun map(input: GenModel_459_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_459_ : GenMapper_459_<String, GenModel_459_>() {
    override fun map(input: String): GenModel_459_ {
        val parts = input.split(":")
        return GenModel_459_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_459_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_459_,
    private val saveUseCase: GenSaveUseCase_459_,
    private val deleteUseCase: GenDeleteUseCase_459_,
    private val searchUseCase: GenSearchUseCase_459_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_459_>(GenState_459_.Idle)
    val state: StateFlow<GenState_459_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_459_) {
        when (event) {
            is GenEvent_459_.Load -> loadAll()
            is GenEvent_459_.Update -> save(event.model)
            is GenEvent_459_.Delete -> delete(event.id)
            is GenEvent_459_.Refresh -> loadAll()
            is GenEvent_459_.Search -> search(event.query)
            is GenEvent_459_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_459_.Loading; _state.value = GenState_459_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_459_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_459_.Success(searchUseCase(query)) } }
}
