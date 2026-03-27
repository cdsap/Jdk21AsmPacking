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

data class GenModel_1608_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1608_ {
    data class Load(val id: Long) : GenEvent_1608_()
    data class Update(val model: GenModel_1608_) : GenEvent_1608_()
    data class Delete(val id: Long) : GenEvent_1608_()
    data object Refresh : GenEvent_1608_()
    data class Search(val query: String) : GenEvent_1608_()
    data class Filter(val predicate: String) : GenEvent_1608_()
}

sealed class GenState_1608_ {
    data object Idle : GenState_1608_()
    data object Loading : GenState_1608_()
    data class Success(val items: List<GenModel_1608_>) : GenState_1608_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1608_()
    data class Partial(val items: List<GenModel_1608_>, val hasMore: Boolean) : GenState_1608_()
}

interface GenRepository_1608_ {
    suspend fun getAll(): List<GenModel_1608_>
    suspend fun getById(id: Long): GenModel_1608_?
    suspend fun save(model: GenModel_1608_): GenModel_1608_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1608_>
}

@Singleton
class GenRepositoryImpl_1608_ @Inject constructor() : GenRepository_1608_ {
    private val store = mutableMapOf<Long, GenModel_1608_>()
    override suspend fun getAll(): List<GenModel_1608_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1608_? = store[id]
    override suspend fun save(model: GenModel_1608_): GenModel_1608_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1608_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1608_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1608_ @Inject constructor(
    private val repository: GenRepositoryImpl_1608_
) : GenUseCase_1608_<Unit, List<GenModel_1608_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1608_> = repository.getAll()
}

class GenSaveUseCase_1608_ @Inject constructor(
    private val repository: GenRepositoryImpl_1608_
) : GenUseCase_1608_<GenModel_1608_, GenModel_1608_> {
    override suspend fun invoke(params: GenModel_1608_): GenModel_1608_ = repository.save(params)
}

class GenDeleteUseCase_1608_ @Inject constructor(
    private val repository: GenRepositoryImpl_1608_
) : GenUseCase_1608_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1608_ @Inject constructor(
    private val repository: GenRepositoryImpl_1608_
) : GenUseCase_1608_<String, List<GenModel_1608_>> {
    override suspend fun invoke(params: String): List<GenModel_1608_> = repository.search(params)
}

abstract class GenMapper_1608_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1608_ : GenMapper_1608_<GenModel_1608_, String>() {
    override fun map(input: GenModel_1608_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1608_ : GenMapper_1608_<String, GenModel_1608_>() {
    override fun map(input: String): GenModel_1608_ {
        val parts = input.split(":")
        return GenModel_1608_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1608_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1608_,
    private val saveUseCase: GenSaveUseCase_1608_,
    private val deleteUseCase: GenDeleteUseCase_1608_,
    private val searchUseCase: GenSearchUseCase_1608_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1608_>(GenState_1608_.Idle)
    val state: StateFlow<GenState_1608_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1608_) {
        when (event) {
            is GenEvent_1608_.Load -> loadAll()
            is GenEvent_1608_.Update -> save(event.model)
            is GenEvent_1608_.Delete -> delete(event.id)
            is GenEvent_1608_.Refresh -> loadAll()
            is GenEvent_1608_.Search -> search(event.query)
            is GenEvent_1608_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1608_.Loading; _state.value = GenState_1608_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1608_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1608_.Success(searchUseCase(query)) } }
}
