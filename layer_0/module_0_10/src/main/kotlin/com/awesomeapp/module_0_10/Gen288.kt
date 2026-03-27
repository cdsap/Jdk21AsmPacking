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

data class GenModel_288_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_288_ {
    data class Load(val id: Long) : GenEvent_288_()
    data class Update(val model: GenModel_288_) : GenEvent_288_()
    data class Delete(val id: Long) : GenEvent_288_()
    data object Refresh : GenEvent_288_()
    data class Search(val query: String) : GenEvent_288_()
    data class Filter(val predicate: String) : GenEvent_288_()
}

sealed class GenState_288_ {
    data object Idle : GenState_288_()
    data object Loading : GenState_288_()
    data class Success(val items: List<GenModel_288_>) : GenState_288_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_288_()
    data class Partial(val items: List<GenModel_288_>, val hasMore: Boolean) : GenState_288_()
}

interface GenRepository_288_ {
    suspend fun getAll(): List<GenModel_288_>
    suspend fun getById(id: Long): GenModel_288_?
    suspend fun save(model: GenModel_288_): GenModel_288_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_288_>
}

@Singleton
class GenRepositoryImpl_288_ @Inject constructor() : GenRepository_288_ {
    private val store = mutableMapOf<Long, GenModel_288_>()
    override suspend fun getAll(): List<GenModel_288_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_288_? = store[id]
    override suspend fun save(model: GenModel_288_): GenModel_288_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_288_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_288_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_288_ @Inject constructor(
    private val repository: GenRepositoryImpl_288_
) : GenUseCase_288_<Unit, List<GenModel_288_>> {
    override suspend fun invoke(params: Unit): List<GenModel_288_> = repository.getAll()
}

class GenSaveUseCase_288_ @Inject constructor(
    private val repository: GenRepositoryImpl_288_
) : GenUseCase_288_<GenModel_288_, GenModel_288_> {
    override suspend fun invoke(params: GenModel_288_): GenModel_288_ = repository.save(params)
}

class GenDeleteUseCase_288_ @Inject constructor(
    private val repository: GenRepositoryImpl_288_
) : GenUseCase_288_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_288_ @Inject constructor(
    private val repository: GenRepositoryImpl_288_
) : GenUseCase_288_<String, List<GenModel_288_>> {
    override suspend fun invoke(params: String): List<GenModel_288_> = repository.search(params)
}

abstract class GenMapper_288_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_288_ : GenMapper_288_<GenModel_288_, String>() {
    override fun map(input: GenModel_288_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_288_ : GenMapper_288_<String, GenModel_288_>() {
    override fun map(input: String): GenModel_288_ {
        val parts = input.split(":")
        return GenModel_288_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_288_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_288_,
    private val saveUseCase: GenSaveUseCase_288_,
    private val deleteUseCase: GenDeleteUseCase_288_,
    private val searchUseCase: GenSearchUseCase_288_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_288_>(GenState_288_.Idle)
    val state: StateFlow<GenState_288_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_288_) {
        when (event) {
            is GenEvent_288_.Load -> loadAll()
            is GenEvent_288_.Update -> save(event.model)
            is GenEvent_288_.Delete -> delete(event.id)
            is GenEvent_288_.Refresh -> loadAll()
            is GenEvent_288_.Search -> search(event.query)
            is GenEvent_288_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_288_.Loading; _state.value = GenState_288_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_288_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_288_.Success(searchUseCase(query)) } }
}
