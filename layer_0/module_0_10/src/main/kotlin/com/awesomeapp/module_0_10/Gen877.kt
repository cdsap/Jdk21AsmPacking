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

data class GenModel_877_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_877_ {
    data class Load(val id: Long) : GenEvent_877_()
    data class Update(val model: GenModel_877_) : GenEvent_877_()
    data class Delete(val id: Long) : GenEvent_877_()
    data object Refresh : GenEvent_877_()
    data class Search(val query: String) : GenEvent_877_()
    data class Filter(val predicate: String) : GenEvent_877_()
}

sealed class GenState_877_ {
    data object Idle : GenState_877_()
    data object Loading : GenState_877_()
    data class Success(val items: List<GenModel_877_>) : GenState_877_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_877_()
    data class Partial(val items: List<GenModel_877_>, val hasMore: Boolean) : GenState_877_()
}

interface GenRepository_877_ {
    suspend fun getAll(): List<GenModel_877_>
    suspend fun getById(id: Long): GenModel_877_?
    suspend fun save(model: GenModel_877_): GenModel_877_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_877_>
}

@Singleton
class GenRepositoryImpl_877_ @Inject constructor() : GenRepository_877_ {
    private val store = mutableMapOf<Long, GenModel_877_>()
    override suspend fun getAll(): List<GenModel_877_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_877_? = store[id]
    override suspend fun save(model: GenModel_877_): GenModel_877_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_877_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_877_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_877_ @Inject constructor(
    private val repository: GenRepositoryImpl_877_
) : GenUseCase_877_<Unit, List<GenModel_877_>> {
    override suspend fun invoke(params: Unit): List<GenModel_877_> = repository.getAll()
}

class GenSaveUseCase_877_ @Inject constructor(
    private val repository: GenRepositoryImpl_877_
) : GenUseCase_877_<GenModel_877_, GenModel_877_> {
    override suspend fun invoke(params: GenModel_877_): GenModel_877_ = repository.save(params)
}

class GenDeleteUseCase_877_ @Inject constructor(
    private val repository: GenRepositoryImpl_877_
) : GenUseCase_877_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_877_ @Inject constructor(
    private val repository: GenRepositoryImpl_877_
) : GenUseCase_877_<String, List<GenModel_877_>> {
    override suspend fun invoke(params: String): List<GenModel_877_> = repository.search(params)
}

abstract class GenMapper_877_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_877_ : GenMapper_877_<GenModel_877_, String>() {
    override fun map(input: GenModel_877_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_877_ : GenMapper_877_<String, GenModel_877_>() {
    override fun map(input: String): GenModel_877_ {
        val parts = input.split(":")
        return GenModel_877_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_877_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_877_,
    private val saveUseCase: GenSaveUseCase_877_,
    private val deleteUseCase: GenDeleteUseCase_877_,
    private val searchUseCase: GenSearchUseCase_877_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_877_>(GenState_877_.Idle)
    val state: StateFlow<GenState_877_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_877_) {
        when (event) {
            is GenEvent_877_.Load -> loadAll()
            is GenEvent_877_.Update -> save(event.model)
            is GenEvent_877_.Delete -> delete(event.id)
            is GenEvent_877_.Refresh -> loadAll()
            is GenEvent_877_.Search -> search(event.query)
            is GenEvent_877_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_877_.Loading; _state.value = GenState_877_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_877_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_877_.Success(searchUseCase(query)) } }
}
