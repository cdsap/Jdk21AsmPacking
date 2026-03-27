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

data class GenModel_917_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_917_ {
    data class Load(val id: Long) : GenEvent_917_()
    data class Update(val model: GenModel_917_) : GenEvent_917_()
    data class Delete(val id: Long) : GenEvent_917_()
    data object Refresh : GenEvent_917_()
    data class Search(val query: String) : GenEvent_917_()
    data class Filter(val predicate: String) : GenEvent_917_()
}

sealed class GenState_917_ {
    data object Idle : GenState_917_()
    data object Loading : GenState_917_()
    data class Success(val items: List<GenModel_917_>) : GenState_917_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_917_()
    data class Partial(val items: List<GenModel_917_>, val hasMore: Boolean) : GenState_917_()
}

interface GenRepository_917_ {
    suspend fun getAll(): List<GenModel_917_>
    suspend fun getById(id: Long): GenModel_917_?
    suspend fun save(model: GenModel_917_): GenModel_917_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_917_>
}

@Singleton
class GenRepositoryImpl_917_ @Inject constructor() : GenRepository_917_ {
    private val store = mutableMapOf<Long, GenModel_917_>()
    override suspend fun getAll(): List<GenModel_917_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_917_? = store[id]
    override suspend fun save(model: GenModel_917_): GenModel_917_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_917_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_917_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_917_ @Inject constructor(
    private val repository: GenRepositoryImpl_917_
) : GenUseCase_917_<Unit, List<GenModel_917_>> {
    override suspend fun invoke(params: Unit): List<GenModel_917_> = repository.getAll()
}

class GenSaveUseCase_917_ @Inject constructor(
    private val repository: GenRepositoryImpl_917_
) : GenUseCase_917_<GenModel_917_, GenModel_917_> {
    override suspend fun invoke(params: GenModel_917_): GenModel_917_ = repository.save(params)
}

class GenDeleteUseCase_917_ @Inject constructor(
    private val repository: GenRepositoryImpl_917_
) : GenUseCase_917_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_917_ @Inject constructor(
    private val repository: GenRepositoryImpl_917_
) : GenUseCase_917_<String, List<GenModel_917_>> {
    override suspend fun invoke(params: String): List<GenModel_917_> = repository.search(params)
}

abstract class GenMapper_917_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_917_ : GenMapper_917_<GenModel_917_, String>() {
    override fun map(input: GenModel_917_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_917_ : GenMapper_917_<String, GenModel_917_>() {
    override fun map(input: String): GenModel_917_ {
        val parts = input.split(":")
        return GenModel_917_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_917_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_917_,
    private val saveUseCase: GenSaveUseCase_917_,
    private val deleteUseCase: GenDeleteUseCase_917_,
    private val searchUseCase: GenSearchUseCase_917_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_917_>(GenState_917_.Idle)
    val state: StateFlow<GenState_917_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_917_) {
        when (event) {
            is GenEvent_917_.Load -> loadAll()
            is GenEvent_917_.Update -> save(event.model)
            is GenEvent_917_.Delete -> delete(event.id)
            is GenEvent_917_.Refresh -> loadAll()
            is GenEvent_917_.Search -> search(event.query)
            is GenEvent_917_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_917_.Loading; _state.value = GenState_917_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_917_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_917_.Success(searchUseCase(query)) } }
}
