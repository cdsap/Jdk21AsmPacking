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

data class GenModel_248_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_248_ {
    data class Load(val id: Long) : GenEvent_248_()
    data class Update(val model: GenModel_248_) : GenEvent_248_()
    data class Delete(val id: Long) : GenEvent_248_()
    data object Refresh : GenEvent_248_()
    data class Search(val query: String) : GenEvent_248_()
    data class Filter(val predicate: String) : GenEvent_248_()
}

sealed class GenState_248_ {
    data object Idle : GenState_248_()
    data object Loading : GenState_248_()
    data class Success(val items: List<GenModel_248_>) : GenState_248_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_248_()
    data class Partial(val items: List<GenModel_248_>, val hasMore: Boolean) : GenState_248_()
}

interface GenRepository_248_ {
    suspend fun getAll(): List<GenModel_248_>
    suspend fun getById(id: Long): GenModel_248_?
    suspend fun save(model: GenModel_248_): GenModel_248_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_248_>
}

@Singleton
class GenRepositoryImpl_248_ @Inject constructor() : GenRepository_248_ {
    private val store = mutableMapOf<Long, GenModel_248_>()
    override suspend fun getAll(): List<GenModel_248_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_248_? = store[id]
    override suspend fun save(model: GenModel_248_): GenModel_248_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_248_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_248_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_248_ @Inject constructor(
    private val repository: GenRepositoryImpl_248_
) : GenUseCase_248_<Unit, List<GenModel_248_>> {
    override suspend fun invoke(params: Unit): List<GenModel_248_> = repository.getAll()
}

class GenSaveUseCase_248_ @Inject constructor(
    private val repository: GenRepositoryImpl_248_
) : GenUseCase_248_<GenModel_248_, GenModel_248_> {
    override suspend fun invoke(params: GenModel_248_): GenModel_248_ = repository.save(params)
}

class GenDeleteUseCase_248_ @Inject constructor(
    private val repository: GenRepositoryImpl_248_
) : GenUseCase_248_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_248_ @Inject constructor(
    private val repository: GenRepositoryImpl_248_
) : GenUseCase_248_<String, List<GenModel_248_>> {
    override suspend fun invoke(params: String): List<GenModel_248_> = repository.search(params)
}

abstract class GenMapper_248_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_248_ : GenMapper_248_<GenModel_248_, String>() {
    override fun map(input: GenModel_248_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_248_ : GenMapper_248_<String, GenModel_248_>() {
    override fun map(input: String): GenModel_248_ {
        val parts = input.split(":")
        return GenModel_248_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_248_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_248_,
    private val saveUseCase: GenSaveUseCase_248_,
    private val deleteUseCase: GenDeleteUseCase_248_,
    private val searchUseCase: GenSearchUseCase_248_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_248_>(GenState_248_.Idle)
    val state: StateFlow<GenState_248_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_248_) {
        when (event) {
            is GenEvent_248_.Load -> loadAll()
            is GenEvent_248_.Update -> save(event.model)
            is GenEvent_248_.Delete -> delete(event.id)
            is GenEvent_248_.Refresh -> loadAll()
            is GenEvent_248_.Search -> search(event.query)
            is GenEvent_248_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_248_.Loading; _state.value = GenState_248_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_248_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_248_.Success(searchUseCase(query)) } }
}
