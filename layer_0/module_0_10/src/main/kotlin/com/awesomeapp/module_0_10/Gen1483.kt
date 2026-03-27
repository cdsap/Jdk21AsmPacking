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

data class GenModel_1483_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1483_ {
    data class Load(val id: Long) : GenEvent_1483_()
    data class Update(val model: GenModel_1483_) : GenEvent_1483_()
    data class Delete(val id: Long) : GenEvent_1483_()
    data object Refresh : GenEvent_1483_()
    data class Search(val query: String) : GenEvent_1483_()
    data class Filter(val predicate: String) : GenEvent_1483_()
}

sealed class GenState_1483_ {
    data object Idle : GenState_1483_()
    data object Loading : GenState_1483_()
    data class Success(val items: List<GenModel_1483_>) : GenState_1483_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1483_()
    data class Partial(val items: List<GenModel_1483_>, val hasMore: Boolean) : GenState_1483_()
}

interface GenRepository_1483_ {
    suspend fun getAll(): List<GenModel_1483_>
    suspend fun getById(id: Long): GenModel_1483_?
    suspend fun save(model: GenModel_1483_): GenModel_1483_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1483_>
}

@Singleton
class GenRepositoryImpl_1483_ @Inject constructor() : GenRepository_1483_ {
    private val store = mutableMapOf<Long, GenModel_1483_>()
    override suspend fun getAll(): List<GenModel_1483_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1483_? = store[id]
    override suspend fun save(model: GenModel_1483_): GenModel_1483_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1483_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1483_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1483_ @Inject constructor(
    private val repository: GenRepositoryImpl_1483_
) : GenUseCase_1483_<Unit, List<GenModel_1483_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1483_> = repository.getAll()
}

class GenSaveUseCase_1483_ @Inject constructor(
    private val repository: GenRepositoryImpl_1483_
) : GenUseCase_1483_<GenModel_1483_, GenModel_1483_> {
    override suspend fun invoke(params: GenModel_1483_): GenModel_1483_ = repository.save(params)
}

class GenDeleteUseCase_1483_ @Inject constructor(
    private val repository: GenRepositoryImpl_1483_
) : GenUseCase_1483_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1483_ @Inject constructor(
    private val repository: GenRepositoryImpl_1483_
) : GenUseCase_1483_<String, List<GenModel_1483_>> {
    override suspend fun invoke(params: String): List<GenModel_1483_> = repository.search(params)
}

abstract class GenMapper_1483_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1483_ : GenMapper_1483_<GenModel_1483_, String>() {
    override fun map(input: GenModel_1483_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1483_ : GenMapper_1483_<String, GenModel_1483_>() {
    override fun map(input: String): GenModel_1483_ {
        val parts = input.split(":")
        return GenModel_1483_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1483_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1483_,
    private val saveUseCase: GenSaveUseCase_1483_,
    private val deleteUseCase: GenDeleteUseCase_1483_,
    private val searchUseCase: GenSearchUseCase_1483_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1483_>(GenState_1483_.Idle)
    val state: StateFlow<GenState_1483_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1483_) {
        when (event) {
            is GenEvent_1483_.Load -> loadAll()
            is GenEvent_1483_.Update -> save(event.model)
            is GenEvent_1483_.Delete -> delete(event.id)
            is GenEvent_1483_.Refresh -> loadAll()
            is GenEvent_1483_.Search -> search(event.query)
            is GenEvent_1483_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1483_.Loading; _state.value = GenState_1483_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1483_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1483_.Success(searchUseCase(query)) } }
}
