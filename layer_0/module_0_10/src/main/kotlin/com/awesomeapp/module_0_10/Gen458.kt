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

data class GenModel_458_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_458_ {
    data class Load(val id: Long) : GenEvent_458_()
    data class Update(val model: GenModel_458_) : GenEvent_458_()
    data class Delete(val id: Long) : GenEvent_458_()
    data object Refresh : GenEvent_458_()
    data class Search(val query: String) : GenEvent_458_()
    data class Filter(val predicate: String) : GenEvent_458_()
}

sealed class GenState_458_ {
    data object Idle : GenState_458_()
    data object Loading : GenState_458_()
    data class Success(val items: List<GenModel_458_>) : GenState_458_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_458_()
    data class Partial(val items: List<GenModel_458_>, val hasMore: Boolean) : GenState_458_()
}

interface GenRepository_458_ {
    suspend fun getAll(): List<GenModel_458_>
    suspend fun getById(id: Long): GenModel_458_?
    suspend fun save(model: GenModel_458_): GenModel_458_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_458_>
}

@Singleton
class GenRepositoryImpl_458_ @Inject constructor() : GenRepository_458_ {
    private val store = mutableMapOf<Long, GenModel_458_>()
    override suspend fun getAll(): List<GenModel_458_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_458_? = store[id]
    override suspend fun save(model: GenModel_458_): GenModel_458_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_458_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_458_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_458_ @Inject constructor(
    private val repository: GenRepositoryImpl_458_
) : GenUseCase_458_<Unit, List<GenModel_458_>> {
    override suspend fun invoke(params: Unit): List<GenModel_458_> = repository.getAll()
}

class GenSaveUseCase_458_ @Inject constructor(
    private val repository: GenRepositoryImpl_458_
) : GenUseCase_458_<GenModel_458_, GenModel_458_> {
    override suspend fun invoke(params: GenModel_458_): GenModel_458_ = repository.save(params)
}

class GenDeleteUseCase_458_ @Inject constructor(
    private val repository: GenRepositoryImpl_458_
) : GenUseCase_458_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_458_ @Inject constructor(
    private val repository: GenRepositoryImpl_458_
) : GenUseCase_458_<String, List<GenModel_458_>> {
    override suspend fun invoke(params: String): List<GenModel_458_> = repository.search(params)
}

abstract class GenMapper_458_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_458_ : GenMapper_458_<GenModel_458_, String>() {
    override fun map(input: GenModel_458_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_458_ : GenMapper_458_<String, GenModel_458_>() {
    override fun map(input: String): GenModel_458_ {
        val parts = input.split(":")
        return GenModel_458_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_458_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_458_,
    private val saveUseCase: GenSaveUseCase_458_,
    private val deleteUseCase: GenDeleteUseCase_458_,
    private val searchUseCase: GenSearchUseCase_458_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_458_>(GenState_458_.Idle)
    val state: StateFlow<GenState_458_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_458_) {
        when (event) {
            is GenEvent_458_.Load -> loadAll()
            is GenEvent_458_.Update -> save(event.model)
            is GenEvent_458_.Delete -> delete(event.id)
            is GenEvent_458_.Refresh -> loadAll()
            is GenEvent_458_.Search -> search(event.query)
            is GenEvent_458_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_458_.Loading; _state.value = GenState_458_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_458_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_458_.Success(searchUseCase(query)) } }
}
