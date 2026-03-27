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

data class GenModel_797_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_797_ {
    data class Load(val id: Long) : GenEvent_797_()
    data class Update(val model: GenModel_797_) : GenEvent_797_()
    data class Delete(val id: Long) : GenEvent_797_()
    data object Refresh : GenEvent_797_()
    data class Search(val query: String) : GenEvent_797_()
    data class Filter(val predicate: String) : GenEvent_797_()
}

sealed class GenState_797_ {
    data object Idle : GenState_797_()
    data object Loading : GenState_797_()
    data class Success(val items: List<GenModel_797_>) : GenState_797_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_797_()
    data class Partial(val items: List<GenModel_797_>, val hasMore: Boolean) : GenState_797_()
}

interface GenRepository_797_ {
    suspend fun getAll(): List<GenModel_797_>
    suspend fun getById(id: Long): GenModel_797_?
    suspend fun save(model: GenModel_797_): GenModel_797_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_797_>
}

@Singleton
class GenRepositoryImpl_797_ @Inject constructor() : GenRepository_797_ {
    private val store = mutableMapOf<Long, GenModel_797_>()
    override suspend fun getAll(): List<GenModel_797_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_797_? = store[id]
    override suspend fun save(model: GenModel_797_): GenModel_797_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_797_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_797_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_797_ @Inject constructor(
    private val repository: GenRepositoryImpl_797_
) : GenUseCase_797_<Unit, List<GenModel_797_>> {
    override suspend fun invoke(params: Unit): List<GenModel_797_> = repository.getAll()
}

class GenSaveUseCase_797_ @Inject constructor(
    private val repository: GenRepositoryImpl_797_
) : GenUseCase_797_<GenModel_797_, GenModel_797_> {
    override suspend fun invoke(params: GenModel_797_): GenModel_797_ = repository.save(params)
}

class GenDeleteUseCase_797_ @Inject constructor(
    private val repository: GenRepositoryImpl_797_
) : GenUseCase_797_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_797_ @Inject constructor(
    private val repository: GenRepositoryImpl_797_
) : GenUseCase_797_<String, List<GenModel_797_>> {
    override suspend fun invoke(params: String): List<GenModel_797_> = repository.search(params)
}

abstract class GenMapper_797_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_797_ : GenMapper_797_<GenModel_797_, String>() {
    override fun map(input: GenModel_797_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_797_ : GenMapper_797_<String, GenModel_797_>() {
    override fun map(input: String): GenModel_797_ {
        val parts = input.split(":")
        return GenModel_797_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_797_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_797_,
    private val saveUseCase: GenSaveUseCase_797_,
    private val deleteUseCase: GenDeleteUseCase_797_,
    private val searchUseCase: GenSearchUseCase_797_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_797_>(GenState_797_.Idle)
    val state: StateFlow<GenState_797_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_797_) {
        when (event) {
            is GenEvent_797_.Load -> loadAll()
            is GenEvent_797_.Update -> save(event.model)
            is GenEvent_797_.Delete -> delete(event.id)
            is GenEvent_797_.Refresh -> loadAll()
            is GenEvent_797_.Search -> search(event.query)
            is GenEvent_797_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_797_.Loading; _state.value = GenState_797_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_797_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_797_.Success(searchUseCase(query)) } }
}
