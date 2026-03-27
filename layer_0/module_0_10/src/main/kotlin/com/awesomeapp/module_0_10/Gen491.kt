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

data class GenModel_491_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_491_ {
    data class Load(val id: Long) : GenEvent_491_()
    data class Update(val model: GenModel_491_) : GenEvent_491_()
    data class Delete(val id: Long) : GenEvent_491_()
    data object Refresh : GenEvent_491_()
    data class Search(val query: String) : GenEvent_491_()
    data class Filter(val predicate: String) : GenEvent_491_()
}

sealed class GenState_491_ {
    data object Idle : GenState_491_()
    data object Loading : GenState_491_()
    data class Success(val items: List<GenModel_491_>) : GenState_491_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_491_()
    data class Partial(val items: List<GenModel_491_>, val hasMore: Boolean) : GenState_491_()
}

interface GenRepository_491_ {
    suspend fun getAll(): List<GenModel_491_>
    suspend fun getById(id: Long): GenModel_491_?
    suspend fun save(model: GenModel_491_): GenModel_491_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_491_>
}

@Singleton
class GenRepositoryImpl_491_ @Inject constructor() : GenRepository_491_ {
    private val store = mutableMapOf<Long, GenModel_491_>()
    override suspend fun getAll(): List<GenModel_491_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_491_? = store[id]
    override suspend fun save(model: GenModel_491_): GenModel_491_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_491_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_491_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_491_ @Inject constructor(
    private val repository: GenRepositoryImpl_491_
) : GenUseCase_491_<Unit, List<GenModel_491_>> {
    override suspend fun invoke(params: Unit): List<GenModel_491_> = repository.getAll()
}

class GenSaveUseCase_491_ @Inject constructor(
    private val repository: GenRepositoryImpl_491_
) : GenUseCase_491_<GenModel_491_, GenModel_491_> {
    override suspend fun invoke(params: GenModel_491_): GenModel_491_ = repository.save(params)
}

class GenDeleteUseCase_491_ @Inject constructor(
    private val repository: GenRepositoryImpl_491_
) : GenUseCase_491_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_491_ @Inject constructor(
    private val repository: GenRepositoryImpl_491_
) : GenUseCase_491_<String, List<GenModel_491_>> {
    override suspend fun invoke(params: String): List<GenModel_491_> = repository.search(params)
}

abstract class GenMapper_491_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_491_ : GenMapper_491_<GenModel_491_, String>() {
    override fun map(input: GenModel_491_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_491_ : GenMapper_491_<String, GenModel_491_>() {
    override fun map(input: String): GenModel_491_ {
        val parts = input.split(":")
        return GenModel_491_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_491_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_491_,
    private val saveUseCase: GenSaveUseCase_491_,
    private val deleteUseCase: GenDeleteUseCase_491_,
    private val searchUseCase: GenSearchUseCase_491_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_491_>(GenState_491_.Idle)
    val state: StateFlow<GenState_491_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_491_) {
        when (event) {
            is GenEvent_491_.Load -> loadAll()
            is GenEvent_491_.Update -> save(event.model)
            is GenEvent_491_.Delete -> delete(event.id)
            is GenEvent_491_.Refresh -> loadAll()
            is GenEvent_491_.Search -> search(event.query)
            is GenEvent_491_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_491_.Loading; _state.value = GenState_491_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_491_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_491_.Success(searchUseCase(query)) } }
}
