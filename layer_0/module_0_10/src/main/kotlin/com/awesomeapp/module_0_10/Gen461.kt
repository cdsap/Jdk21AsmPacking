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

data class GenModel_461_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_461_ {
    data class Load(val id: Long) : GenEvent_461_()
    data class Update(val model: GenModel_461_) : GenEvent_461_()
    data class Delete(val id: Long) : GenEvent_461_()
    data object Refresh : GenEvent_461_()
    data class Search(val query: String) : GenEvent_461_()
    data class Filter(val predicate: String) : GenEvent_461_()
}

sealed class GenState_461_ {
    data object Idle : GenState_461_()
    data object Loading : GenState_461_()
    data class Success(val items: List<GenModel_461_>) : GenState_461_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_461_()
    data class Partial(val items: List<GenModel_461_>, val hasMore: Boolean) : GenState_461_()
}

interface GenRepository_461_ {
    suspend fun getAll(): List<GenModel_461_>
    suspend fun getById(id: Long): GenModel_461_?
    suspend fun save(model: GenModel_461_): GenModel_461_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_461_>
}

@Singleton
class GenRepositoryImpl_461_ @Inject constructor() : GenRepository_461_ {
    private val store = mutableMapOf<Long, GenModel_461_>()
    override suspend fun getAll(): List<GenModel_461_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_461_? = store[id]
    override suspend fun save(model: GenModel_461_): GenModel_461_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_461_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_461_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_461_ @Inject constructor(
    private val repository: GenRepositoryImpl_461_
) : GenUseCase_461_<Unit, List<GenModel_461_>> {
    override suspend fun invoke(params: Unit): List<GenModel_461_> = repository.getAll()
}

class GenSaveUseCase_461_ @Inject constructor(
    private val repository: GenRepositoryImpl_461_
) : GenUseCase_461_<GenModel_461_, GenModel_461_> {
    override suspend fun invoke(params: GenModel_461_): GenModel_461_ = repository.save(params)
}

class GenDeleteUseCase_461_ @Inject constructor(
    private val repository: GenRepositoryImpl_461_
) : GenUseCase_461_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_461_ @Inject constructor(
    private val repository: GenRepositoryImpl_461_
) : GenUseCase_461_<String, List<GenModel_461_>> {
    override suspend fun invoke(params: String): List<GenModel_461_> = repository.search(params)
}

abstract class GenMapper_461_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_461_ : GenMapper_461_<GenModel_461_, String>() {
    override fun map(input: GenModel_461_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_461_ : GenMapper_461_<String, GenModel_461_>() {
    override fun map(input: String): GenModel_461_ {
        val parts = input.split(":")
        return GenModel_461_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_461_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_461_,
    private val saveUseCase: GenSaveUseCase_461_,
    private val deleteUseCase: GenDeleteUseCase_461_,
    private val searchUseCase: GenSearchUseCase_461_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_461_>(GenState_461_.Idle)
    val state: StateFlow<GenState_461_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_461_) {
        when (event) {
            is GenEvent_461_.Load -> loadAll()
            is GenEvent_461_.Update -> save(event.model)
            is GenEvent_461_.Delete -> delete(event.id)
            is GenEvent_461_.Refresh -> loadAll()
            is GenEvent_461_.Search -> search(event.query)
            is GenEvent_461_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_461_.Loading; _state.value = GenState_461_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_461_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_461_.Success(searchUseCase(query)) } }
}
