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

data class GenModel_3458_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3458_ {
    data class Load(val id: Long) : GenEvent_3458_()
    data class Update(val model: GenModel_3458_) : GenEvent_3458_()
    data class Delete(val id: Long) : GenEvent_3458_()
    data object Refresh : GenEvent_3458_()
    data class Search(val query: String) : GenEvent_3458_()
    data class Filter(val predicate: String) : GenEvent_3458_()
}

sealed class GenState_3458_ {
    data object Idle : GenState_3458_()
    data object Loading : GenState_3458_()
    data class Success(val items: List<GenModel_3458_>) : GenState_3458_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3458_()
    data class Partial(val items: List<GenModel_3458_>, val hasMore: Boolean) : GenState_3458_()
}

interface GenRepository_3458_ {
    suspend fun getAll(): List<GenModel_3458_>
    suspend fun getById(id: Long): GenModel_3458_?
    suspend fun save(model: GenModel_3458_): GenModel_3458_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3458_>
}

@Singleton
class GenRepositoryImpl_3458_ @Inject constructor() : GenRepository_3458_ {
    private val store = mutableMapOf<Long, GenModel_3458_>()
    override suspend fun getAll(): List<GenModel_3458_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3458_? = store[id]
    override suspend fun save(model: GenModel_3458_): GenModel_3458_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3458_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3458_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3458_ @Inject constructor(
    private val repository: GenRepositoryImpl_3458_
) : GenUseCase_3458_<Unit, List<GenModel_3458_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3458_> = repository.getAll()
}

class GenSaveUseCase_3458_ @Inject constructor(
    private val repository: GenRepositoryImpl_3458_
) : GenUseCase_3458_<GenModel_3458_, GenModel_3458_> {
    override suspend fun invoke(params: GenModel_3458_): GenModel_3458_ = repository.save(params)
}

class GenDeleteUseCase_3458_ @Inject constructor(
    private val repository: GenRepositoryImpl_3458_
) : GenUseCase_3458_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3458_ @Inject constructor(
    private val repository: GenRepositoryImpl_3458_
) : GenUseCase_3458_<String, List<GenModel_3458_>> {
    override suspend fun invoke(params: String): List<GenModel_3458_> = repository.search(params)
}

abstract class GenMapper_3458_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3458_ : GenMapper_3458_<GenModel_3458_, String>() {
    override fun map(input: GenModel_3458_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3458_ : GenMapper_3458_<String, GenModel_3458_>() {
    override fun map(input: String): GenModel_3458_ {
        val parts = input.split(":")
        return GenModel_3458_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3458_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3458_,
    private val saveUseCase: GenSaveUseCase_3458_,
    private val deleteUseCase: GenDeleteUseCase_3458_,
    private val searchUseCase: GenSearchUseCase_3458_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3458_>(GenState_3458_.Idle)
    val state: StateFlow<GenState_3458_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3458_) {
        when (event) {
            is GenEvent_3458_.Load -> loadAll()
            is GenEvent_3458_.Update -> save(event.model)
            is GenEvent_3458_.Delete -> delete(event.id)
            is GenEvent_3458_.Refresh -> loadAll()
            is GenEvent_3458_.Search -> search(event.query)
            is GenEvent_3458_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3458_.Loading; _state.value = GenState_3458_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3458_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3458_.Success(searchUseCase(query)) } }
}
