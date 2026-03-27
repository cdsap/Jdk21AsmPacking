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

data class GenModel_1886_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1886_ {
    data class Load(val id: Long) : GenEvent_1886_()
    data class Update(val model: GenModel_1886_) : GenEvent_1886_()
    data class Delete(val id: Long) : GenEvent_1886_()
    data object Refresh : GenEvent_1886_()
    data class Search(val query: String) : GenEvent_1886_()
    data class Filter(val predicate: String) : GenEvent_1886_()
}

sealed class GenState_1886_ {
    data object Idle : GenState_1886_()
    data object Loading : GenState_1886_()
    data class Success(val items: List<GenModel_1886_>) : GenState_1886_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1886_()
    data class Partial(val items: List<GenModel_1886_>, val hasMore: Boolean) : GenState_1886_()
}

interface GenRepository_1886_ {
    suspend fun getAll(): List<GenModel_1886_>
    suspend fun getById(id: Long): GenModel_1886_?
    suspend fun save(model: GenModel_1886_): GenModel_1886_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1886_>
}

@Singleton
class GenRepositoryImpl_1886_ @Inject constructor() : GenRepository_1886_ {
    private val store = mutableMapOf<Long, GenModel_1886_>()
    override suspend fun getAll(): List<GenModel_1886_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1886_? = store[id]
    override suspend fun save(model: GenModel_1886_): GenModel_1886_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1886_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1886_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1886_ @Inject constructor(
    private val repository: GenRepositoryImpl_1886_
) : GenUseCase_1886_<Unit, List<GenModel_1886_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1886_> = repository.getAll()
}

class GenSaveUseCase_1886_ @Inject constructor(
    private val repository: GenRepositoryImpl_1886_
) : GenUseCase_1886_<GenModel_1886_, GenModel_1886_> {
    override suspend fun invoke(params: GenModel_1886_): GenModel_1886_ = repository.save(params)
}

class GenDeleteUseCase_1886_ @Inject constructor(
    private val repository: GenRepositoryImpl_1886_
) : GenUseCase_1886_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1886_ @Inject constructor(
    private val repository: GenRepositoryImpl_1886_
) : GenUseCase_1886_<String, List<GenModel_1886_>> {
    override suspend fun invoke(params: String): List<GenModel_1886_> = repository.search(params)
}

abstract class GenMapper_1886_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1886_ : GenMapper_1886_<GenModel_1886_, String>() {
    override fun map(input: GenModel_1886_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1886_ : GenMapper_1886_<String, GenModel_1886_>() {
    override fun map(input: String): GenModel_1886_ {
        val parts = input.split(":")
        return GenModel_1886_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1886_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1886_,
    private val saveUseCase: GenSaveUseCase_1886_,
    private val deleteUseCase: GenDeleteUseCase_1886_,
    private val searchUseCase: GenSearchUseCase_1886_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1886_>(GenState_1886_.Idle)
    val state: StateFlow<GenState_1886_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1886_) {
        when (event) {
            is GenEvent_1886_.Load -> loadAll()
            is GenEvent_1886_.Update -> save(event.model)
            is GenEvent_1886_.Delete -> delete(event.id)
            is GenEvent_1886_.Refresh -> loadAll()
            is GenEvent_1886_.Search -> search(event.query)
            is GenEvent_1886_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1886_.Loading; _state.value = GenState_1886_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1886_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1886_.Success(searchUseCase(query)) } }
}
