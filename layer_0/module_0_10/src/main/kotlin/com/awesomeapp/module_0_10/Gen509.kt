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

data class GenModel_509_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_509_ {
    data class Load(val id: Long) : GenEvent_509_()
    data class Update(val model: GenModel_509_) : GenEvent_509_()
    data class Delete(val id: Long) : GenEvent_509_()
    data object Refresh : GenEvent_509_()
    data class Search(val query: String) : GenEvent_509_()
    data class Filter(val predicate: String) : GenEvent_509_()
}

sealed class GenState_509_ {
    data object Idle : GenState_509_()
    data object Loading : GenState_509_()
    data class Success(val items: List<GenModel_509_>) : GenState_509_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_509_()
    data class Partial(val items: List<GenModel_509_>, val hasMore: Boolean) : GenState_509_()
}

interface GenRepository_509_ {
    suspend fun getAll(): List<GenModel_509_>
    suspend fun getById(id: Long): GenModel_509_?
    suspend fun save(model: GenModel_509_): GenModel_509_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_509_>
}

@Singleton
class GenRepositoryImpl_509_ @Inject constructor() : GenRepository_509_ {
    private val store = mutableMapOf<Long, GenModel_509_>()
    override suspend fun getAll(): List<GenModel_509_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_509_? = store[id]
    override suspend fun save(model: GenModel_509_): GenModel_509_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_509_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_509_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_509_ @Inject constructor(
    private val repository: GenRepositoryImpl_509_
) : GenUseCase_509_<Unit, List<GenModel_509_>> {
    override suspend fun invoke(params: Unit): List<GenModel_509_> = repository.getAll()
}

class GenSaveUseCase_509_ @Inject constructor(
    private val repository: GenRepositoryImpl_509_
) : GenUseCase_509_<GenModel_509_, GenModel_509_> {
    override suspend fun invoke(params: GenModel_509_): GenModel_509_ = repository.save(params)
}

class GenDeleteUseCase_509_ @Inject constructor(
    private val repository: GenRepositoryImpl_509_
) : GenUseCase_509_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_509_ @Inject constructor(
    private val repository: GenRepositoryImpl_509_
) : GenUseCase_509_<String, List<GenModel_509_>> {
    override suspend fun invoke(params: String): List<GenModel_509_> = repository.search(params)
}

abstract class GenMapper_509_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_509_ : GenMapper_509_<GenModel_509_, String>() {
    override fun map(input: GenModel_509_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_509_ : GenMapper_509_<String, GenModel_509_>() {
    override fun map(input: String): GenModel_509_ {
        val parts = input.split(":")
        return GenModel_509_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_509_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_509_,
    private val saveUseCase: GenSaveUseCase_509_,
    private val deleteUseCase: GenDeleteUseCase_509_,
    private val searchUseCase: GenSearchUseCase_509_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_509_>(GenState_509_.Idle)
    val state: StateFlow<GenState_509_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_509_) {
        when (event) {
            is GenEvent_509_.Load -> loadAll()
            is GenEvent_509_.Update -> save(event.model)
            is GenEvent_509_.Delete -> delete(event.id)
            is GenEvent_509_.Refresh -> loadAll()
            is GenEvent_509_.Search -> search(event.query)
            is GenEvent_509_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_509_.Loading; _state.value = GenState_509_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_509_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_509_.Success(searchUseCase(query)) } }
}
