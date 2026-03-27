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

data class GenModel_3145_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3145_ {
    data class Load(val id: Long) : GenEvent_3145_()
    data class Update(val model: GenModel_3145_) : GenEvent_3145_()
    data class Delete(val id: Long) : GenEvent_3145_()
    data object Refresh : GenEvent_3145_()
    data class Search(val query: String) : GenEvent_3145_()
    data class Filter(val predicate: String) : GenEvent_3145_()
}

sealed class GenState_3145_ {
    data object Idle : GenState_3145_()
    data object Loading : GenState_3145_()
    data class Success(val items: List<GenModel_3145_>) : GenState_3145_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3145_()
    data class Partial(val items: List<GenModel_3145_>, val hasMore: Boolean) : GenState_3145_()
}

interface GenRepository_3145_ {
    suspend fun getAll(): List<GenModel_3145_>
    suspend fun getById(id: Long): GenModel_3145_?
    suspend fun save(model: GenModel_3145_): GenModel_3145_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3145_>
}

@Singleton
class GenRepositoryImpl_3145_ @Inject constructor() : GenRepository_3145_ {
    private val store = mutableMapOf<Long, GenModel_3145_>()
    override suspend fun getAll(): List<GenModel_3145_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3145_? = store[id]
    override suspend fun save(model: GenModel_3145_): GenModel_3145_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3145_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3145_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3145_ @Inject constructor(
    private val repository: GenRepositoryImpl_3145_
) : GenUseCase_3145_<Unit, List<GenModel_3145_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3145_> = repository.getAll()
}

class GenSaveUseCase_3145_ @Inject constructor(
    private val repository: GenRepositoryImpl_3145_
) : GenUseCase_3145_<GenModel_3145_, GenModel_3145_> {
    override suspend fun invoke(params: GenModel_3145_): GenModel_3145_ = repository.save(params)
}

class GenDeleteUseCase_3145_ @Inject constructor(
    private val repository: GenRepositoryImpl_3145_
) : GenUseCase_3145_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3145_ @Inject constructor(
    private val repository: GenRepositoryImpl_3145_
) : GenUseCase_3145_<String, List<GenModel_3145_>> {
    override suspend fun invoke(params: String): List<GenModel_3145_> = repository.search(params)
}

abstract class GenMapper_3145_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3145_ : GenMapper_3145_<GenModel_3145_, String>() {
    override fun map(input: GenModel_3145_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3145_ : GenMapper_3145_<String, GenModel_3145_>() {
    override fun map(input: String): GenModel_3145_ {
        val parts = input.split(":")
        return GenModel_3145_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3145_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3145_,
    private val saveUseCase: GenSaveUseCase_3145_,
    private val deleteUseCase: GenDeleteUseCase_3145_,
    private val searchUseCase: GenSearchUseCase_3145_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3145_>(GenState_3145_.Idle)
    val state: StateFlow<GenState_3145_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3145_) {
        when (event) {
            is GenEvent_3145_.Load -> loadAll()
            is GenEvent_3145_.Update -> save(event.model)
            is GenEvent_3145_.Delete -> delete(event.id)
            is GenEvent_3145_.Refresh -> loadAll()
            is GenEvent_3145_.Search -> search(event.query)
            is GenEvent_3145_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3145_.Loading; _state.value = GenState_3145_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3145_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3145_.Success(searchUseCase(query)) } }
}
