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

data class GenModel_483_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_483_ {
    data class Load(val id: Long) : GenEvent_483_()
    data class Update(val model: GenModel_483_) : GenEvent_483_()
    data class Delete(val id: Long) : GenEvent_483_()
    data object Refresh : GenEvent_483_()
    data class Search(val query: String) : GenEvent_483_()
    data class Filter(val predicate: String) : GenEvent_483_()
}

sealed class GenState_483_ {
    data object Idle : GenState_483_()
    data object Loading : GenState_483_()
    data class Success(val items: List<GenModel_483_>) : GenState_483_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_483_()
    data class Partial(val items: List<GenModel_483_>, val hasMore: Boolean) : GenState_483_()
}

interface GenRepository_483_ {
    suspend fun getAll(): List<GenModel_483_>
    suspend fun getById(id: Long): GenModel_483_?
    suspend fun save(model: GenModel_483_): GenModel_483_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_483_>
}

@Singleton
class GenRepositoryImpl_483_ @Inject constructor() : GenRepository_483_ {
    private val store = mutableMapOf<Long, GenModel_483_>()
    override suspend fun getAll(): List<GenModel_483_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_483_? = store[id]
    override suspend fun save(model: GenModel_483_): GenModel_483_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_483_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_483_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_483_ @Inject constructor(
    private val repository: GenRepositoryImpl_483_
) : GenUseCase_483_<Unit, List<GenModel_483_>> {
    override suspend fun invoke(params: Unit): List<GenModel_483_> = repository.getAll()
}

class GenSaveUseCase_483_ @Inject constructor(
    private val repository: GenRepositoryImpl_483_
) : GenUseCase_483_<GenModel_483_, GenModel_483_> {
    override suspend fun invoke(params: GenModel_483_): GenModel_483_ = repository.save(params)
}

class GenDeleteUseCase_483_ @Inject constructor(
    private val repository: GenRepositoryImpl_483_
) : GenUseCase_483_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_483_ @Inject constructor(
    private val repository: GenRepositoryImpl_483_
) : GenUseCase_483_<String, List<GenModel_483_>> {
    override suspend fun invoke(params: String): List<GenModel_483_> = repository.search(params)
}

abstract class GenMapper_483_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_483_ : GenMapper_483_<GenModel_483_, String>() {
    override fun map(input: GenModel_483_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_483_ : GenMapper_483_<String, GenModel_483_>() {
    override fun map(input: String): GenModel_483_ {
        val parts = input.split(":")
        return GenModel_483_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_483_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_483_,
    private val saveUseCase: GenSaveUseCase_483_,
    private val deleteUseCase: GenDeleteUseCase_483_,
    private val searchUseCase: GenSearchUseCase_483_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_483_>(GenState_483_.Idle)
    val state: StateFlow<GenState_483_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_483_) {
        when (event) {
            is GenEvent_483_.Load -> loadAll()
            is GenEvent_483_.Update -> save(event.model)
            is GenEvent_483_.Delete -> delete(event.id)
            is GenEvent_483_.Refresh -> loadAll()
            is GenEvent_483_.Search -> search(event.query)
            is GenEvent_483_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_483_.Loading; _state.value = GenState_483_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_483_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_483_.Success(searchUseCase(query)) } }
}
