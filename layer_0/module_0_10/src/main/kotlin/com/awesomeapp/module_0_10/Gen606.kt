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

data class GenModel_606_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_606_ {
    data class Load(val id: Long) : GenEvent_606_()
    data class Update(val model: GenModel_606_) : GenEvent_606_()
    data class Delete(val id: Long) : GenEvent_606_()
    data object Refresh : GenEvent_606_()
    data class Search(val query: String) : GenEvent_606_()
    data class Filter(val predicate: String) : GenEvent_606_()
}

sealed class GenState_606_ {
    data object Idle : GenState_606_()
    data object Loading : GenState_606_()
    data class Success(val items: List<GenModel_606_>) : GenState_606_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_606_()
    data class Partial(val items: List<GenModel_606_>, val hasMore: Boolean) : GenState_606_()
}

interface GenRepository_606_ {
    suspend fun getAll(): List<GenModel_606_>
    suspend fun getById(id: Long): GenModel_606_?
    suspend fun save(model: GenModel_606_): GenModel_606_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_606_>
}

@Singleton
class GenRepositoryImpl_606_ @Inject constructor() : GenRepository_606_ {
    private val store = mutableMapOf<Long, GenModel_606_>()
    override suspend fun getAll(): List<GenModel_606_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_606_? = store[id]
    override suspend fun save(model: GenModel_606_): GenModel_606_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_606_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_606_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_606_ @Inject constructor(
    private val repository: GenRepositoryImpl_606_
) : GenUseCase_606_<Unit, List<GenModel_606_>> {
    override suspend fun invoke(params: Unit): List<GenModel_606_> = repository.getAll()
}

class GenSaveUseCase_606_ @Inject constructor(
    private val repository: GenRepositoryImpl_606_
) : GenUseCase_606_<GenModel_606_, GenModel_606_> {
    override suspend fun invoke(params: GenModel_606_): GenModel_606_ = repository.save(params)
}

class GenDeleteUseCase_606_ @Inject constructor(
    private val repository: GenRepositoryImpl_606_
) : GenUseCase_606_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_606_ @Inject constructor(
    private val repository: GenRepositoryImpl_606_
) : GenUseCase_606_<String, List<GenModel_606_>> {
    override suspend fun invoke(params: String): List<GenModel_606_> = repository.search(params)
}

abstract class GenMapper_606_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_606_ : GenMapper_606_<GenModel_606_, String>() {
    override fun map(input: GenModel_606_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_606_ : GenMapper_606_<String, GenModel_606_>() {
    override fun map(input: String): GenModel_606_ {
        val parts = input.split(":")
        return GenModel_606_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_606_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_606_,
    private val saveUseCase: GenSaveUseCase_606_,
    private val deleteUseCase: GenDeleteUseCase_606_,
    private val searchUseCase: GenSearchUseCase_606_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_606_>(GenState_606_.Idle)
    val state: StateFlow<GenState_606_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_606_) {
        when (event) {
            is GenEvent_606_.Load -> loadAll()
            is GenEvent_606_.Update -> save(event.model)
            is GenEvent_606_.Delete -> delete(event.id)
            is GenEvent_606_.Refresh -> loadAll()
            is GenEvent_606_.Search -> search(event.query)
            is GenEvent_606_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_606_.Loading; _state.value = GenState_606_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_606_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_606_.Success(searchUseCase(query)) } }
}
