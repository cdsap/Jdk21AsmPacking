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

data class GenModel_291_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_291_ {
    data class Load(val id: Long) : GenEvent_291_()
    data class Update(val model: GenModel_291_) : GenEvent_291_()
    data class Delete(val id: Long) : GenEvent_291_()
    data object Refresh : GenEvent_291_()
    data class Search(val query: String) : GenEvent_291_()
    data class Filter(val predicate: String) : GenEvent_291_()
}

sealed class GenState_291_ {
    data object Idle : GenState_291_()
    data object Loading : GenState_291_()
    data class Success(val items: List<GenModel_291_>) : GenState_291_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_291_()
    data class Partial(val items: List<GenModel_291_>, val hasMore: Boolean) : GenState_291_()
}

interface GenRepository_291_ {
    suspend fun getAll(): List<GenModel_291_>
    suspend fun getById(id: Long): GenModel_291_?
    suspend fun save(model: GenModel_291_): GenModel_291_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_291_>
}

@Singleton
class GenRepositoryImpl_291_ @Inject constructor() : GenRepository_291_ {
    private val store = mutableMapOf<Long, GenModel_291_>()
    override suspend fun getAll(): List<GenModel_291_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_291_? = store[id]
    override suspend fun save(model: GenModel_291_): GenModel_291_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_291_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_291_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_291_ @Inject constructor(
    private val repository: GenRepositoryImpl_291_
) : GenUseCase_291_<Unit, List<GenModel_291_>> {
    override suspend fun invoke(params: Unit): List<GenModel_291_> = repository.getAll()
}

class GenSaveUseCase_291_ @Inject constructor(
    private val repository: GenRepositoryImpl_291_
) : GenUseCase_291_<GenModel_291_, GenModel_291_> {
    override suspend fun invoke(params: GenModel_291_): GenModel_291_ = repository.save(params)
}

class GenDeleteUseCase_291_ @Inject constructor(
    private val repository: GenRepositoryImpl_291_
) : GenUseCase_291_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_291_ @Inject constructor(
    private val repository: GenRepositoryImpl_291_
) : GenUseCase_291_<String, List<GenModel_291_>> {
    override suspend fun invoke(params: String): List<GenModel_291_> = repository.search(params)
}

abstract class GenMapper_291_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_291_ : GenMapper_291_<GenModel_291_, String>() {
    override fun map(input: GenModel_291_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_291_ : GenMapper_291_<String, GenModel_291_>() {
    override fun map(input: String): GenModel_291_ {
        val parts = input.split(":")
        return GenModel_291_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_291_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_291_,
    private val saveUseCase: GenSaveUseCase_291_,
    private val deleteUseCase: GenDeleteUseCase_291_,
    private val searchUseCase: GenSearchUseCase_291_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_291_>(GenState_291_.Idle)
    val state: StateFlow<GenState_291_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_291_) {
        when (event) {
            is GenEvent_291_.Load -> loadAll()
            is GenEvent_291_.Update -> save(event.model)
            is GenEvent_291_.Delete -> delete(event.id)
            is GenEvent_291_.Refresh -> loadAll()
            is GenEvent_291_.Search -> search(event.query)
            is GenEvent_291_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_291_.Loading; _state.value = GenState_291_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_291_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_291_.Success(searchUseCase(query)) } }
}
