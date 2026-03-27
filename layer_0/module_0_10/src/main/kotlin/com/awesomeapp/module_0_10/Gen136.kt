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

data class GenModel_136_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_136_ {
    data class Load(val id: Long) : GenEvent_136_()
    data class Update(val model: GenModel_136_) : GenEvent_136_()
    data class Delete(val id: Long) : GenEvent_136_()
    data object Refresh : GenEvent_136_()
    data class Search(val query: String) : GenEvent_136_()
    data class Filter(val predicate: String) : GenEvent_136_()
}

sealed class GenState_136_ {
    data object Idle : GenState_136_()
    data object Loading : GenState_136_()
    data class Success(val items: List<GenModel_136_>) : GenState_136_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_136_()
    data class Partial(val items: List<GenModel_136_>, val hasMore: Boolean) : GenState_136_()
}

interface GenRepository_136_ {
    suspend fun getAll(): List<GenModel_136_>
    suspend fun getById(id: Long): GenModel_136_?
    suspend fun save(model: GenModel_136_): GenModel_136_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_136_>
}

@Singleton
class GenRepositoryImpl_136_ @Inject constructor() : GenRepository_136_ {
    private val store = mutableMapOf<Long, GenModel_136_>()
    override suspend fun getAll(): List<GenModel_136_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_136_? = store[id]
    override suspend fun save(model: GenModel_136_): GenModel_136_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_136_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_136_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_136_ @Inject constructor(
    private val repository: GenRepositoryImpl_136_
) : GenUseCase_136_<Unit, List<GenModel_136_>> {
    override suspend fun invoke(params: Unit): List<GenModel_136_> = repository.getAll()
}

class GenSaveUseCase_136_ @Inject constructor(
    private val repository: GenRepositoryImpl_136_
) : GenUseCase_136_<GenModel_136_, GenModel_136_> {
    override suspend fun invoke(params: GenModel_136_): GenModel_136_ = repository.save(params)
}

class GenDeleteUseCase_136_ @Inject constructor(
    private val repository: GenRepositoryImpl_136_
) : GenUseCase_136_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_136_ @Inject constructor(
    private val repository: GenRepositoryImpl_136_
) : GenUseCase_136_<String, List<GenModel_136_>> {
    override suspend fun invoke(params: String): List<GenModel_136_> = repository.search(params)
}

abstract class GenMapper_136_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_136_ : GenMapper_136_<GenModel_136_, String>() {
    override fun map(input: GenModel_136_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_136_ : GenMapper_136_<String, GenModel_136_>() {
    override fun map(input: String): GenModel_136_ {
        val parts = input.split(":")
        return GenModel_136_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_136_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_136_,
    private val saveUseCase: GenSaveUseCase_136_,
    private val deleteUseCase: GenDeleteUseCase_136_,
    private val searchUseCase: GenSearchUseCase_136_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_136_>(GenState_136_.Idle)
    val state: StateFlow<GenState_136_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_136_) {
        when (event) {
            is GenEvent_136_.Load -> loadAll()
            is GenEvent_136_.Update -> save(event.model)
            is GenEvent_136_.Delete -> delete(event.id)
            is GenEvent_136_.Refresh -> loadAll()
            is GenEvent_136_.Search -> search(event.query)
            is GenEvent_136_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_136_.Loading; _state.value = GenState_136_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_136_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_136_.Success(searchUseCase(query)) } }
}
