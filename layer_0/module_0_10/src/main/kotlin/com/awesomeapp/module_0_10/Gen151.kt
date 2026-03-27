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

data class GenModel_151_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_151_ {
    data class Load(val id: Long) : GenEvent_151_()
    data class Update(val model: GenModel_151_) : GenEvent_151_()
    data class Delete(val id: Long) : GenEvent_151_()
    data object Refresh : GenEvent_151_()
    data class Search(val query: String) : GenEvent_151_()
    data class Filter(val predicate: String) : GenEvent_151_()
}

sealed class GenState_151_ {
    data object Idle : GenState_151_()
    data object Loading : GenState_151_()
    data class Success(val items: List<GenModel_151_>) : GenState_151_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_151_()
    data class Partial(val items: List<GenModel_151_>, val hasMore: Boolean) : GenState_151_()
}

interface GenRepository_151_ {
    suspend fun getAll(): List<GenModel_151_>
    suspend fun getById(id: Long): GenModel_151_?
    suspend fun save(model: GenModel_151_): GenModel_151_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_151_>
}

@Singleton
class GenRepositoryImpl_151_ @Inject constructor() : GenRepository_151_ {
    private val store = mutableMapOf<Long, GenModel_151_>()
    override suspend fun getAll(): List<GenModel_151_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_151_? = store[id]
    override suspend fun save(model: GenModel_151_): GenModel_151_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_151_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_151_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_151_ @Inject constructor(
    private val repository: GenRepositoryImpl_151_
) : GenUseCase_151_<Unit, List<GenModel_151_>> {
    override suspend fun invoke(params: Unit): List<GenModel_151_> = repository.getAll()
}

class GenSaveUseCase_151_ @Inject constructor(
    private val repository: GenRepositoryImpl_151_
) : GenUseCase_151_<GenModel_151_, GenModel_151_> {
    override suspend fun invoke(params: GenModel_151_): GenModel_151_ = repository.save(params)
}

class GenDeleteUseCase_151_ @Inject constructor(
    private val repository: GenRepositoryImpl_151_
) : GenUseCase_151_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_151_ @Inject constructor(
    private val repository: GenRepositoryImpl_151_
) : GenUseCase_151_<String, List<GenModel_151_>> {
    override suspend fun invoke(params: String): List<GenModel_151_> = repository.search(params)
}

abstract class GenMapper_151_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_151_ : GenMapper_151_<GenModel_151_, String>() {
    override fun map(input: GenModel_151_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_151_ : GenMapper_151_<String, GenModel_151_>() {
    override fun map(input: String): GenModel_151_ {
        val parts = input.split(":")
        return GenModel_151_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_151_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_151_,
    private val saveUseCase: GenSaveUseCase_151_,
    private val deleteUseCase: GenDeleteUseCase_151_,
    private val searchUseCase: GenSearchUseCase_151_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_151_>(GenState_151_.Idle)
    val state: StateFlow<GenState_151_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_151_) {
        when (event) {
            is GenEvent_151_.Load -> loadAll()
            is GenEvent_151_.Update -> save(event.model)
            is GenEvent_151_.Delete -> delete(event.id)
            is GenEvent_151_.Refresh -> loadAll()
            is GenEvent_151_.Search -> search(event.query)
            is GenEvent_151_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_151_.Loading; _state.value = GenState_151_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_151_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_151_.Success(searchUseCase(query)) } }
}
