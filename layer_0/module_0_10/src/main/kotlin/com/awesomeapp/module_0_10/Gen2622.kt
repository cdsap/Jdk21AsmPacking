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

data class GenModel_2622_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2622_ {
    data class Load(val id: Long) : GenEvent_2622_()
    data class Update(val model: GenModel_2622_) : GenEvent_2622_()
    data class Delete(val id: Long) : GenEvent_2622_()
    data object Refresh : GenEvent_2622_()
    data class Search(val query: String) : GenEvent_2622_()
    data class Filter(val predicate: String) : GenEvent_2622_()
}

sealed class GenState_2622_ {
    data object Idle : GenState_2622_()
    data object Loading : GenState_2622_()
    data class Success(val items: List<GenModel_2622_>) : GenState_2622_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2622_()
    data class Partial(val items: List<GenModel_2622_>, val hasMore: Boolean) : GenState_2622_()
}

interface GenRepository_2622_ {
    suspend fun getAll(): List<GenModel_2622_>
    suspend fun getById(id: Long): GenModel_2622_?
    suspend fun save(model: GenModel_2622_): GenModel_2622_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2622_>
}

@Singleton
class GenRepositoryImpl_2622_ @Inject constructor() : GenRepository_2622_ {
    private val store = mutableMapOf<Long, GenModel_2622_>()
    override suspend fun getAll(): List<GenModel_2622_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2622_? = store[id]
    override suspend fun save(model: GenModel_2622_): GenModel_2622_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2622_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2622_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2622_ @Inject constructor(
    private val repository: GenRepositoryImpl_2622_
) : GenUseCase_2622_<Unit, List<GenModel_2622_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2622_> = repository.getAll()
}

class GenSaveUseCase_2622_ @Inject constructor(
    private val repository: GenRepositoryImpl_2622_
) : GenUseCase_2622_<GenModel_2622_, GenModel_2622_> {
    override suspend fun invoke(params: GenModel_2622_): GenModel_2622_ = repository.save(params)
}

class GenDeleteUseCase_2622_ @Inject constructor(
    private val repository: GenRepositoryImpl_2622_
) : GenUseCase_2622_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2622_ @Inject constructor(
    private val repository: GenRepositoryImpl_2622_
) : GenUseCase_2622_<String, List<GenModel_2622_>> {
    override suspend fun invoke(params: String): List<GenModel_2622_> = repository.search(params)
}

abstract class GenMapper_2622_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2622_ : GenMapper_2622_<GenModel_2622_, String>() {
    override fun map(input: GenModel_2622_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2622_ : GenMapper_2622_<String, GenModel_2622_>() {
    override fun map(input: String): GenModel_2622_ {
        val parts = input.split(":")
        return GenModel_2622_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2622_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2622_,
    private val saveUseCase: GenSaveUseCase_2622_,
    private val deleteUseCase: GenDeleteUseCase_2622_,
    private val searchUseCase: GenSearchUseCase_2622_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2622_>(GenState_2622_.Idle)
    val state: StateFlow<GenState_2622_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2622_) {
        when (event) {
            is GenEvent_2622_.Load -> loadAll()
            is GenEvent_2622_.Update -> save(event.model)
            is GenEvent_2622_.Delete -> delete(event.id)
            is GenEvent_2622_.Refresh -> loadAll()
            is GenEvent_2622_.Search -> search(event.query)
            is GenEvent_2622_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2622_.Loading; _state.value = GenState_2622_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2622_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2622_.Success(searchUseCase(query)) } }
}
