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

data class GenModel_3256_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3256_ {
    data class Load(val id: Long) : GenEvent_3256_()
    data class Update(val model: GenModel_3256_) : GenEvent_3256_()
    data class Delete(val id: Long) : GenEvent_3256_()
    data object Refresh : GenEvent_3256_()
    data class Search(val query: String) : GenEvent_3256_()
    data class Filter(val predicate: String) : GenEvent_3256_()
}

sealed class GenState_3256_ {
    data object Idle : GenState_3256_()
    data object Loading : GenState_3256_()
    data class Success(val items: List<GenModel_3256_>) : GenState_3256_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3256_()
    data class Partial(val items: List<GenModel_3256_>, val hasMore: Boolean) : GenState_3256_()
}

interface GenRepository_3256_ {
    suspend fun getAll(): List<GenModel_3256_>
    suspend fun getById(id: Long): GenModel_3256_?
    suspend fun save(model: GenModel_3256_): GenModel_3256_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3256_>
}

@Singleton
class GenRepositoryImpl_3256_ @Inject constructor() : GenRepository_3256_ {
    private val store = mutableMapOf<Long, GenModel_3256_>()
    override suspend fun getAll(): List<GenModel_3256_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3256_? = store[id]
    override suspend fun save(model: GenModel_3256_): GenModel_3256_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3256_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3256_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3256_ @Inject constructor(
    private val repository: GenRepositoryImpl_3256_
) : GenUseCase_3256_<Unit, List<GenModel_3256_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3256_> = repository.getAll()
}

class GenSaveUseCase_3256_ @Inject constructor(
    private val repository: GenRepositoryImpl_3256_
) : GenUseCase_3256_<GenModel_3256_, GenModel_3256_> {
    override suspend fun invoke(params: GenModel_3256_): GenModel_3256_ = repository.save(params)
}

class GenDeleteUseCase_3256_ @Inject constructor(
    private val repository: GenRepositoryImpl_3256_
) : GenUseCase_3256_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3256_ @Inject constructor(
    private val repository: GenRepositoryImpl_3256_
) : GenUseCase_3256_<String, List<GenModel_3256_>> {
    override suspend fun invoke(params: String): List<GenModel_3256_> = repository.search(params)
}

abstract class GenMapper_3256_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3256_ : GenMapper_3256_<GenModel_3256_, String>() {
    override fun map(input: GenModel_3256_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3256_ : GenMapper_3256_<String, GenModel_3256_>() {
    override fun map(input: String): GenModel_3256_ {
        val parts = input.split(":")
        return GenModel_3256_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3256_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3256_,
    private val saveUseCase: GenSaveUseCase_3256_,
    private val deleteUseCase: GenDeleteUseCase_3256_,
    private val searchUseCase: GenSearchUseCase_3256_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3256_>(GenState_3256_.Idle)
    val state: StateFlow<GenState_3256_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3256_) {
        when (event) {
            is GenEvent_3256_.Load -> loadAll()
            is GenEvent_3256_.Update -> save(event.model)
            is GenEvent_3256_.Delete -> delete(event.id)
            is GenEvent_3256_.Refresh -> loadAll()
            is GenEvent_3256_.Search -> search(event.query)
            is GenEvent_3256_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3256_.Loading; _state.value = GenState_3256_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3256_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3256_.Success(searchUseCase(query)) } }
}
