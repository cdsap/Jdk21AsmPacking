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

data class GenModel_875_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_875_ {
    data class Load(val id: Long) : GenEvent_875_()
    data class Update(val model: GenModel_875_) : GenEvent_875_()
    data class Delete(val id: Long) : GenEvent_875_()
    data object Refresh : GenEvent_875_()
    data class Search(val query: String) : GenEvent_875_()
    data class Filter(val predicate: String) : GenEvent_875_()
}

sealed class GenState_875_ {
    data object Idle : GenState_875_()
    data object Loading : GenState_875_()
    data class Success(val items: List<GenModel_875_>) : GenState_875_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_875_()
    data class Partial(val items: List<GenModel_875_>, val hasMore: Boolean) : GenState_875_()
}

interface GenRepository_875_ {
    suspend fun getAll(): List<GenModel_875_>
    suspend fun getById(id: Long): GenModel_875_?
    suspend fun save(model: GenModel_875_): GenModel_875_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_875_>
}

@Singleton
class GenRepositoryImpl_875_ @Inject constructor() : GenRepository_875_ {
    private val store = mutableMapOf<Long, GenModel_875_>()
    override suspend fun getAll(): List<GenModel_875_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_875_? = store[id]
    override suspend fun save(model: GenModel_875_): GenModel_875_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_875_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_875_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_875_ @Inject constructor(
    private val repository: GenRepositoryImpl_875_
) : GenUseCase_875_<Unit, List<GenModel_875_>> {
    override suspend fun invoke(params: Unit): List<GenModel_875_> = repository.getAll()
}

class GenSaveUseCase_875_ @Inject constructor(
    private val repository: GenRepositoryImpl_875_
) : GenUseCase_875_<GenModel_875_, GenModel_875_> {
    override suspend fun invoke(params: GenModel_875_): GenModel_875_ = repository.save(params)
}

class GenDeleteUseCase_875_ @Inject constructor(
    private val repository: GenRepositoryImpl_875_
) : GenUseCase_875_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_875_ @Inject constructor(
    private val repository: GenRepositoryImpl_875_
) : GenUseCase_875_<String, List<GenModel_875_>> {
    override suspend fun invoke(params: String): List<GenModel_875_> = repository.search(params)
}

abstract class GenMapper_875_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_875_ : GenMapper_875_<GenModel_875_, String>() {
    override fun map(input: GenModel_875_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_875_ : GenMapper_875_<String, GenModel_875_>() {
    override fun map(input: String): GenModel_875_ {
        val parts = input.split(":")
        return GenModel_875_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_875_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_875_,
    private val saveUseCase: GenSaveUseCase_875_,
    private val deleteUseCase: GenDeleteUseCase_875_,
    private val searchUseCase: GenSearchUseCase_875_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_875_>(GenState_875_.Idle)
    val state: StateFlow<GenState_875_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_875_) {
        when (event) {
            is GenEvent_875_.Load -> loadAll()
            is GenEvent_875_.Update -> save(event.model)
            is GenEvent_875_.Delete -> delete(event.id)
            is GenEvent_875_.Refresh -> loadAll()
            is GenEvent_875_.Search -> search(event.query)
            is GenEvent_875_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_875_.Loading; _state.value = GenState_875_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_875_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_875_.Success(searchUseCase(query)) } }
}
