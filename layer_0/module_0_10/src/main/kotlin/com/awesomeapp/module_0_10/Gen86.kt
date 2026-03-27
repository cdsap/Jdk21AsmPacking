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

data class GenModel_86_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_86_ {
    data class Load(val id: Long) : GenEvent_86_()
    data class Update(val model: GenModel_86_) : GenEvent_86_()
    data class Delete(val id: Long) : GenEvent_86_()
    data object Refresh : GenEvent_86_()
    data class Search(val query: String) : GenEvent_86_()
    data class Filter(val predicate: String) : GenEvent_86_()
}

sealed class GenState_86_ {
    data object Idle : GenState_86_()
    data object Loading : GenState_86_()
    data class Success(val items: List<GenModel_86_>) : GenState_86_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_86_()
    data class Partial(val items: List<GenModel_86_>, val hasMore: Boolean) : GenState_86_()
}

interface GenRepository_86_ {
    suspend fun getAll(): List<GenModel_86_>
    suspend fun getById(id: Long): GenModel_86_?
    suspend fun save(model: GenModel_86_): GenModel_86_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_86_>
}

@Singleton
class GenRepositoryImpl_86_ @Inject constructor() : GenRepository_86_ {
    private val store = mutableMapOf<Long, GenModel_86_>()
    override suspend fun getAll(): List<GenModel_86_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_86_? = store[id]
    override suspend fun save(model: GenModel_86_): GenModel_86_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_86_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_86_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_86_ @Inject constructor(
    private val repository: GenRepositoryImpl_86_
) : GenUseCase_86_<Unit, List<GenModel_86_>> {
    override suspend fun invoke(params: Unit): List<GenModel_86_> = repository.getAll()
}

class GenSaveUseCase_86_ @Inject constructor(
    private val repository: GenRepositoryImpl_86_
) : GenUseCase_86_<GenModel_86_, GenModel_86_> {
    override suspend fun invoke(params: GenModel_86_): GenModel_86_ = repository.save(params)
}

class GenDeleteUseCase_86_ @Inject constructor(
    private val repository: GenRepositoryImpl_86_
) : GenUseCase_86_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_86_ @Inject constructor(
    private val repository: GenRepositoryImpl_86_
) : GenUseCase_86_<String, List<GenModel_86_>> {
    override suspend fun invoke(params: String): List<GenModel_86_> = repository.search(params)
}

abstract class GenMapper_86_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_86_ : GenMapper_86_<GenModel_86_, String>() {
    override fun map(input: GenModel_86_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_86_ : GenMapper_86_<String, GenModel_86_>() {
    override fun map(input: String): GenModel_86_ {
        val parts = input.split(":")
        return GenModel_86_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_86_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_86_,
    private val saveUseCase: GenSaveUseCase_86_,
    private val deleteUseCase: GenDeleteUseCase_86_,
    private val searchUseCase: GenSearchUseCase_86_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_86_>(GenState_86_.Idle)
    val state: StateFlow<GenState_86_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_86_) {
        when (event) {
            is GenEvent_86_.Load -> loadAll()
            is GenEvent_86_.Update -> save(event.model)
            is GenEvent_86_.Delete -> delete(event.id)
            is GenEvent_86_.Refresh -> loadAll()
            is GenEvent_86_.Search -> search(event.query)
            is GenEvent_86_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_86_.Loading; _state.value = GenState_86_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_86_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_86_.Success(searchUseCase(query)) } }
}
