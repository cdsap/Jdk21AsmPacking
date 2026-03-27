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

data class GenModel_208_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_208_ {
    data class Load(val id: Long) : GenEvent_208_()
    data class Update(val model: GenModel_208_) : GenEvent_208_()
    data class Delete(val id: Long) : GenEvent_208_()
    data object Refresh : GenEvent_208_()
    data class Search(val query: String) : GenEvent_208_()
    data class Filter(val predicate: String) : GenEvent_208_()
}

sealed class GenState_208_ {
    data object Idle : GenState_208_()
    data object Loading : GenState_208_()
    data class Success(val items: List<GenModel_208_>) : GenState_208_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_208_()
    data class Partial(val items: List<GenModel_208_>, val hasMore: Boolean) : GenState_208_()
}

interface GenRepository_208_ {
    suspend fun getAll(): List<GenModel_208_>
    suspend fun getById(id: Long): GenModel_208_?
    suspend fun save(model: GenModel_208_): GenModel_208_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_208_>
}

@Singleton
class GenRepositoryImpl_208_ @Inject constructor() : GenRepository_208_ {
    private val store = mutableMapOf<Long, GenModel_208_>()
    override suspend fun getAll(): List<GenModel_208_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_208_? = store[id]
    override suspend fun save(model: GenModel_208_): GenModel_208_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_208_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_208_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_208_ @Inject constructor(
    private val repository: GenRepositoryImpl_208_
) : GenUseCase_208_<Unit, List<GenModel_208_>> {
    override suspend fun invoke(params: Unit): List<GenModel_208_> = repository.getAll()
}

class GenSaveUseCase_208_ @Inject constructor(
    private val repository: GenRepositoryImpl_208_
) : GenUseCase_208_<GenModel_208_, GenModel_208_> {
    override suspend fun invoke(params: GenModel_208_): GenModel_208_ = repository.save(params)
}

class GenDeleteUseCase_208_ @Inject constructor(
    private val repository: GenRepositoryImpl_208_
) : GenUseCase_208_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_208_ @Inject constructor(
    private val repository: GenRepositoryImpl_208_
) : GenUseCase_208_<String, List<GenModel_208_>> {
    override suspend fun invoke(params: String): List<GenModel_208_> = repository.search(params)
}

abstract class GenMapper_208_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_208_ : GenMapper_208_<GenModel_208_, String>() {
    override fun map(input: GenModel_208_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_208_ : GenMapper_208_<String, GenModel_208_>() {
    override fun map(input: String): GenModel_208_ {
        val parts = input.split(":")
        return GenModel_208_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_208_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_208_,
    private val saveUseCase: GenSaveUseCase_208_,
    private val deleteUseCase: GenDeleteUseCase_208_,
    private val searchUseCase: GenSearchUseCase_208_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_208_>(GenState_208_.Idle)
    val state: StateFlow<GenState_208_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_208_) {
        when (event) {
            is GenEvent_208_.Load -> loadAll()
            is GenEvent_208_.Update -> save(event.model)
            is GenEvent_208_.Delete -> delete(event.id)
            is GenEvent_208_.Refresh -> loadAll()
            is GenEvent_208_.Search -> search(event.query)
            is GenEvent_208_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_208_.Loading; _state.value = GenState_208_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_208_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_208_.Success(searchUseCase(query)) } }
}
