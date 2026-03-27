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

data class GenModel_49_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_49_ {
    data class Load(val id: Long) : GenEvent_49_()
    data class Update(val model: GenModel_49_) : GenEvent_49_()
    data class Delete(val id: Long) : GenEvent_49_()
    data object Refresh : GenEvent_49_()
    data class Search(val query: String) : GenEvent_49_()
    data class Filter(val predicate: String) : GenEvent_49_()
}

sealed class GenState_49_ {
    data object Idle : GenState_49_()
    data object Loading : GenState_49_()
    data class Success(val items: List<GenModel_49_>) : GenState_49_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_49_()
    data class Partial(val items: List<GenModel_49_>, val hasMore: Boolean) : GenState_49_()
}

interface GenRepository_49_ {
    suspend fun getAll(): List<GenModel_49_>
    suspend fun getById(id: Long): GenModel_49_?
    suspend fun save(model: GenModel_49_): GenModel_49_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_49_>
}

@Singleton
class GenRepositoryImpl_49_ @Inject constructor() : GenRepository_49_ {
    private val store = mutableMapOf<Long, GenModel_49_>()
    override suspend fun getAll(): List<GenModel_49_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_49_? = store[id]
    override suspend fun save(model: GenModel_49_): GenModel_49_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_49_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_49_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_49_ @Inject constructor(
    private val repository: GenRepositoryImpl_49_
) : GenUseCase_49_<Unit, List<GenModel_49_>> {
    override suspend fun invoke(params: Unit): List<GenModel_49_> = repository.getAll()
}

class GenSaveUseCase_49_ @Inject constructor(
    private val repository: GenRepositoryImpl_49_
) : GenUseCase_49_<GenModel_49_, GenModel_49_> {
    override suspend fun invoke(params: GenModel_49_): GenModel_49_ = repository.save(params)
}

class GenDeleteUseCase_49_ @Inject constructor(
    private val repository: GenRepositoryImpl_49_
) : GenUseCase_49_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_49_ @Inject constructor(
    private val repository: GenRepositoryImpl_49_
) : GenUseCase_49_<String, List<GenModel_49_>> {
    override suspend fun invoke(params: String): List<GenModel_49_> = repository.search(params)
}

abstract class GenMapper_49_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_49_ : GenMapper_49_<GenModel_49_, String>() {
    override fun map(input: GenModel_49_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_49_ : GenMapper_49_<String, GenModel_49_>() {
    override fun map(input: String): GenModel_49_ {
        val parts = input.split(":")
        return GenModel_49_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_49_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_49_,
    private val saveUseCase: GenSaveUseCase_49_,
    private val deleteUseCase: GenDeleteUseCase_49_,
    private val searchUseCase: GenSearchUseCase_49_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_49_>(GenState_49_.Idle)
    val state: StateFlow<GenState_49_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_49_) {
        when (event) {
            is GenEvent_49_.Load -> loadAll()
            is GenEvent_49_.Update -> save(event.model)
            is GenEvent_49_.Delete -> delete(event.id)
            is GenEvent_49_.Refresh -> loadAll()
            is GenEvent_49_.Search -> search(event.query)
            is GenEvent_49_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_49_.Loading; _state.value = GenState_49_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_49_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_49_.Success(searchUseCase(query)) } }
}
