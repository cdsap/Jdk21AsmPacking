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

data class GenModel_974_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_974_ {
    data class Load(val id: Long) : GenEvent_974_()
    data class Update(val model: GenModel_974_) : GenEvent_974_()
    data class Delete(val id: Long) : GenEvent_974_()
    data object Refresh : GenEvent_974_()
    data class Search(val query: String) : GenEvent_974_()
    data class Filter(val predicate: String) : GenEvent_974_()
}

sealed class GenState_974_ {
    data object Idle : GenState_974_()
    data object Loading : GenState_974_()
    data class Success(val items: List<GenModel_974_>) : GenState_974_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_974_()
    data class Partial(val items: List<GenModel_974_>, val hasMore: Boolean) : GenState_974_()
}

interface GenRepository_974_ {
    suspend fun getAll(): List<GenModel_974_>
    suspend fun getById(id: Long): GenModel_974_?
    suspend fun save(model: GenModel_974_): GenModel_974_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_974_>
}

@Singleton
class GenRepositoryImpl_974_ @Inject constructor() : GenRepository_974_ {
    private val store = mutableMapOf<Long, GenModel_974_>()
    override suspend fun getAll(): List<GenModel_974_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_974_? = store[id]
    override suspend fun save(model: GenModel_974_): GenModel_974_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_974_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_974_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_974_ @Inject constructor(
    private val repository: GenRepositoryImpl_974_
) : GenUseCase_974_<Unit, List<GenModel_974_>> {
    override suspend fun invoke(params: Unit): List<GenModel_974_> = repository.getAll()
}

class GenSaveUseCase_974_ @Inject constructor(
    private val repository: GenRepositoryImpl_974_
) : GenUseCase_974_<GenModel_974_, GenModel_974_> {
    override suspend fun invoke(params: GenModel_974_): GenModel_974_ = repository.save(params)
}

class GenDeleteUseCase_974_ @Inject constructor(
    private val repository: GenRepositoryImpl_974_
) : GenUseCase_974_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_974_ @Inject constructor(
    private val repository: GenRepositoryImpl_974_
) : GenUseCase_974_<String, List<GenModel_974_>> {
    override suspend fun invoke(params: String): List<GenModel_974_> = repository.search(params)
}

abstract class GenMapper_974_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_974_ : GenMapper_974_<GenModel_974_, String>() {
    override fun map(input: GenModel_974_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_974_ : GenMapper_974_<String, GenModel_974_>() {
    override fun map(input: String): GenModel_974_ {
        val parts = input.split(":")
        return GenModel_974_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_974_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_974_,
    private val saveUseCase: GenSaveUseCase_974_,
    private val deleteUseCase: GenDeleteUseCase_974_,
    private val searchUseCase: GenSearchUseCase_974_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_974_>(GenState_974_.Idle)
    val state: StateFlow<GenState_974_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_974_) {
        when (event) {
            is GenEvent_974_.Load -> loadAll()
            is GenEvent_974_.Update -> save(event.model)
            is GenEvent_974_.Delete -> delete(event.id)
            is GenEvent_974_.Refresh -> loadAll()
            is GenEvent_974_.Search -> search(event.query)
            is GenEvent_974_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_974_.Loading; _state.value = GenState_974_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_974_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_974_.Success(searchUseCase(query)) } }
}
