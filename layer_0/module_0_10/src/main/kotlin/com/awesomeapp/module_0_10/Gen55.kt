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

data class GenModel_55_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_55_ {
    data class Load(val id: Long) : GenEvent_55_()
    data class Update(val model: GenModel_55_) : GenEvent_55_()
    data class Delete(val id: Long) : GenEvent_55_()
    data object Refresh : GenEvent_55_()
    data class Search(val query: String) : GenEvent_55_()
    data class Filter(val predicate: String) : GenEvent_55_()
}

sealed class GenState_55_ {
    data object Idle : GenState_55_()
    data object Loading : GenState_55_()
    data class Success(val items: List<GenModel_55_>) : GenState_55_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_55_()
    data class Partial(val items: List<GenModel_55_>, val hasMore: Boolean) : GenState_55_()
}

interface GenRepository_55_ {
    suspend fun getAll(): List<GenModel_55_>
    suspend fun getById(id: Long): GenModel_55_?
    suspend fun save(model: GenModel_55_): GenModel_55_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_55_>
}

@Singleton
class GenRepositoryImpl_55_ @Inject constructor() : GenRepository_55_ {
    private val store = mutableMapOf<Long, GenModel_55_>()
    override suspend fun getAll(): List<GenModel_55_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_55_? = store[id]
    override suspend fun save(model: GenModel_55_): GenModel_55_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_55_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_55_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_55_ @Inject constructor(
    private val repository: GenRepositoryImpl_55_
) : GenUseCase_55_<Unit, List<GenModel_55_>> {
    override suspend fun invoke(params: Unit): List<GenModel_55_> = repository.getAll()
}

class GenSaveUseCase_55_ @Inject constructor(
    private val repository: GenRepositoryImpl_55_
) : GenUseCase_55_<GenModel_55_, GenModel_55_> {
    override suspend fun invoke(params: GenModel_55_): GenModel_55_ = repository.save(params)
}

class GenDeleteUseCase_55_ @Inject constructor(
    private val repository: GenRepositoryImpl_55_
) : GenUseCase_55_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_55_ @Inject constructor(
    private val repository: GenRepositoryImpl_55_
) : GenUseCase_55_<String, List<GenModel_55_>> {
    override suspend fun invoke(params: String): List<GenModel_55_> = repository.search(params)
}

abstract class GenMapper_55_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_55_ : GenMapper_55_<GenModel_55_, String>() {
    override fun map(input: GenModel_55_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_55_ : GenMapper_55_<String, GenModel_55_>() {
    override fun map(input: String): GenModel_55_ {
        val parts = input.split(":")
        return GenModel_55_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_55_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_55_,
    private val saveUseCase: GenSaveUseCase_55_,
    private val deleteUseCase: GenDeleteUseCase_55_,
    private val searchUseCase: GenSearchUseCase_55_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_55_>(GenState_55_.Idle)
    val state: StateFlow<GenState_55_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_55_) {
        when (event) {
            is GenEvent_55_.Load -> loadAll()
            is GenEvent_55_.Update -> save(event.model)
            is GenEvent_55_.Delete -> delete(event.id)
            is GenEvent_55_.Refresh -> loadAll()
            is GenEvent_55_.Search -> search(event.query)
            is GenEvent_55_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_55_.Loading; _state.value = GenState_55_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_55_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_55_.Success(searchUseCase(query)) } }
}
