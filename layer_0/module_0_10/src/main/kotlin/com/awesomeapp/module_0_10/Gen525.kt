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

data class GenModel_525_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_525_ {
    data class Load(val id: Long) : GenEvent_525_()
    data class Update(val model: GenModel_525_) : GenEvent_525_()
    data class Delete(val id: Long) : GenEvent_525_()
    data object Refresh : GenEvent_525_()
    data class Search(val query: String) : GenEvent_525_()
    data class Filter(val predicate: String) : GenEvent_525_()
}

sealed class GenState_525_ {
    data object Idle : GenState_525_()
    data object Loading : GenState_525_()
    data class Success(val items: List<GenModel_525_>) : GenState_525_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_525_()
    data class Partial(val items: List<GenModel_525_>, val hasMore: Boolean) : GenState_525_()
}

interface GenRepository_525_ {
    suspend fun getAll(): List<GenModel_525_>
    suspend fun getById(id: Long): GenModel_525_?
    suspend fun save(model: GenModel_525_): GenModel_525_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_525_>
}

@Singleton
class GenRepositoryImpl_525_ @Inject constructor() : GenRepository_525_ {
    private val store = mutableMapOf<Long, GenModel_525_>()
    override suspend fun getAll(): List<GenModel_525_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_525_? = store[id]
    override suspend fun save(model: GenModel_525_): GenModel_525_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_525_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_525_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_525_ @Inject constructor(
    private val repository: GenRepositoryImpl_525_
) : GenUseCase_525_<Unit, List<GenModel_525_>> {
    override suspend fun invoke(params: Unit): List<GenModel_525_> = repository.getAll()
}

class GenSaveUseCase_525_ @Inject constructor(
    private val repository: GenRepositoryImpl_525_
) : GenUseCase_525_<GenModel_525_, GenModel_525_> {
    override suspend fun invoke(params: GenModel_525_): GenModel_525_ = repository.save(params)
}

class GenDeleteUseCase_525_ @Inject constructor(
    private val repository: GenRepositoryImpl_525_
) : GenUseCase_525_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_525_ @Inject constructor(
    private val repository: GenRepositoryImpl_525_
) : GenUseCase_525_<String, List<GenModel_525_>> {
    override suspend fun invoke(params: String): List<GenModel_525_> = repository.search(params)
}

abstract class GenMapper_525_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_525_ : GenMapper_525_<GenModel_525_, String>() {
    override fun map(input: GenModel_525_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_525_ : GenMapper_525_<String, GenModel_525_>() {
    override fun map(input: String): GenModel_525_ {
        val parts = input.split(":")
        return GenModel_525_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_525_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_525_,
    private val saveUseCase: GenSaveUseCase_525_,
    private val deleteUseCase: GenDeleteUseCase_525_,
    private val searchUseCase: GenSearchUseCase_525_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_525_>(GenState_525_.Idle)
    val state: StateFlow<GenState_525_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_525_) {
        when (event) {
            is GenEvent_525_.Load -> loadAll()
            is GenEvent_525_.Update -> save(event.model)
            is GenEvent_525_.Delete -> delete(event.id)
            is GenEvent_525_.Refresh -> loadAll()
            is GenEvent_525_.Search -> search(event.query)
            is GenEvent_525_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_525_.Loading; _state.value = GenState_525_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_525_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_525_.Success(searchUseCase(query)) } }
}
