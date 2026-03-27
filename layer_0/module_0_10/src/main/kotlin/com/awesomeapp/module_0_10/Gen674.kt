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

data class GenModel_674_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_674_ {
    data class Load(val id: Long) : GenEvent_674_()
    data class Update(val model: GenModel_674_) : GenEvent_674_()
    data class Delete(val id: Long) : GenEvent_674_()
    data object Refresh : GenEvent_674_()
    data class Search(val query: String) : GenEvent_674_()
    data class Filter(val predicate: String) : GenEvent_674_()
}

sealed class GenState_674_ {
    data object Idle : GenState_674_()
    data object Loading : GenState_674_()
    data class Success(val items: List<GenModel_674_>) : GenState_674_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_674_()
    data class Partial(val items: List<GenModel_674_>, val hasMore: Boolean) : GenState_674_()
}

interface GenRepository_674_ {
    suspend fun getAll(): List<GenModel_674_>
    suspend fun getById(id: Long): GenModel_674_?
    suspend fun save(model: GenModel_674_): GenModel_674_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_674_>
}

@Singleton
class GenRepositoryImpl_674_ @Inject constructor() : GenRepository_674_ {
    private val store = mutableMapOf<Long, GenModel_674_>()
    override suspend fun getAll(): List<GenModel_674_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_674_? = store[id]
    override suspend fun save(model: GenModel_674_): GenModel_674_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_674_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_674_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_674_ @Inject constructor(
    private val repository: GenRepositoryImpl_674_
) : GenUseCase_674_<Unit, List<GenModel_674_>> {
    override suspend fun invoke(params: Unit): List<GenModel_674_> = repository.getAll()
}

class GenSaveUseCase_674_ @Inject constructor(
    private val repository: GenRepositoryImpl_674_
) : GenUseCase_674_<GenModel_674_, GenModel_674_> {
    override suspend fun invoke(params: GenModel_674_): GenModel_674_ = repository.save(params)
}

class GenDeleteUseCase_674_ @Inject constructor(
    private val repository: GenRepositoryImpl_674_
) : GenUseCase_674_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_674_ @Inject constructor(
    private val repository: GenRepositoryImpl_674_
) : GenUseCase_674_<String, List<GenModel_674_>> {
    override suspend fun invoke(params: String): List<GenModel_674_> = repository.search(params)
}

abstract class GenMapper_674_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_674_ : GenMapper_674_<GenModel_674_, String>() {
    override fun map(input: GenModel_674_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_674_ : GenMapper_674_<String, GenModel_674_>() {
    override fun map(input: String): GenModel_674_ {
        val parts = input.split(":")
        return GenModel_674_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_674_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_674_,
    private val saveUseCase: GenSaveUseCase_674_,
    private val deleteUseCase: GenDeleteUseCase_674_,
    private val searchUseCase: GenSearchUseCase_674_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_674_>(GenState_674_.Idle)
    val state: StateFlow<GenState_674_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_674_) {
        when (event) {
            is GenEvent_674_.Load -> loadAll()
            is GenEvent_674_.Update -> save(event.model)
            is GenEvent_674_.Delete -> delete(event.id)
            is GenEvent_674_.Refresh -> loadAll()
            is GenEvent_674_.Search -> search(event.query)
            is GenEvent_674_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_674_.Loading; _state.value = GenState_674_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_674_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_674_.Success(searchUseCase(query)) } }
}
