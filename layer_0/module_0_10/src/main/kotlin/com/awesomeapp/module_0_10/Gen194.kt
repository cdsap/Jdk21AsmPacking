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

data class GenModel_194_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_194_ {
    data class Load(val id: Long) : GenEvent_194_()
    data class Update(val model: GenModel_194_) : GenEvent_194_()
    data class Delete(val id: Long) : GenEvent_194_()
    data object Refresh : GenEvent_194_()
    data class Search(val query: String) : GenEvent_194_()
    data class Filter(val predicate: String) : GenEvent_194_()
}

sealed class GenState_194_ {
    data object Idle : GenState_194_()
    data object Loading : GenState_194_()
    data class Success(val items: List<GenModel_194_>) : GenState_194_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_194_()
    data class Partial(val items: List<GenModel_194_>, val hasMore: Boolean) : GenState_194_()
}

interface GenRepository_194_ {
    suspend fun getAll(): List<GenModel_194_>
    suspend fun getById(id: Long): GenModel_194_?
    suspend fun save(model: GenModel_194_): GenModel_194_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_194_>
}

@Singleton
class GenRepositoryImpl_194_ @Inject constructor() : GenRepository_194_ {
    private val store = mutableMapOf<Long, GenModel_194_>()
    override suspend fun getAll(): List<GenModel_194_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_194_? = store[id]
    override suspend fun save(model: GenModel_194_): GenModel_194_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_194_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_194_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_194_ @Inject constructor(
    private val repository: GenRepositoryImpl_194_
) : GenUseCase_194_<Unit, List<GenModel_194_>> {
    override suspend fun invoke(params: Unit): List<GenModel_194_> = repository.getAll()
}

class GenSaveUseCase_194_ @Inject constructor(
    private val repository: GenRepositoryImpl_194_
) : GenUseCase_194_<GenModel_194_, GenModel_194_> {
    override suspend fun invoke(params: GenModel_194_): GenModel_194_ = repository.save(params)
}

class GenDeleteUseCase_194_ @Inject constructor(
    private val repository: GenRepositoryImpl_194_
) : GenUseCase_194_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_194_ @Inject constructor(
    private val repository: GenRepositoryImpl_194_
) : GenUseCase_194_<String, List<GenModel_194_>> {
    override suspend fun invoke(params: String): List<GenModel_194_> = repository.search(params)
}

abstract class GenMapper_194_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_194_ : GenMapper_194_<GenModel_194_, String>() {
    override fun map(input: GenModel_194_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_194_ : GenMapper_194_<String, GenModel_194_>() {
    override fun map(input: String): GenModel_194_ {
        val parts = input.split(":")
        return GenModel_194_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_194_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_194_,
    private val saveUseCase: GenSaveUseCase_194_,
    private val deleteUseCase: GenDeleteUseCase_194_,
    private val searchUseCase: GenSearchUseCase_194_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_194_>(GenState_194_.Idle)
    val state: StateFlow<GenState_194_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_194_) {
        when (event) {
            is GenEvent_194_.Load -> loadAll()
            is GenEvent_194_.Update -> save(event.model)
            is GenEvent_194_.Delete -> delete(event.id)
            is GenEvent_194_.Refresh -> loadAll()
            is GenEvent_194_.Search -> search(event.query)
            is GenEvent_194_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_194_.Loading; _state.value = GenState_194_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_194_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_194_.Success(searchUseCase(query)) } }
}
