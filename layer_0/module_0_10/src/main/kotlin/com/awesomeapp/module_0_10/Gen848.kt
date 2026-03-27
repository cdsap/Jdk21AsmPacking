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

data class GenModel_848_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_848_ {
    data class Load(val id: Long) : GenEvent_848_()
    data class Update(val model: GenModel_848_) : GenEvent_848_()
    data class Delete(val id: Long) : GenEvent_848_()
    data object Refresh : GenEvent_848_()
    data class Search(val query: String) : GenEvent_848_()
    data class Filter(val predicate: String) : GenEvent_848_()
}

sealed class GenState_848_ {
    data object Idle : GenState_848_()
    data object Loading : GenState_848_()
    data class Success(val items: List<GenModel_848_>) : GenState_848_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_848_()
    data class Partial(val items: List<GenModel_848_>, val hasMore: Boolean) : GenState_848_()
}

interface GenRepository_848_ {
    suspend fun getAll(): List<GenModel_848_>
    suspend fun getById(id: Long): GenModel_848_?
    suspend fun save(model: GenModel_848_): GenModel_848_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_848_>
}

@Singleton
class GenRepositoryImpl_848_ @Inject constructor() : GenRepository_848_ {
    private val store = mutableMapOf<Long, GenModel_848_>()
    override suspend fun getAll(): List<GenModel_848_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_848_? = store[id]
    override suspend fun save(model: GenModel_848_): GenModel_848_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_848_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_848_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_848_ @Inject constructor(
    private val repository: GenRepositoryImpl_848_
) : GenUseCase_848_<Unit, List<GenModel_848_>> {
    override suspend fun invoke(params: Unit): List<GenModel_848_> = repository.getAll()
}

class GenSaveUseCase_848_ @Inject constructor(
    private val repository: GenRepositoryImpl_848_
) : GenUseCase_848_<GenModel_848_, GenModel_848_> {
    override suspend fun invoke(params: GenModel_848_): GenModel_848_ = repository.save(params)
}

class GenDeleteUseCase_848_ @Inject constructor(
    private val repository: GenRepositoryImpl_848_
) : GenUseCase_848_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_848_ @Inject constructor(
    private val repository: GenRepositoryImpl_848_
) : GenUseCase_848_<String, List<GenModel_848_>> {
    override suspend fun invoke(params: String): List<GenModel_848_> = repository.search(params)
}

abstract class GenMapper_848_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_848_ : GenMapper_848_<GenModel_848_, String>() {
    override fun map(input: GenModel_848_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_848_ : GenMapper_848_<String, GenModel_848_>() {
    override fun map(input: String): GenModel_848_ {
        val parts = input.split(":")
        return GenModel_848_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_848_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_848_,
    private val saveUseCase: GenSaveUseCase_848_,
    private val deleteUseCase: GenDeleteUseCase_848_,
    private val searchUseCase: GenSearchUseCase_848_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_848_>(GenState_848_.Idle)
    val state: StateFlow<GenState_848_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_848_) {
        when (event) {
            is GenEvent_848_.Load -> loadAll()
            is GenEvent_848_.Update -> save(event.model)
            is GenEvent_848_.Delete -> delete(event.id)
            is GenEvent_848_.Refresh -> loadAll()
            is GenEvent_848_.Search -> search(event.query)
            is GenEvent_848_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_848_.Loading; _state.value = GenState_848_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_848_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_848_.Success(searchUseCase(query)) } }
}
