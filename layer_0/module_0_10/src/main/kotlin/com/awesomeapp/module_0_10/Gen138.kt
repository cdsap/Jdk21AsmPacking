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

data class GenModel_138_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_138_ {
    data class Load(val id: Long) : GenEvent_138_()
    data class Update(val model: GenModel_138_) : GenEvent_138_()
    data class Delete(val id: Long) : GenEvent_138_()
    data object Refresh : GenEvent_138_()
    data class Search(val query: String) : GenEvent_138_()
    data class Filter(val predicate: String) : GenEvent_138_()
}

sealed class GenState_138_ {
    data object Idle : GenState_138_()
    data object Loading : GenState_138_()
    data class Success(val items: List<GenModel_138_>) : GenState_138_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_138_()
    data class Partial(val items: List<GenModel_138_>, val hasMore: Boolean) : GenState_138_()
}

interface GenRepository_138_ {
    suspend fun getAll(): List<GenModel_138_>
    suspend fun getById(id: Long): GenModel_138_?
    suspend fun save(model: GenModel_138_): GenModel_138_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_138_>
}

@Singleton
class GenRepositoryImpl_138_ @Inject constructor() : GenRepository_138_ {
    private val store = mutableMapOf<Long, GenModel_138_>()
    override suspend fun getAll(): List<GenModel_138_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_138_? = store[id]
    override suspend fun save(model: GenModel_138_): GenModel_138_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_138_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_138_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_138_ @Inject constructor(
    private val repository: GenRepositoryImpl_138_
) : GenUseCase_138_<Unit, List<GenModel_138_>> {
    override suspend fun invoke(params: Unit): List<GenModel_138_> = repository.getAll()
}

class GenSaveUseCase_138_ @Inject constructor(
    private val repository: GenRepositoryImpl_138_
) : GenUseCase_138_<GenModel_138_, GenModel_138_> {
    override suspend fun invoke(params: GenModel_138_): GenModel_138_ = repository.save(params)
}

class GenDeleteUseCase_138_ @Inject constructor(
    private val repository: GenRepositoryImpl_138_
) : GenUseCase_138_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_138_ @Inject constructor(
    private val repository: GenRepositoryImpl_138_
) : GenUseCase_138_<String, List<GenModel_138_>> {
    override suspend fun invoke(params: String): List<GenModel_138_> = repository.search(params)
}

abstract class GenMapper_138_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_138_ : GenMapper_138_<GenModel_138_, String>() {
    override fun map(input: GenModel_138_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_138_ : GenMapper_138_<String, GenModel_138_>() {
    override fun map(input: String): GenModel_138_ {
        val parts = input.split(":")
        return GenModel_138_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_138_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_138_,
    private val saveUseCase: GenSaveUseCase_138_,
    private val deleteUseCase: GenDeleteUseCase_138_,
    private val searchUseCase: GenSearchUseCase_138_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_138_>(GenState_138_.Idle)
    val state: StateFlow<GenState_138_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_138_) {
        when (event) {
            is GenEvent_138_.Load -> loadAll()
            is GenEvent_138_.Update -> save(event.model)
            is GenEvent_138_.Delete -> delete(event.id)
            is GenEvent_138_.Refresh -> loadAll()
            is GenEvent_138_.Search -> search(event.query)
            is GenEvent_138_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_138_.Loading; _state.value = GenState_138_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_138_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_138_.Success(searchUseCase(query)) } }
}
