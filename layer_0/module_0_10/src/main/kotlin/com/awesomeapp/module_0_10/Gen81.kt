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

data class GenModel_81_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_81_ {
    data class Load(val id: Long) : GenEvent_81_()
    data class Update(val model: GenModel_81_) : GenEvent_81_()
    data class Delete(val id: Long) : GenEvent_81_()
    data object Refresh : GenEvent_81_()
    data class Search(val query: String) : GenEvent_81_()
    data class Filter(val predicate: String) : GenEvent_81_()
}

sealed class GenState_81_ {
    data object Idle : GenState_81_()
    data object Loading : GenState_81_()
    data class Success(val items: List<GenModel_81_>) : GenState_81_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_81_()
    data class Partial(val items: List<GenModel_81_>, val hasMore: Boolean) : GenState_81_()
}

interface GenRepository_81_ {
    suspend fun getAll(): List<GenModel_81_>
    suspend fun getById(id: Long): GenModel_81_?
    suspend fun save(model: GenModel_81_): GenModel_81_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_81_>
}

@Singleton
class GenRepositoryImpl_81_ @Inject constructor() : GenRepository_81_ {
    private val store = mutableMapOf<Long, GenModel_81_>()
    override suspend fun getAll(): List<GenModel_81_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_81_? = store[id]
    override suspend fun save(model: GenModel_81_): GenModel_81_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_81_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_81_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_81_ @Inject constructor(
    private val repository: GenRepositoryImpl_81_
) : GenUseCase_81_<Unit, List<GenModel_81_>> {
    override suspend fun invoke(params: Unit): List<GenModel_81_> = repository.getAll()
}

class GenSaveUseCase_81_ @Inject constructor(
    private val repository: GenRepositoryImpl_81_
) : GenUseCase_81_<GenModel_81_, GenModel_81_> {
    override suspend fun invoke(params: GenModel_81_): GenModel_81_ = repository.save(params)
}

class GenDeleteUseCase_81_ @Inject constructor(
    private val repository: GenRepositoryImpl_81_
) : GenUseCase_81_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_81_ @Inject constructor(
    private val repository: GenRepositoryImpl_81_
) : GenUseCase_81_<String, List<GenModel_81_>> {
    override suspend fun invoke(params: String): List<GenModel_81_> = repository.search(params)
}

abstract class GenMapper_81_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_81_ : GenMapper_81_<GenModel_81_, String>() {
    override fun map(input: GenModel_81_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_81_ : GenMapper_81_<String, GenModel_81_>() {
    override fun map(input: String): GenModel_81_ {
        val parts = input.split(":")
        return GenModel_81_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_81_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_81_,
    private val saveUseCase: GenSaveUseCase_81_,
    private val deleteUseCase: GenDeleteUseCase_81_,
    private val searchUseCase: GenSearchUseCase_81_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_81_>(GenState_81_.Idle)
    val state: StateFlow<GenState_81_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_81_) {
        when (event) {
            is GenEvent_81_.Load -> loadAll()
            is GenEvent_81_.Update -> save(event.model)
            is GenEvent_81_.Delete -> delete(event.id)
            is GenEvent_81_.Refresh -> loadAll()
            is GenEvent_81_.Search -> search(event.query)
            is GenEvent_81_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_81_.Loading; _state.value = GenState_81_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_81_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_81_.Success(searchUseCase(query)) } }
}
