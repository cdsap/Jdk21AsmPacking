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

data class GenModel_176_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_176_ {
    data class Load(val id: Long) : GenEvent_176_()
    data class Update(val model: GenModel_176_) : GenEvent_176_()
    data class Delete(val id: Long) : GenEvent_176_()
    data object Refresh : GenEvent_176_()
    data class Search(val query: String) : GenEvent_176_()
    data class Filter(val predicate: String) : GenEvent_176_()
}

sealed class GenState_176_ {
    data object Idle : GenState_176_()
    data object Loading : GenState_176_()
    data class Success(val items: List<GenModel_176_>) : GenState_176_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_176_()
    data class Partial(val items: List<GenModel_176_>, val hasMore: Boolean) : GenState_176_()
}

interface GenRepository_176_ {
    suspend fun getAll(): List<GenModel_176_>
    suspend fun getById(id: Long): GenModel_176_?
    suspend fun save(model: GenModel_176_): GenModel_176_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_176_>
}

@Singleton
class GenRepositoryImpl_176_ @Inject constructor() : GenRepository_176_ {
    private val store = mutableMapOf<Long, GenModel_176_>()
    override suspend fun getAll(): List<GenModel_176_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_176_? = store[id]
    override suspend fun save(model: GenModel_176_): GenModel_176_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_176_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_176_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_176_ @Inject constructor(
    private val repository: GenRepositoryImpl_176_
) : GenUseCase_176_<Unit, List<GenModel_176_>> {
    override suspend fun invoke(params: Unit): List<GenModel_176_> = repository.getAll()
}

class GenSaveUseCase_176_ @Inject constructor(
    private val repository: GenRepositoryImpl_176_
) : GenUseCase_176_<GenModel_176_, GenModel_176_> {
    override suspend fun invoke(params: GenModel_176_): GenModel_176_ = repository.save(params)
}

class GenDeleteUseCase_176_ @Inject constructor(
    private val repository: GenRepositoryImpl_176_
) : GenUseCase_176_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_176_ @Inject constructor(
    private val repository: GenRepositoryImpl_176_
) : GenUseCase_176_<String, List<GenModel_176_>> {
    override suspend fun invoke(params: String): List<GenModel_176_> = repository.search(params)
}

abstract class GenMapper_176_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_176_ : GenMapper_176_<GenModel_176_, String>() {
    override fun map(input: GenModel_176_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_176_ : GenMapper_176_<String, GenModel_176_>() {
    override fun map(input: String): GenModel_176_ {
        val parts = input.split(":")
        return GenModel_176_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_176_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_176_,
    private val saveUseCase: GenSaveUseCase_176_,
    private val deleteUseCase: GenDeleteUseCase_176_,
    private val searchUseCase: GenSearchUseCase_176_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_176_>(GenState_176_.Idle)
    val state: StateFlow<GenState_176_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_176_) {
        when (event) {
            is GenEvent_176_.Load -> loadAll()
            is GenEvent_176_.Update -> save(event.model)
            is GenEvent_176_.Delete -> delete(event.id)
            is GenEvent_176_.Refresh -> loadAll()
            is GenEvent_176_.Search -> search(event.query)
            is GenEvent_176_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_176_.Loading; _state.value = GenState_176_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_176_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_176_.Success(searchUseCase(query)) } }
}
