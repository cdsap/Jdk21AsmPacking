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

data class GenModel_695_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_695_ {
    data class Load(val id: Long) : GenEvent_695_()
    data class Update(val model: GenModel_695_) : GenEvent_695_()
    data class Delete(val id: Long) : GenEvent_695_()
    data object Refresh : GenEvent_695_()
    data class Search(val query: String) : GenEvent_695_()
    data class Filter(val predicate: String) : GenEvent_695_()
}

sealed class GenState_695_ {
    data object Idle : GenState_695_()
    data object Loading : GenState_695_()
    data class Success(val items: List<GenModel_695_>) : GenState_695_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_695_()
    data class Partial(val items: List<GenModel_695_>, val hasMore: Boolean) : GenState_695_()
}

interface GenRepository_695_ {
    suspend fun getAll(): List<GenModel_695_>
    suspend fun getById(id: Long): GenModel_695_?
    suspend fun save(model: GenModel_695_): GenModel_695_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_695_>
}

@Singleton
class GenRepositoryImpl_695_ @Inject constructor() : GenRepository_695_ {
    private val store = mutableMapOf<Long, GenModel_695_>()
    override suspend fun getAll(): List<GenModel_695_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_695_? = store[id]
    override suspend fun save(model: GenModel_695_): GenModel_695_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_695_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_695_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_695_ @Inject constructor(
    private val repository: GenRepositoryImpl_695_
) : GenUseCase_695_<Unit, List<GenModel_695_>> {
    override suspend fun invoke(params: Unit): List<GenModel_695_> = repository.getAll()
}

class GenSaveUseCase_695_ @Inject constructor(
    private val repository: GenRepositoryImpl_695_
) : GenUseCase_695_<GenModel_695_, GenModel_695_> {
    override suspend fun invoke(params: GenModel_695_): GenModel_695_ = repository.save(params)
}

class GenDeleteUseCase_695_ @Inject constructor(
    private val repository: GenRepositoryImpl_695_
) : GenUseCase_695_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_695_ @Inject constructor(
    private val repository: GenRepositoryImpl_695_
) : GenUseCase_695_<String, List<GenModel_695_>> {
    override suspend fun invoke(params: String): List<GenModel_695_> = repository.search(params)
}

abstract class GenMapper_695_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_695_ : GenMapper_695_<GenModel_695_, String>() {
    override fun map(input: GenModel_695_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_695_ : GenMapper_695_<String, GenModel_695_>() {
    override fun map(input: String): GenModel_695_ {
        val parts = input.split(":")
        return GenModel_695_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_695_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_695_,
    private val saveUseCase: GenSaveUseCase_695_,
    private val deleteUseCase: GenDeleteUseCase_695_,
    private val searchUseCase: GenSearchUseCase_695_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_695_>(GenState_695_.Idle)
    val state: StateFlow<GenState_695_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_695_) {
        when (event) {
            is GenEvent_695_.Load -> loadAll()
            is GenEvent_695_.Update -> save(event.model)
            is GenEvent_695_.Delete -> delete(event.id)
            is GenEvent_695_.Refresh -> loadAll()
            is GenEvent_695_.Search -> search(event.query)
            is GenEvent_695_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_695_.Loading; _state.value = GenState_695_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_695_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_695_.Success(searchUseCase(query)) } }
}
