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

data class GenModel_377_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_377_ {
    data class Load(val id: Long) : GenEvent_377_()
    data class Update(val model: GenModel_377_) : GenEvent_377_()
    data class Delete(val id: Long) : GenEvent_377_()
    data object Refresh : GenEvent_377_()
    data class Search(val query: String) : GenEvent_377_()
    data class Filter(val predicate: String) : GenEvent_377_()
}

sealed class GenState_377_ {
    data object Idle : GenState_377_()
    data object Loading : GenState_377_()
    data class Success(val items: List<GenModel_377_>) : GenState_377_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_377_()
    data class Partial(val items: List<GenModel_377_>, val hasMore: Boolean) : GenState_377_()
}

interface GenRepository_377_ {
    suspend fun getAll(): List<GenModel_377_>
    suspend fun getById(id: Long): GenModel_377_?
    suspend fun save(model: GenModel_377_): GenModel_377_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_377_>
}

@Singleton
class GenRepositoryImpl_377_ @Inject constructor() : GenRepository_377_ {
    private val store = mutableMapOf<Long, GenModel_377_>()
    override suspend fun getAll(): List<GenModel_377_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_377_? = store[id]
    override suspend fun save(model: GenModel_377_): GenModel_377_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_377_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_377_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_377_ @Inject constructor(
    private val repository: GenRepositoryImpl_377_
) : GenUseCase_377_<Unit, List<GenModel_377_>> {
    override suspend fun invoke(params: Unit): List<GenModel_377_> = repository.getAll()
}

class GenSaveUseCase_377_ @Inject constructor(
    private val repository: GenRepositoryImpl_377_
) : GenUseCase_377_<GenModel_377_, GenModel_377_> {
    override suspend fun invoke(params: GenModel_377_): GenModel_377_ = repository.save(params)
}

class GenDeleteUseCase_377_ @Inject constructor(
    private val repository: GenRepositoryImpl_377_
) : GenUseCase_377_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_377_ @Inject constructor(
    private val repository: GenRepositoryImpl_377_
) : GenUseCase_377_<String, List<GenModel_377_>> {
    override suspend fun invoke(params: String): List<GenModel_377_> = repository.search(params)
}

abstract class GenMapper_377_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_377_ : GenMapper_377_<GenModel_377_, String>() {
    override fun map(input: GenModel_377_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_377_ : GenMapper_377_<String, GenModel_377_>() {
    override fun map(input: String): GenModel_377_ {
        val parts = input.split(":")
        return GenModel_377_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_377_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_377_,
    private val saveUseCase: GenSaveUseCase_377_,
    private val deleteUseCase: GenDeleteUseCase_377_,
    private val searchUseCase: GenSearchUseCase_377_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_377_>(GenState_377_.Idle)
    val state: StateFlow<GenState_377_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_377_) {
        when (event) {
            is GenEvent_377_.Load -> loadAll()
            is GenEvent_377_.Update -> save(event.model)
            is GenEvent_377_.Delete -> delete(event.id)
            is GenEvent_377_.Refresh -> loadAll()
            is GenEvent_377_.Search -> search(event.query)
            is GenEvent_377_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_377_.Loading; _state.value = GenState_377_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_377_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_377_.Success(searchUseCase(query)) } }
}
