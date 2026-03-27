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

data class GenModel_608_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_608_ {
    data class Load(val id: Long) : GenEvent_608_()
    data class Update(val model: GenModel_608_) : GenEvent_608_()
    data class Delete(val id: Long) : GenEvent_608_()
    data object Refresh : GenEvent_608_()
    data class Search(val query: String) : GenEvent_608_()
    data class Filter(val predicate: String) : GenEvent_608_()
}

sealed class GenState_608_ {
    data object Idle : GenState_608_()
    data object Loading : GenState_608_()
    data class Success(val items: List<GenModel_608_>) : GenState_608_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_608_()
    data class Partial(val items: List<GenModel_608_>, val hasMore: Boolean) : GenState_608_()
}

interface GenRepository_608_ {
    suspend fun getAll(): List<GenModel_608_>
    suspend fun getById(id: Long): GenModel_608_?
    suspend fun save(model: GenModel_608_): GenModel_608_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_608_>
}

@Singleton
class GenRepositoryImpl_608_ @Inject constructor() : GenRepository_608_ {
    private val store = mutableMapOf<Long, GenModel_608_>()
    override suspend fun getAll(): List<GenModel_608_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_608_? = store[id]
    override suspend fun save(model: GenModel_608_): GenModel_608_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_608_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_608_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_608_ @Inject constructor(
    private val repository: GenRepositoryImpl_608_
) : GenUseCase_608_<Unit, List<GenModel_608_>> {
    override suspend fun invoke(params: Unit): List<GenModel_608_> = repository.getAll()
}

class GenSaveUseCase_608_ @Inject constructor(
    private val repository: GenRepositoryImpl_608_
) : GenUseCase_608_<GenModel_608_, GenModel_608_> {
    override suspend fun invoke(params: GenModel_608_): GenModel_608_ = repository.save(params)
}

class GenDeleteUseCase_608_ @Inject constructor(
    private val repository: GenRepositoryImpl_608_
) : GenUseCase_608_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_608_ @Inject constructor(
    private val repository: GenRepositoryImpl_608_
) : GenUseCase_608_<String, List<GenModel_608_>> {
    override suspend fun invoke(params: String): List<GenModel_608_> = repository.search(params)
}

abstract class GenMapper_608_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_608_ : GenMapper_608_<GenModel_608_, String>() {
    override fun map(input: GenModel_608_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_608_ : GenMapper_608_<String, GenModel_608_>() {
    override fun map(input: String): GenModel_608_ {
        val parts = input.split(":")
        return GenModel_608_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_608_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_608_,
    private val saveUseCase: GenSaveUseCase_608_,
    private val deleteUseCase: GenDeleteUseCase_608_,
    private val searchUseCase: GenSearchUseCase_608_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_608_>(GenState_608_.Idle)
    val state: StateFlow<GenState_608_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_608_) {
        when (event) {
            is GenEvent_608_.Load -> loadAll()
            is GenEvent_608_.Update -> save(event.model)
            is GenEvent_608_.Delete -> delete(event.id)
            is GenEvent_608_.Refresh -> loadAll()
            is GenEvent_608_.Search -> search(event.query)
            is GenEvent_608_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_608_.Loading; _state.value = GenState_608_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_608_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_608_.Success(searchUseCase(query)) } }
}
