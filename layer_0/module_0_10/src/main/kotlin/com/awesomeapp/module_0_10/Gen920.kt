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

data class GenModel_920_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_920_ {
    data class Load(val id: Long) : GenEvent_920_()
    data class Update(val model: GenModel_920_) : GenEvent_920_()
    data class Delete(val id: Long) : GenEvent_920_()
    data object Refresh : GenEvent_920_()
    data class Search(val query: String) : GenEvent_920_()
    data class Filter(val predicate: String) : GenEvent_920_()
}

sealed class GenState_920_ {
    data object Idle : GenState_920_()
    data object Loading : GenState_920_()
    data class Success(val items: List<GenModel_920_>) : GenState_920_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_920_()
    data class Partial(val items: List<GenModel_920_>, val hasMore: Boolean) : GenState_920_()
}

interface GenRepository_920_ {
    suspend fun getAll(): List<GenModel_920_>
    suspend fun getById(id: Long): GenModel_920_?
    suspend fun save(model: GenModel_920_): GenModel_920_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_920_>
}

@Singleton
class GenRepositoryImpl_920_ @Inject constructor() : GenRepository_920_ {
    private val store = mutableMapOf<Long, GenModel_920_>()
    override suspend fun getAll(): List<GenModel_920_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_920_? = store[id]
    override suspend fun save(model: GenModel_920_): GenModel_920_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_920_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_920_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_920_ @Inject constructor(
    private val repository: GenRepositoryImpl_920_
) : GenUseCase_920_<Unit, List<GenModel_920_>> {
    override suspend fun invoke(params: Unit): List<GenModel_920_> = repository.getAll()
}

class GenSaveUseCase_920_ @Inject constructor(
    private val repository: GenRepositoryImpl_920_
) : GenUseCase_920_<GenModel_920_, GenModel_920_> {
    override suspend fun invoke(params: GenModel_920_): GenModel_920_ = repository.save(params)
}

class GenDeleteUseCase_920_ @Inject constructor(
    private val repository: GenRepositoryImpl_920_
) : GenUseCase_920_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_920_ @Inject constructor(
    private val repository: GenRepositoryImpl_920_
) : GenUseCase_920_<String, List<GenModel_920_>> {
    override suspend fun invoke(params: String): List<GenModel_920_> = repository.search(params)
}

abstract class GenMapper_920_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_920_ : GenMapper_920_<GenModel_920_, String>() {
    override fun map(input: GenModel_920_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_920_ : GenMapper_920_<String, GenModel_920_>() {
    override fun map(input: String): GenModel_920_ {
        val parts = input.split(":")
        return GenModel_920_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_920_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_920_,
    private val saveUseCase: GenSaveUseCase_920_,
    private val deleteUseCase: GenDeleteUseCase_920_,
    private val searchUseCase: GenSearchUseCase_920_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_920_>(GenState_920_.Idle)
    val state: StateFlow<GenState_920_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_920_) {
        when (event) {
            is GenEvent_920_.Load -> loadAll()
            is GenEvent_920_.Update -> save(event.model)
            is GenEvent_920_.Delete -> delete(event.id)
            is GenEvent_920_.Refresh -> loadAll()
            is GenEvent_920_.Search -> search(event.query)
            is GenEvent_920_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_920_.Loading; _state.value = GenState_920_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_920_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_920_.Success(searchUseCase(query)) } }
}
