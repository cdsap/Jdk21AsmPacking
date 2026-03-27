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

data class GenModel_598_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_598_ {
    data class Load(val id: Long) : GenEvent_598_()
    data class Update(val model: GenModel_598_) : GenEvent_598_()
    data class Delete(val id: Long) : GenEvent_598_()
    data object Refresh : GenEvent_598_()
    data class Search(val query: String) : GenEvent_598_()
    data class Filter(val predicate: String) : GenEvent_598_()
}

sealed class GenState_598_ {
    data object Idle : GenState_598_()
    data object Loading : GenState_598_()
    data class Success(val items: List<GenModel_598_>) : GenState_598_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_598_()
    data class Partial(val items: List<GenModel_598_>, val hasMore: Boolean) : GenState_598_()
}

interface GenRepository_598_ {
    suspend fun getAll(): List<GenModel_598_>
    suspend fun getById(id: Long): GenModel_598_?
    suspend fun save(model: GenModel_598_): GenModel_598_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_598_>
}

@Singleton
class GenRepositoryImpl_598_ @Inject constructor() : GenRepository_598_ {
    private val store = mutableMapOf<Long, GenModel_598_>()
    override suspend fun getAll(): List<GenModel_598_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_598_? = store[id]
    override suspend fun save(model: GenModel_598_): GenModel_598_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_598_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_598_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_598_ @Inject constructor(
    private val repository: GenRepositoryImpl_598_
) : GenUseCase_598_<Unit, List<GenModel_598_>> {
    override suspend fun invoke(params: Unit): List<GenModel_598_> = repository.getAll()
}

class GenSaveUseCase_598_ @Inject constructor(
    private val repository: GenRepositoryImpl_598_
) : GenUseCase_598_<GenModel_598_, GenModel_598_> {
    override suspend fun invoke(params: GenModel_598_): GenModel_598_ = repository.save(params)
}

class GenDeleteUseCase_598_ @Inject constructor(
    private val repository: GenRepositoryImpl_598_
) : GenUseCase_598_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_598_ @Inject constructor(
    private val repository: GenRepositoryImpl_598_
) : GenUseCase_598_<String, List<GenModel_598_>> {
    override suspend fun invoke(params: String): List<GenModel_598_> = repository.search(params)
}

abstract class GenMapper_598_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_598_ : GenMapper_598_<GenModel_598_, String>() {
    override fun map(input: GenModel_598_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_598_ : GenMapper_598_<String, GenModel_598_>() {
    override fun map(input: String): GenModel_598_ {
        val parts = input.split(":")
        return GenModel_598_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_598_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_598_,
    private val saveUseCase: GenSaveUseCase_598_,
    private val deleteUseCase: GenDeleteUseCase_598_,
    private val searchUseCase: GenSearchUseCase_598_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_598_>(GenState_598_.Idle)
    val state: StateFlow<GenState_598_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_598_) {
        when (event) {
            is GenEvent_598_.Load -> loadAll()
            is GenEvent_598_.Update -> save(event.model)
            is GenEvent_598_.Delete -> delete(event.id)
            is GenEvent_598_.Refresh -> loadAll()
            is GenEvent_598_.Search -> search(event.query)
            is GenEvent_598_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_598_.Loading; _state.value = GenState_598_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_598_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_598_.Success(searchUseCase(query)) } }
}
