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

data class GenModel_181_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_181_ {
    data class Load(val id: Long) : GenEvent_181_()
    data class Update(val model: GenModel_181_) : GenEvent_181_()
    data class Delete(val id: Long) : GenEvent_181_()
    data object Refresh : GenEvent_181_()
    data class Search(val query: String) : GenEvent_181_()
    data class Filter(val predicate: String) : GenEvent_181_()
}

sealed class GenState_181_ {
    data object Idle : GenState_181_()
    data object Loading : GenState_181_()
    data class Success(val items: List<GenModel_181_>) : GenState_181_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_181_()
    data class Partial(val items: List<GenModel_181_>, val hasMore: Boolean) : GenState_181_()
}

interface GenRepository_181_ {
    suspend fun getAll(): List<GenModel_181_>
    suspend fun getById(id: Long): GenModel_181_?
    suspend fun save(model: GenModel_181_): GenModel_181_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_181_>
}

@Singleton
class GenRepositoryImpl_181_ @Inject constructor() : GenRepository_181_ {
    private val store = mutableMapOf<Long, GenModel_181_>()
    override suspend fun getAll(): List<GenModel_181_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_181_? = store[id]
    override suspend fun save(model: GenModel_181_): GenModel_181_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_181_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_181_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_181_ @Inject constructor(
    private val repository: GenRepositoryImpl_181_
) : GenUseCase_181_<Unit, List<GenModel_181_>> {
    override suspend fun invoke(params: Unit): List<GenModel_181_> = repository.getAll()
}

class GenSaveUseCase_181_ @Inject constructor(
    private val repository: GenRepositoryImpl_181_
) : GenUseCase_181_<GenModel_181_, GenModel_181_> {
    override suspend fun invoke(params: GenModel_181_): GenModel_181_ = repository.save(params)
}

class GenDeleteUseCase_181_ @Inject constructor(
    private val repository: GenRepositoryImpl_181_
) : GenUseCase_181_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_181_ @Inject constructor(
    private val repository: GenRepositoryImpl_181_
) : GenUseCase_181_<String, List<GenModel_181_>> {
    override suspend fun invoke(params: String): List<GenModel_181_> = repository.search(params)
}

abstract class GenMapper_181_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_181_ : GenMapper_181_<GenModel_181_, String>() {
    override fun map(input: GenModel_181_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_181_ : GenMapper_181_<String, GenModel_181_>() {
    override fun map(input: String): GenModel_181_ {
        val parts = input.split(":")
        return GenModel_181_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_181_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_181_,
    private val saveUseCase: GenSaveUseCase_181_,
    private val deleteUseCase: GenDeleteUseCase_181_,
    private val searchUseCase: GenSearchUseCase_181_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_181_>(GenState_181_.Idle)
    val state: StateFlow<GenState_181_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_181_) {
        when (event) {
            is GenEvent_181_.Load -> loadAll()
            is GenEvent_181_.Update -> save(event.model)
            is GenEvent_181_.Delete -> delete(event.id)
            is GenEvent_181_.Refresh -> loadAll()
            is GenEvent_181_.Search -> search(event.query)
            is GenEvent_181_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_181_.Loading; _state.value = GenState_181_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_181_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_181_.Success(searchUseCase(query)) } }
}
