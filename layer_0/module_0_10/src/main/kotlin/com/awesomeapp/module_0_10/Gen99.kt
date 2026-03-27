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

data class GenModel_99_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_99_ {
    data class Load(val id: Long) : GenEvent_99_()
    data class Update(val model: GenModel_99_) : GenEvent_99_()
    data class Delete(val id: Long) : GenEvent_99_()
    data object Refresh : GenEvent_99_()
    data class Search(val query: String) : GenEvent_99_()
    data class Filter(val predicate: String) : GenEvent_99_()
}

sealed class GenState_99_ {
    data object Idle : GenState_99_()
    data object Loading : GenState_99_()
    data class Success(val items: List<GenModel_99_>) : GenState_99_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_99_()
    data class Partial(val items: List<GenModel_99_>, val hasMore: Boolean) : GenState_99_()
}

interface GenRepository_99_ {
    suspend fun getAll(): List<GenModel_99_>
    suspend fun getById(id: Long): GenModel_99_?
    suspend fun save(model: GenModel_99_): GenModel_99_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_99_>
}

@Singleton
class GenRepositoryImpl_99_ @Inject constructor() : GenRepository_99_ {
    private val store = mutableMapOf<Long, GenModel_99_>()
    override suspend fun getAll(): List<GenModel_99_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_99_? = store[id]
    override suspend fun save(model: GenModel_99_): GenModel_99_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_99_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_99_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_99_ @Inject constructor(
    private val repository: GenRepositoryImpl_99_
) : GenUseCase_99_<Unit, List<GenModel_99_>> {
    override suspend fun invoke(params: Unit): List<GenModel_99_> = repository.getAll()
}

class GenSaveUseCase_99_ @Inject constructor(
    private val repository: GenRepositoryImpl_99_
) : GenUseCase_99_<GenModel_99_, GenModel_99_> {
    override suspend fun invoke(params: GenModel_99_): GenModel_99_ = repository.save(params)
}

class GenDeleteUseCase_99_ @Inject constructor(
    private val repository: GenRepositoryImpl_99_
) : GenUseCase_99_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_99_ @Inject constructor(
    private val repository: GenRepositoryImpl_99_
) : GenUseCase_99_<String, List<GenModel_99_>> {
    override suspend fun invoke(params: String): List<GenModel_99_> = repository.search(params)
}

abstract class GenMapper_99_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_99_ : GenMapper_99_<GenModel_99_, String>() {
    override fun map(input: GenModel_99_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_99_ : GenMapper_99_<String, GenModel_99_>() {
    override fun map(input: String): GenModel_99_ {
        val parts = input.split(":")
        return GenModel_99_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_99_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_99_,
    private val saveUseCase: GenSaveUseCase_99_,
    private val deleteUseCase: GenDeleteUseCase_99_,
    private val searchUseCase: GenSearchUseCase_99_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_99_>(GenState_99_.Idle)
    val state: StateFlow<GenState_99_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_99_) {
        when (event) {
            is GenEvent_99_.Load -> loadAll()
            is GenEvent_99_.Update -> save(event.model)
            is GenEvent_99_.Delete -> delete(event.id)
            is GenEvent_99_.Refresh -> loadAll()
            is GenEvent_99_.Search -> search(event.query)
            is GenEvent_99_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_99_.Loading; _state.value = GenState_99_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_99_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_99_.Success(searchUseCase(query)) } }
}
