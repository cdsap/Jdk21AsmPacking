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

data class GenModel_3180_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3180_ {
    data class Load(val id: Long) : GenEvent_3180_()
    data class Update(val model: GenModel_3180_) : GenEvent_3180_()
    data class Delete(val id: Long) : GenEvent_3180_()
    data object Refresh : GenEvent_3180_()
    data class Search(val query: String) : GenEvent_3180_()
    data class Filter(val predicate: String) : GenEvent_3180_()
}

sealed class GenState_3180_ {
    data object Idle : GenState_3180_()
    data object Loading : GenState_3180_()
    data class Success(val items: List<GenModel_3180_>) : GenState_3180_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3180_()
    data class Partial(val items: List<GenModel_3180_>, val hasMore: Boolean) : GenState_3180_()
}

interface GenRepository_3180_ {
    suspend fun getAll(): List<GenModel_3180_>
    suspend fun getById(id: Long): GenModel_3180_?
    suspend fun save(model: GenModel_3180_): GenModel_3180_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3180_>
}

@Singleton
class GenRepositoryImpl_3180_ @Inject constructor() : GenRepository_3180_ {
    private val store = mutableMapOf<Long, GenModel_3180_>()
    override suspend fun getAll(): List<GenModel_3180_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3180_? = store[id]
    override suspend fun save(model: GenModel_3180_): GenModel_3180_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3180_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3180_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3180_ @Inject constructor(
    private val repository: GenRepositoryImpl_3180_
) : GenUseCase_3180_<Unit, List<GenModel_3180_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3180_> = repository.getAll()
}

class GenSaveUseCase_3180_ @Inject constructor(
    private val repository: GenRepositoryImpl_3180_
) : GenUseCase_3180_<GenModel_3180_, GenModel_3180_> {
    override suspend fun invoke(params: GenModel_3180_): GenModel_3180_ = repository.save(params)
}

class GenDeleteUseCase_3180_ @Inject constructor(
    private val repository: GenRepositoryImpl_3180_
) : GenUseCase_3180_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3180_ @Inject constructor(
    private val repository: GenRepositoryImpl_3180_
) : GenUseCase_3180_<String, List<GenModel_3180_>> {
    override suspend fun invoke(params: String): List<GenModel_3180_> = repository.search(params)
}

abstract class GenMapper_3180_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3180_ : GenMapper_3180_<GenModel_3180_, String>() {
    override fun map(input: GenModel_3180_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3180_ : GenMapper_3180_<String, GenModel_3180_>() {
    override fun map(input: String): GenModel_3180_ {
        val parts = input.split(":")
        return GenModel_3180_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3180_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3180_,
    private val saveUseCase: GenSaveUseCase_3180_,
    private val deleteUseCase: GenDeleteUseCase_3180_,
    private val searchUseCase: GenSearchUseCase_3180_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3180_>(GenState_3180_.Idle)
    val state: StateFlow<GenState_3180_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3180_) {
        when (event) {
            is GenEvent_3180_.Load -> loadAll()
            is GenEvent_3180_.Update -> save(event.model)
            is GenEvent_3180_.Delete -> delete(event.id)
            is GenEvent_3180_.Refresh -> loadAll()
            is GenEvent_3180_.Search -> search(event.query)
            is GenEvent_3180_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3180_.Loading; _state.value = GenState_3180_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3180_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3180_.Success(searchUseCase(query)) } }
}
