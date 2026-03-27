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

data class GenModel_68_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_68_ {
    data class Load(val id: Long) : GenEvent_68_()
    data class Update(val model: GenModel_68_) : GenEvent_68_()
    data class Delete(val id: Long) : GenEvent_68_()
    data object Refresh : GenEvent_68_()
    data class Search(val query: String) : GenEvent_68_()
    data class Filter(val predicate: String) : GenEvent_68_()
}

sealed class GenState_68_ {
    data object Idle : GenState_68_()
    data object Loading : GenState_68_()
    data class Success(val items: List<GenModel_68_>) : GenState_68_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_68_()
    data class Partial(val items: List<GenModel_68_>, val hasMore: Boolean) : GenState_68_()
}

interface GenRepository_68_ {
    suspend fun getAll(): List<GenModel_68_>
    suspend fun getById(id: Long): GenModel_68_?
    suspend fun save(model: GenModel_68_): GenModel_68_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_68_>
}

@Singleton
class GenRepositoryImpl_68_ @Inject constructor() : GenRepository_68_ {
    private val store = mutableMapOf<Long, GenModel_68_>()
    override suspend fun getAll(): List<GenModel_68_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_68_? = store[id]
    override suspend fun save(model: GenModel_68_): GenModel_68_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_68_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_68_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_68_ @Inject constructor(
    private val repository: GenRepositoryImpl_68_
) : GenUseCase_68_<Unit, List<GenModel_68_>> {
    override suspend fun invoke(params: Unit): List<GenModel_68_> = repository.getAll()
}

class GenSaveUseCase_68_ @Inject constructor(
    private val repository: GenRepositoryImpl_68_
) : GenUseCase_68_<GenModel_68_, GenModel_68_> {
    override suspend fun invoke(params: GenModel_68_): GenModel_68_ = repository.save(params)
}

class GenDeleteUseCase_68_ @Inject constructor(
    private val repository: GenRepositoryImpl_68_
) : GenUseCase_68_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_68_ @Inject constructor(
    private val repository: GenRepositoryImpl_68_
) : GenUseCase_68_<String, List<GenModel_68_>> {
    override suspend fun invoke(params: String): List<GenModel_68_> = repository.search(params)
}

abstract class GenMapper_68_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_68_ : GenMapper_68_<GenModel_68_, String>() {
    override fun map(input: GenModel_68_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_68_ : GenMapper_68_<String, GenModel_68_>() {
    override fun map(input: String): GenModel_68_ {
        val parts = input.split(":")
        return GenModel_68_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_68_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_68_,
    private val saveUseCase: GenSaveUseCase_68_,
    private val deleteUseCase: GenDeleteUseCase_68_,
    private val searchUseCase: GenSearchUseCase_68_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_68_>(GenState_68_.Idle)
    val state: StateFlow<GenState_68_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_68_) {
        when (event) {
            is GenEvent_68_.Load -> loadAll()
            is GenEvent_68_.Update -> save(event.model)
            is GenEvent_68_.Delete -> delete(event.id)
            is GenEvent_68_.Refresh -> loadAll()
            is GenEvent_68_.Search -> search(event.query)
            is GenEvent_68_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_68_.Loading; _state.value = GenState_68_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_68_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_68_.Success(searchUseCase(query)) } }
}
