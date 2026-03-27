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

data class GenModel_60_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_60_ {
    data class Load(val id: Long) : GenEvent_60_()
    data class Update(val model: GenModel_60_) : GenEvent_60_()
    data class Delete(val id: Long) : GenEvent_60_()
    data object Refresh : GenEvent_60_()
    data class Search(val query: String) : GenEvent_60_()
    data class Filter(val predicate: String) : GenEvent_60_()
}

sealed class GenState_60_ {
    data object Idle : GenState_60_()
    data object Loading : GenState_60_()
    data class Success(val items: List<GenModel_60_>) : GenState_60_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_60_()
    data class Partial(val items: List<GenModel_60_>, val hasMore: Boolean) : GenState_60_()
}

interface GenRepository_60_ {
    suspend fun getAll(): List<GenModel_60_>
    suspend fun getById(id: Long): GenModel_60_?
    suspend fun save(model: GenModel_60_): GenModel_60_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_60_>
}

@Singleton
class GenRepositoryImpl_60_ @Inject constructor() : GenRepository_60_ {
    private val store = mutableMapOf<Long, GenModel_60_>()
    override suspend fun getAll(): List<GenModel_60_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_60_? = store[id]
    override suspend fun save(model: GenModel_60_): GenModel_60_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_60_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_60_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_60_ @Inject constructor(
    private val repository: GenRepositoryImpl_60_
) : GenUseCase_60_<Unit, List<GenModel_60_>> {
    override suspend fun invoke(params: Unit): List<GenModel_60_> = repository.getAll()
}

class GenSaveUseCase_60_ @Inject constructor(
    private val repository: GenRepositoryImpl_60_
) : GenUseCase_60_<GenModel_60_, GenModel_60_> {
    override suspend fun invoke(params: GenModel_60_): GenModel_60_ = repository.save(params)
}

class GenDeleteUseCase_60_ @Inject constructor(
    private val repository: GenRepositoryImpl_60_
) : GenUseCase_60_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_60_ @Inject constructor(
    private val repository: GenRepositoryImpl_60_
) : GenUseCase_60_<String, List<GenModel_60_>> {
    override suspend fun invoke(params: String): List<GenModel_60_> = repository.search(params)
}

abstract class GenMapper_60_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_60_ : GenMapper_60_<GenModel_60_, String>() {
    override fun map(input: GenModel_60_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_60_ : GenMapper_60_<String, GenModel_60_>() {
    override fun map(input: String): GenModel_60_ {
        val parts = input.split(":")
        return GenModel_60_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_60_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_60_,
    private val saveUseCase: GenSaveUseCase_60_,
    private val deleteUseCase: GenDeleteUseCase_60_,
    private val searchUseCase: GenSearchUseCase_60_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_60_>(GenState_60_.Idle)
    val state: StateFlow<GenState_60_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_60_) {
        when (event) {
            is GenEvent_60_.Load -> loadAll()
            is GenEvent_60_.Update -> save(event.model)
            is GenEvent_60_.Delete -> delete(event.id)
            is GenEvent_60_.Refresh -> loadAll()
            is GenEvent_60_.Search -> search(event.query)
            is GenEvent_60_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_60_.Loading; _state.value = GenState_60_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_60_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_60_.Success(searchUseCase(query)) } }
}
