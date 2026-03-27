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

data class GenModel_488_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_488_ {
    data class Load(val id: Long) : GenEvent_488_()
    data class Update(val model: GenModel_488_) : GenEvent_488_()
    data class Delete(val id: Long) : GenEvent_488_()
    data object Refresh : GenEvent_488_()
    data class Search(val query: String) : GenEvent_488_()
    data class Filter(val predicate: String) : GenEvent_488_()
}

sealed class GenState_488_ {
    data object Idle : GenState_488_()
    data object Loading : GenState_488_()
    data class Success(val items: List<GenModel_488_>) : GenState_488_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_488_()
    data class Partial(val items: List<GenModel_488_>, val hasMore: Boolean) : GenState_488_()
}

interface GenRepository_488_ {
    suspend fun getAll(): List<GenModel_488_>
    suspend fun getById(id: Long): GenModel_488_?
    suspend fun save(model: GenModel_488_): GenModel_488_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_488_>
}

@Singleton
class GenRepositoryImpl_488_ @Inject constructor() : GenRepository_488_ {
    private val store = mutableMapOf<Long, GenModel_488_>()
    override suspend fun getAll(): List<GenModel_488_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_488_? = store[id]
    override suspend fun save(model: GenModel_488_): GenModel_488_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_488_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_488_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_488_ @Inject constructor(
    private val repository: GenRepositoryImpl_488_
) : GenUseCase_488_<Unit, List<GenModel_488_>> {
    override suspend fun invoke(params: Unit): List<GenModel_488_> = repository.getAll()
}

class GenSaveUseCase_488_ @Inject constructor(
    private val repository: GenRepositoryImpl_488_
) : GenUseCase_488_<GenModel_488_, GenModel_488_> {
    override suspend fun invoke(params: GenModel_488_): GenModel_488_ = repository.save(params)
}

class GenDeleteUseCase_488_ @Inject constructor(
    private val repository: GenRepositoryImpl_488_
) : GenUseCase_488_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_488_ @Inject constructor(
    private val repository: GenRepositoryImpl_488_
) : GenUseCase_488_<String, List<GenModel_488_>> {
    override suspend fun invoke(params: String): List<GenModel_488_> = repository.search(params)
}

abstract class GenMapper_488_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_488_ : GenMapper_488_<GenModel_488_, String>() {
    override fun map(input: GenModel_488_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_488_ : GenMapper_488_<String, GenModel_488_>() {
    override fun map(input: String): GenModel_488_ {
        val parts = input.split(":")
        return GenModel_488_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_488_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_488_,
    private val saveUseCase: GenSaveUseCase_488_,
    private val deleteUseCase: GenDeleteUseCase_488_,
    private val searchUseCase: GenSearchUseCase_488_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_488_>(GenState_488_.Idle)
    val state: StateFlow<GenState_488_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_488_) {
        when (event) {
            is GenEvent_488_.Load -> loadAll()
            is GenEvent_488_.Update -> save(event.model)
            is GenEvent_488_.Delete -> delete(event.id)
            is GenEvent_488_.Refresh -> loadAll()
            is GenEvent_488_.Search -> search(event.query)
            is GenEvent_488_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_488_.Loading; _state.value = GenState_488_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_488_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_488_.Success(searchUseCase(query)) } }
}
