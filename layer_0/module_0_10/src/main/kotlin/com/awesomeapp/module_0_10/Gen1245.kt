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

data class GenModel_1245_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1245_ {
    data class Load(val id: Long) : GenEvent_1245_()
    data class Update(val model: GenModel_1245_) : GenEvent_1245_()
    data class Delete(val id: Long) : GenEvent_1245_()
    data object Refresh : GenEvent_1245_()
    data class Search(val query: String) : GenEvent_1245_()
    data class Filter(val predicate: String) : GenEvent_1245_()
}

sealed class GenState_1245_ {
    data object Idle : GenState_1245_()
    data object Loading : GenState_1245_()
    data class Success(val items: List<GenModel_1245_>) : GenState_1245_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1245_()
    data class Partial(val items: List<GenModel_1245_>, val hasMore: Boolean) : GenState_1245_()
}

interface GenRepository_1245_ {
    suspend fun getAll(): List<GenModel_1245_>
    suspend fun getById(id: Long): GenModel_1245_?
    suspend fun save(model: GenModel_1245_): GenModel_1245_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1245_>
}

@Singleton
class GenRepositoryImpl_1245_ @Inject constructor() : GenRepository_1245_ {
    private val store = mutableMapOf<Long, GenModel_1245_>()
    override suspend fun getAll(): List<GenModel_1245_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1245_? = store[id]
    override suspend fun save(model: GenModel_1245_): GenModel_1245_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1245_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1245_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1245_ @Inject constructor(
    private val repository: GenRepositoryImpl_1245_
) : GenUseCase_1245_<Unit, List<GenModel_1245_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1245_> = repository.getAll()
}

class GenSaveUseCase_1245_ @Inject constructor(
    private val repository: GenRepositoryImpl_1245_
) : GenUseCase_1245_<GenModel_1245_, GenModel_1245_> {
    override suspend fun invoke(params: GenModel_1245_): GenModel_1245_ = repository.save(params)
}

class GenDeleteUseCase_1245_ @Inject constructor(
    private val repository: GenRepositoryImpl_1245_
) : GenUseCase_1245_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1245_ @Inject constructor(
    private val repository: GenRepositoryImpl_1245_
) : GenUseCase_1245_<String, List<GenModel_1245_>> {
    override suspend fun invoke(params: String): List<GenModel_1245_> = repository.search(params)
}

abstract class GenMapper_1245_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1245_ : GenMapper_1245_<GenModel_1245_, String>() {
    override fun map(input: GenModel_1245_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1245_ : GenMapper_1245_<String, GenModel_1245_>() {
    override fun map(input: String): GenModel_1245_ {
        val parts = input.split(":")
        return GenModel_1245_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1245_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1245_,
    private val saveUseCase: GenSaveUseCase_1245_,
    private val deleteUseCase: GenDeleteUseCase_1245_,
    private val searchUseCase: GenSearchUseCase_1245_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1245_>(GenState_1245_.Idle)
    val state: StateFlow<GenState_1245_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1245_) {
        when (event) {
            is GenEvent_1245_.Load -> loadAll()
            is GenEvent_1245_.Update -> save(event.model)
            is GenEvent_1245_.Delete -> delete(event.id)
            is GenEvent_1245_.Refresh -> loadAll()
            is GenEvent_1245_.Search -> search(event.query)
            is GenEvent_1245_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1245_.Loading; _state.value = GenState_1245_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1245_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1245_.Success(searchUseCase(query)) } }
}
