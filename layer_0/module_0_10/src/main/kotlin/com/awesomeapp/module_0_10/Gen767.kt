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

data class GenModel_767_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_767_ {
    data class Load(val id: Long) : GenEvent_767_()
    data class Update(val model: GenModel_767_) : GenEvent_767_()
    data class Delete(val id: Long) : GenEvent_767_()
    data object Refresh : GenEvent_767_()
    data class Search(val query: String) : GenEvent_767_()
    data class Filter(val predicate: String) : GenEvent_767_()
}

sealed class GenState_767_ {
    data object Idle : GenState_767_()
    data object Loading : GenState_767_()
    data class Success(val items: List<GenModel_767_>) : GenState_767_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_767_()
    data class Partial(val items: List<GenModel_767_>, val hasMore: Boolean) : GenState_767_()
}

interface GenRepository_767_ {
    suspend fun getAll(): List<GenModel_767_>
    suspend fun getById(id: Long): GenModel_767_?
    suspend fun save(model: GenModel_767_): GenModel_767_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_767_>
}

@Singleton
class GenRepositoryImpl_767_ @Inject constructor() : GenRepository_767_ {
    private val store = mutableMapOf<Long, GenModel_767_>()
    override suspend fun getAll(): List<GenModel_767_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_767_? = store[id]
    override suspend fun save(model: GenModel_767_): GenModel_767_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_767_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_767_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_767_ @Inject constructor(
    private val repository: GenRepositoryImpl_767_
) : GenUseCase_767_<Unit, List<GenModel_767_>> {
    override suspend fun invoke(params: Unit): List<GenModel_767_> = repository.getAll()
}

class GenSaveUseCase_767_ @Inject constructor(
    private val repository: GenRepositoryImpl_767_
) : GenUseCase_767_<GenModel_767_, GenModel_767_> {
    override suspend fun invoke(params: GenModel_767_): GenModel_767_ = repository.save(params)
}

class GenDeleteUseCase_767_ @Inject constructor(
    private val repository: GenRepositoryImpl_767_
) : GenUseCase_767_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_767_ @Inject constructor(
    private val repository: GenRepositoryImpl_767_
) : GenUseCase_767_<String, List<GenModel_767_>> {
    override suspend fun invoke(params: String): List<GenModel_767_> = repository.search(params)
}

abstract class GenMapper_767_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_767_ : GenMapper_767_<GenModel_767_, String>() {
    override fun map(input: GenModel_767_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_767_ : GenMapper_767_<String, GenModel_767_>() {
    override fun map(input: String): GenModel_767_ {
        val parts = input.split(":")
        return GenModel_767_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_767_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_767_,
    private val saveUseCase: GenSaveUseCase_767_,
    private val deleteUseCase: GenDeleteUseCase_767_,
    private val searchUseCase: GenSearchUseCase_767_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_767_>(GenState_767_.Idle)
    val state: StateFlow<GenState_767_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_767_) {
        when (event) {
            is GenEvent_767_.Load -> loadAll()
            is GenEvent_767_.Update -> save(event.model)
            is GenEvent_767_.Delete -> delete(event.id)
            is GenEvent_767_.Refresh -> loadAll()
            is GenEvent_767_.Search -> search(event.query)
            is GenEvent_767_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_767_.Loading; _state.value = GenState_767_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_767_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_767_.Success(searchUseCase(query)) } }
}
