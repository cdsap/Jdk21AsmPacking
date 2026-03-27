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

data class GenModel_3198_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3198_ {
    data class Load(val id: Long) : GenEvent_3198_()
    data class Update(val model: GenModel_3198_) : GenEvent_3198_()
    data class Delete(val id: Long) : GenEvent_3198_()
    data object Refresh : GenEvent_3198_()
    data class Search(val query: String) : GenEvent_3198_()
    data class Filter(val predicate: String) : GenEvent_3198_()
}

sealed class GenState_3198_ {
    data object Idle : GenState_3198_()
    data object Loading : GenState_3198_()
    data class Success(val items: List<GenModel_3198_>) : GenState_3198_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3198_()
    data class Partial(val items: List<GenModel_3198_>, val hasMore: Boolean) : GenState_3198_()
}

interface GenRepository_3198_ {
    suspend fun getAll(): List<GenModel_3198_>
    suspend fun getById(id: Long): GenModel_3198_?
    suspend fun save(model: GenModel_3198_): GenModel_3198_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3198_>
}

@Singleton
class GenRepositoryImpl_3198_ @Inject constructor() : GenRepository_3198_ {
    private val store = mutableMapOf<Long, GenModel_3198_>()
    override suspend fun getAll(): List<GenModel_3198_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3198_? = store[id]
    override suspend fun save(model: GenModel_3198_): GenModel_3198_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3198_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3198_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3198_ @Inject constructor(
    private val repository: GenRepositoryImpl_3198_
) : GenUseCase_3198_<Unit, List<GenModel_3198_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3198_> = repository.getAll()
}

class GenSaveUseCase_3198_ @Inject constructor(
    private val repository: GenRepositoryImpl_3198_
) : GenUseCase_3198_<GenModel_3198_, GenModel_3198_> {
    override suspend fun invoke(params: GenModel_3198_): GenModel_3198_ = repository.save(params)
}

class GenDeleteUseCase_3198_ @Inject constructor(
    private val repository: GenRepositoryImpl_3198_
) : GenUseCase_3198_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3198_ @Inject constructor(
    private val repository: GenRepositoryImpl_3198_
) : GenUseCase_3198_<String, List<GenModel_3198_>> {
    override suspend fun invoke(params: String): List<GenModel_3198_> = repository.search(params)
}

abstract class GenMapper_3198_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3198_ : GenMapper_3198_<GenModel_3198_, String>() {
    override fun map(input: GenModel_3198_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3198_ : GenMapper_3198_<String, GenModel_3198_>() {
    override fun map(input: String): GenModel_3198_ {
        val parts = input.split(":")
        return GenModel_3198_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3198_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3198_,
    private val saveUseCase: GenSaveUseCase_3198_,
    private val deleteUseCase: GenDeleteUseCase_3198_,
    private val searchUseCase: GenSearchUseCase_3198_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3198_>(GenState_3198_.Idle)
    val state: StateFlow<GenState_3198_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3198_) {
        when (event) {
            is GenEvent_3198_.Load -> loadAll()
            is GenEvent_3198_.Update -> save(event.model)
            is GenEvent_3198_.Delete -> delete(event.id)
            is GenEvent_3198_.Refresh -> loadAll()
            is GenEvent_3198_.Search -> search(event.query)
            is GenEvent_3198_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3198_.Loading; _state.value = GenState_3198_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3198_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3198_.Success(searchUseCase(query)) } }
}
