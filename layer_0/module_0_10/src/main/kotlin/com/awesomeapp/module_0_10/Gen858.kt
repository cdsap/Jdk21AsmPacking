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

data class GenModel_858_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_858_ {
    data class Load(val id: Long) : GenEvent_858_()
    data class Update(val model: GenModel_858_) : GenEvent_858_()
    data class Delete(val id: Long) : GenEvent_858_()
    data object Refresh : GenEvent_858_()
    data class Search(val query: String) : GenEvent_858_()
    data class Filter(val predicate: String) : GenEvent_858_()
}

sealed class GenState_858_ {
    data object Idle : GenState_858_()
    data object Loading : GenState_858_()
    data class Success(val items: List<GenModel_858_>) : GenState_858_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_858_()
    data class Partial(val items: List<GenModel_858_>, val hasMore: Boolean) : GenState_858_()
}

interface GenRepository_858_ {
    suspend fun getAll(): List<GenModel_858_>
    suspend fun getById(id: Long): GenModel_858_?
    suspend fun save(model: GenModel_858_): GenModel_858_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_858_>
}

@Singleton
class GenRepositoryImpl_858_ @Inject constructor() : GenRepository_858_ {
    private val store = mutableMapOf<Long, GenModel_858_>()
    override suspend fun getAll(): List<GenModel_858_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_858_? = store[id]
    override suspend fun save(model: GenModel_858_): GenModel_858_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_858_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_858_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_858_ @Inject constructor(
    private val repository: GenRepositoryImpl_858_
) : GenUseCase_858_<Unit, List<GenModel_858_>> {
    override suspend fun invoke(params: Unit): List<GenModel_858_> = repository.getAll()
}

class GenSaveUseCase_858_ @Inject constructor(
    private val repository: GenRepositoryImpl_858_
) : GenUseCase_858_<GenModel_858_, GenModel_858_> {
    override suspend fun invoke(params: GenModel_858_): GenModel_858_ = repository.save(params)
}

class GenDeleteUseCase_858_ @Inject constructor(
    private val repository: GenRepositoryImpl_858_
) : GenUseCase_858_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_858_ @Inject constructor(
    private val repository: GenRepositoryImpl_858_
) : GenUseCase_858_<String, List<GenModel_858_>> {
    override suspend fun invoke(params: String): List<GenModel_858_> = repository.search(params)
}

abstract class GenMapper_858_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_858_ : GenMapper_858_<GenModel_858_, String>() {
    override fun map(input: GenModel_858_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_858_ : GenMapper_858_<String, GenModel_858_>() {
    override fun map(input: String): GenModel_858_ {
        val parts = input.split(":")
        return GenModel_858_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_858_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_858_,
    private val saveUseCase: GenSaveUseCase_858_,
    private val deleteUseCase: GenDeleteUseCase_858_,
    private val searchUseCase: GenSearchUseCase_858_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_858_>(GenState_858_.Idle)
    val state: StateFlow<GenState_858_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_858_) {
        when (event) {
            is GenEvent_858_.Load -> loadAll()
            is GenEvent_858_.Update -> save(event.model)
            is GenEvent_858_.Delete -> delete(event.id)
            is GenEvent_858_.Refresh -> loadAll()
            is GenEvent_858_.Search -> search(event.query)
            is GenEvent_858_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_858_.Loading; _state.value = GenState_858_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_858_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_858_.Success(searchUseCase(query)) } }
}
