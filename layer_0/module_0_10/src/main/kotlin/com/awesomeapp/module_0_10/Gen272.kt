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

data class GenModel_272_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_272_ {
    data class Load(val id: Long) : GenEvent_272_()
    data class Update(val model: GenModel_272_) : GenEvent_272_()
    data class Delete(val id: Long) : GenEvent_272_()
    data object Refresh : GenEvent_272_()
    data class Search(val query: String) : GenEvent_272_()
    data class Filter(val predicate: String) : GenEvent_272_()
}

sealed class GenState_272_ {
    data object Idle : GenState_272_()
    data object Loading : GenState_272_()
    data class Success(val items: List<GenModel_272_>) : GenState_272_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_272_()
    data class Partial(val items: List<GenModel_272_>, val hasMore: Boolean) : GenState_272_()
}

interface GenRepository_272_ {
    suspend fun getAll(): List<GenModel_272_>
    suspend fun getById(id: Long): GenModel_272_?
    suspend fun save(model: GenModel_272_): GenModel_272_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_272_>
}

@Singleton
class GenRepositoryImpl_272_ @Inject constructor() : GenRepository_272_ {
    private val store = mutableMapOf<Long, GenModel_272_>()
    override suspend fun getAll(): List<GenModel_272_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_272_? = store[id]
    override suspend fun save(model: GenModel_272_): GenModel_272_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_272_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_272_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_272_ @Inject constructor(
    private val repository: GenRepositoryImpl_272_
) : GenUseCase_272_<Unit, List<GenModel_272_>> {
    override suspend fun invoke(params: Unit): List<GenModel_272_> = repository.getAll()
}

class GenSaveUseCase_272_ @Inject constructor(
    private val repository: GenRepositoryImpl_272_
) : GenUseCase_272_<GenModel_272_, GenModel_272_> {
    override suspend fun invoke(params: GenModel_272_): GenModel_272_ = repository.save(params)
}

class GenDeleteUseCase_272_ @Inject constructor(
    private val repository: GenRepositoryImpl_272_
) : GenUseCase_272_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_272_ @Inject constructor(
    private val repository: GenRepositoryImpl_272_
) : GenUseCase_272_<String, List<GenModel_272_>> {
    override suspend fun invoke(params: String): List<GenModel_272_> = repository.search(params)
}

abstract class GenMapper_272_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_272_ : GenMapper_272_<GenModel_272_, String>() {
    override fun map(input: GenModel_272_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_272_ : GenMapper_272_<String, GenModel_272_>() {
    override fun map(input: String): GenModel_272_ {
        val parts = input.split(":")
        return GenModel_272_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_272_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_272_,
    private val saveUseCase: GenSaveUseCase_272_,
    private val deleteUseCase: GenDeleteUseCase_272_,
    private val searchUseCase: GenSearchUseCase_272_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_272_>(GenState_272_.Idle)
    val state: StateFlow<GenState_272_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_272_) {
        when (event) {
            is GenEvent_272_.Load -> loadAll()
            is GenEvent_272_.Update -> save(event.model)
            is GenEvent_272_.Delete -> delete(event.id)
            is GenEvent_272_.Refresh -> loadAll()
            is GenEvent_272_.Search -> search(event.query)
            is GenEvent_272_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_272_.Loading; _state.value = GenState_272_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_272_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_272_.Success(searchUseCase(query)) } }
}
