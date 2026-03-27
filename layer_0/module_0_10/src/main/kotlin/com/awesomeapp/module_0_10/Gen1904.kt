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

data class GenModel_1904_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1904_ {
    data class Load(val id: Long) : GenEvent_1904_()
    data class Update(val model: GenModel_1904_) : GenEvent_1904_()
    data class Delete(val id: Long) : GenEvent_1904_()
    data object Refresh : GenEvent_1904_()
    data class Search(val query: String) : GenEvent_1904_()
    data class Filter(val predicate: String) : GenEvent_1904_()
}

sealed class GenState_1904_ {
    data object Idle : GenState_1904_()
    data object Loading : GenState_1904_()
    data class Success(val items: List<GenModel_1904_>) : GenState_1904_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1904_()
    data class Partial(val items: List<GenModel_1904_>, val hasMore: Boolean) : GenState_1904_()
}

interface GenRepository_1904_ {
    suspend fun getAll(): List<GenModel_1904_>
    suspend fun getById(id: Long): GenModel_1904_?
    suspend fun save(model: GenModel_1904_): GenModel_1904_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1904_>
}

@Singleton
class GenRepositoryImpl_1904_ @Inject constructor() : GenRepository_1904_ {
    private val store = mutableMapOf<Long, GenModel_1904_>()
    override suspend fun getAll(): List<GenModel_1904_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1904_? = store[id]
    override suspend fun save(model: GenModel_1904_): GenModel_1904_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1904_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1904_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1904_ @Inject constructor(
    private val repository: GenRepositoryImpl_1904_
) : GenUseCase_1904_<Unit, List<GenModel_1904_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1904_> = repository.getAll()
}

class GenSaveUseCase_1904_ @Inject constructor(
    private val repository: GenRepositoryImpl_1904_
) : GenUseCase_1904_<GenModel_1904_, GenModel_1904_> {
    override suspend fun invoke(params: GenModel_1904_): GenModel_1904_ = repository.save(params)
}

class GenDeleteUseCase_1904_ @Inject constructor(
    private val repository: GenRepositoryImpl_1904_
) : GenUseCase_1904_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1904_ @Inject constructor(
    private val repository: GenRepositoryImpl_1904_
) : GenUseCase_1904_<String, List<GenModel_1904_>> {
    override suspend fun invoke(params: String): List<GenModel_1904_> = repository.search(params)
}

abstract class GenMapper_1904_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1904_ : GenMapper_1904_<GenModel_1904_, String>() {
    override fun map(input: GenModel_1904_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1904_ : GenMapper_1904_<String, GenModel_1904_>() {
    override fun map(input: String): GenModel_1904_ {
        val parts = input.split(":")
        return GenModel_1904_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1904_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1904_,
    private val saveUseCase: GenSaveUseCase_1904_,
    private val deleteUseCase: GenDeleteUseCase_1904_,
    private val searchUseCase: GenSearchUseCase_1904_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1904_>(GenState_1904_.Idle)
    val state: StateFlow<GenState_1904_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1904_) {
        when (event) {
            is GenEvent_1904_.Load -> loadAll()
            is GenEvent_1904_.Update -> save(event.model)
            is GenEvent_1904_.Delete -> delete(event.id)
            is GenEvent_1904_.Refresh -> loadAll()
            is GenEvent_1904_.Search -> search(event.query)
            is GenEvent_1904_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1904_.Loading; _state.value = GenState_1904_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1904_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1904_.Success(searchUseCase(query)) } }
}
