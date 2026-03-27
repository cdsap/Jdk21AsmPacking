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

data class GenModel_289_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_289_ {
    data class Load(val id: Long) : GenEvent_289_()
    data class Update(val model: GenModel_289_) : GenEvent_289_()
    data class Delete(val id: Long) : GenEvent_289_()
    data object Refresh : GenEvent_289_()
    data class Search(val query: String) : GenEvent_289_()
    data class Filter(val predicate: String) : GenEvent_289_()
}

sealed class GenState_289_ {
    data object Idle : GenState_289_()
    data object Loading : GenState_289_()
    data class Success(val items: List<GenModel_289_>) : GenState_289_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_289_()
    data class Partial(val items: List<GenModel_289_>, val hasMore: Boolean) : GenState_289_()
}

interface GenRepository_289_ {
    suspend fun getAll(): List<GenModel_289_>
    suspend fun getById(id: Long): GenModel_289_?
    suspend fun save(model: GenModel_289_): GenModel_289_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_289_>
}

@Singleton
class GenRepositoryImpl_289_ @Inject constructor() : GenRepository_289_ {
    private val store = mutableMapOf<Long, GenModel_289_>()
    override suspend fun getAll(): List<GenModel_289_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_289_? = store[id]
    override suspend fun save(model: GenModel_289_): GenModel_289_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_289_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_289_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_289_ @Inject constructor(
    private val repository: GenRepositoryImpl_289_
) : GenUseCase_289_<Unit, List<GenModel_289_>> {
    override suspend fun invoke(params: Unit): List<GenModel_289_> = repository.getAll()
}

class GenSaveUseCase_289_ @Inject constructor(
    private val repository: GenRepositoryImpl_289_
) : GenUseCase_289_<GenModel_289_, GenModel_289_> {
    override suspend fun invoke(params: GenModel_289_): GenModel_289_ = repository.save(params)
}

class GenDeleteUseCase_289_ @Inject constructor(
    private val repository: GenRepositoryImpl_289_
) : GenUseCase_289_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_289_ @Inject constructor(
    private val repository: GenRepositoryImpl_289_
) : GenUseCase_289_<String, List<GenModel_289_>> {
    override suspend fun invoke(params: String): List<GenModel_289_> = repository.search(params)
}

abstract class GenMapper_289_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_289_ : GenMapper_289_<GenModel_289_, String>() {
    override fun map(input: GenModel_289_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_289_ : GenMapper_289_<String, GenModel_289_>() {
    override fun map(input: String): GenModel_289_ {
        val parts = input.split(":")
        return GenModel_289_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_289_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_289_,
    private val saveUseCase: GenSaveUseCase_289_,
    private val deleteUseCase: GenDeleteUseCase_289_,
    private val searchUseCase: GenSearchUseCase_289_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_289_>(GenState_289_.Idle)
    val state: StateFlow<GenState_289_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_289_) {
        when (event) {
            is GenEvent_289_.Load -> loadAll()
            is GenEvent_289_.Update -> save(event.model)
            is GenEvent_289_.Delete -> delete(event.id)
            is GenEvent_289_.Refresh -> loadAll()
            is GenEvent_289_.Search -> search(event.query)
            is GenEvent_289_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_289_.Loading; _state.value = GenState_289_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_289_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_289_.Success(searchUseCase(query)) } }
}
