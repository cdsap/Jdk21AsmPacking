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

data class GenModel_73_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_73_ {
    data class Load(val id: Long) : GenEvent_73_()
    data class Update(val model: GenModel_73_) : GenEvent_73_()
    data class Delete(val id: Long) : GenEvent_73_()
    data object Refresh : GenEvent_73_()
    data class Search(val query: String) : GenEvent_73_()
    data class Filter(val predicate: String) : GenEvent_73_()
}

sealed class GenState_73_ {
    data object Idle : GenState_73_()
    data object Loading : GenState_73_()
    data class Success(val items: List<GenModel_73_>) : GenState_73_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_73_()
    data class Partial(val items: List<GenModel_73_>, val hasMore: Boolean) : GenState_73_()
}

interface GenRepository_73_ {
    suspend fun getAll(): List<GenModel_73_>
    suspend fun getById(id: Long): GenModel_73_?
    suspend fun save(model: GenModel_73_): GenModel_73_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_73_>
}

@Singleton
class GenRepositoryImpl_73_ @Inject constructor() : GenRepository_73_ {
    private val store = mutableMapOf<Long, GenModel_73_>()
    override suspend fun getAll(): List<GenModel_73_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_73_? = store[id]
    override suspend fun save(model: GenModel_73_): GenModel_73_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_73_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_73_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_73_ @Inject constructor(
    private val repository: GenRepositoryImpl_73_
) : GenUseCase_73_<Unit, List<GenModel_73_>> {
    override suspend fun invoke(params: Unit): List<GenModel_73_> = repository.getAll()
}

class GenSaveUseCase_73_ @Inject constructor(
    private val repository: GenRepositoryImpl_73_
) : GenUseCase_73_<GenModel_73_, GenModel_73_> {
    override suspend fun invoke(params: GenModel_73_): GenModel_73_ = repository.save(params)
}

class GenDeleteUseCase_73_ @Inject constructor(
    private val repository: GenRepositoryImpl_73_
) : GenUseCase_73_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_73_ @Inject constructor(
    private val repository: GenRepositoryImpl_73_
) : GenUseCase_73_<String, List<GenModel_73_>> {
    override suspend fun invoke(params: String): List<GenModel_73_> = repository.search(params)
}

abstract class GenMapper_73_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_73_ : GenMapper_73_<GenModel_73_, String>() {
    override fun map(input: GenModel_73_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_73_ : GenMapper_73_<String, GenModel_73_>() {
    override fun map(input: String): GenModel_73_ {
        val parts = input.split(":")
        return GenModel_73_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_73_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_73_,
    private val saveUseCase: GenSaveUseCase_73_,
    private val deleteUseCase: GenDeleteUseCase_73_,
    private val searchUseCase: GenSearchUseCase_73_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_73_>(GenState_73_.Idle)
    val state: StateFlow<GenState_73_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_73_) {
        when (event) {
            is GenEvent_73_.Load -> loadAll()
            is GenEvent_73_.Update -> save(event.model)
            is GenEvent_73_.Delete -> delete(event.id)
            is GenEvent_73_.Refresh -> loadAll()
            is GenEvent_73_.Search -> search(event.query)
            is GenEvent_73_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_73_.Loading; _state.value = GenState_73_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_73_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_73_.Success(searchUseCase(query)) } }
}
