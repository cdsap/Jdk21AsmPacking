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

data class GenModel_317_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_317_ {
    data class Load(val id: Long) : GenEvent_317_()
    data class Update(val model: GenModel_317_) : GenEvent_317_()
    data class Delete(val id: Long) : GenEvent_317_()
    data object Refresh : GenEvent_317_()
    data class Search(val query: String) : GenEvent_317_()
    data class Filter(val predicate: String) : GenEvent_317_()
}

sealed class GenState_317_ {
    data object Idle : GenState_317_()
    data object Loading : GenState_317_()
    data class Success(val items: List<GenModel_317_>) : GenState_317_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_317_()
    data class Partial(val items: List<GenModel_317_>, val hasMore: Boolean) : GenState_317_()
}

interface GenRepository_317_ {
    suspend fun getAll(): List<GenModel_317_>
    suspend fun getById(id: Long): GenModel_317_?
    suspend fun save(model: GenModel_317_): GenModel_317_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_317_>
}

@Singleton
class GenRepositoryImpl_317_ @Inject constructor() : GenRepository_317_ {
    private val store = mutableMapOf<Long, GenModel_317_>()
    override suspend fun getAll(): List<GenModel_317_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_317_? = store[id]
    override suspend fun save(model: GenModel_317_): GenModel_317_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_317_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_317_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_317_ @Inject constructor(
    private val repository: GenRepositoryImpl_317_
) : GenUseCase_317_<Unit, List<GenModel_317_>> {
    override suspend fun invoke(params: Unit): List<GenModel_317_> = repository.getAll()
}

class GenSaveUseCase_317_ @Inject constructor(
    private val repository: GenRepositoryImpl_317_
) : GenUseCase_317_<GenModel_317_, GenModel_317_> {
    override suspend fun invoke(params: GenModel_317_): GenModel_317_ = repository.save(params)
}

class GenDeleteUseCase_317_ @Inject constructor(
    private val repository: GenRepositoryImpl_317_
) : GenUseCase_317_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_317_ @Inject constructor(
    private val repository: GenRepositoryImpl_317_
) : GenUseCase_317_<String, List<GenModel_317_>> {
    override suspend fun invoke(params: String): List<GenModel_317_> = repository.search(params)
}

abstract class GenMapper_317_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_317_ : GenMapper_317_<GenModel_317_, String>() {
    override fun map(input: GenModel_317_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_317_ : GenMapper_317_<String, GenModel_317_>() {
    override fun map(input: String): GenModel_317_ {
        val parts = input.split(":")
        return GenModel_317_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_317_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_317_,
    private val saveUseCase: GenSaveUseCase_317_,
    private val deleteUseCase: GenDeleteUseCase_317_,
    private val searchUseCase: GenSearchUseCase_317_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_317_>(GenState_317_.Idle)
    val state: StateFlow<GenState_317_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_317_) {
        when (event) {
            is GenEvent_317_.Load -> loadAll()
            is GenEvent_317_.Update -> save(event.model)
            is GenEvent_317_.Delete -> delete(event.id)
            is GenEvent_317_.Refresh -> loadAll()
            is GenEvent_317_.Search -> search(event.query)
            is GenEvent_317_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_317_.Loading; _state.value = GenState_317_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_317_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_317_.Success(searchUseCase(query)) } }
}
