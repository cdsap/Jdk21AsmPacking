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

data class GenModel_88_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_88_ {
    data class Load(val id: Long) : GenEvent_88_()
    data class Update(val model: GenModel_88_) : GenEvent_88_()
    data class Delete(val id: Long) : GenEvent_88_()
    data object Refresh : GenEvent_88_()
    data class Search(val query: String) : GenEvent_88_()
    data class Filter(val predicate: String) : GenEvent_88_()
}

sealed class GenState_88_ {
    data object Idle : GenState_88_()
    data object Loading : GenState_88_()
    data class Success(val items: List<GenModel_88_>) : GenState_88_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_88_()
    data class Partial(val items: List<GenModel_88_>, val hasMore: Boolean) : GenState_88_()
}

interface GenRepository_88_ {
    suspend fun getAll(): List<GenModel_88_>
    suspend fun getById(id: Long): GenModel_88_?
    suspend fun save(model: GenModel_88_): GenModel_88_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_88_>
}

@Singleton
class GenRepositoryImpl_88_ @Inject constructor() : GenRepository_88_ {
    private val store = mutableMapOf<Long, GenModel_88_>()
    override suspend fun getAll(): List<GenModel_88_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_88_? = store[id]
    override suspend fun save(model: GenModel_88_): GenModel_88_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_88_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_88_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_88_ @Inject constructor(
    private val repository: GenRepositoryImpl_88_
) : GenUseCase_88_<Unit, List<GenModel_88_>> {
    override suspend fun invoke(params: Unit): List<GenModel_88_> = repository.getAll()
}

class GenSaveUseCase_88_ @Inject constructor(
    private val repository: GenRepositoryImpl_88_
) : GenUseCase_88_<GenModel_88_, GenModel_88_> {
    override suspend fun invoke(params: GenModel_88_): GenModel_88_ = repository.save(params)
}

class GenDeleteUseCase_88_ @Inject constructor(
    private val repository: GenRepositoryImpl_88_
) : GenUseCase_88_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_88_ @Inject constructor(
    private val repository: GenRepositoryImpl_88_
) : GenUseCase_88_<String, List<GenModel_88_>> {
    override suspend fun invoke(params: String): List<GenModel_88_> = repository.search(params)
}

abstract class GenMapper_88_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_88_ : GenMapper_88_<GenModel_88_, String>() {
    override fun map(input: GenModel_88_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_88_ : GenMapper_88_<String, GenModel_88_>() {
    override fun map(input: String): GenModel_88_ {
        val parts = input.split(":")
        return GenModel_88_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_88_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_88_,
    private val saveUseCase: GenSaveUseCase_88_,
    private val deleteUseCase: GenDeleteUseCase_88_,
    private val searchUseCase: GenSearchUseCase_88_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_88_>(GenState_88_.Idle)
    val state: StateFlow<GenState_88_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_88_) {
        when (event) {
            is GenEvent_88_.Load -> loadAll()
            is GenEvent_88_.Update -> save(event.model)
            is GenEvent_88_.Delete -> delete(event.id)
            is GenEvent_88_.Refresh -> loadAll()
            is GenEvent_88_.Search -> search(event.query)
            is GenEvent_88_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_88_.Loading; _state.value = GenState_88_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_88_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_88_.Success(searchUseCase(query)) } }
}
