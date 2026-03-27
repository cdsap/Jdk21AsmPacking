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

data class GenModel_1248_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1248_ {
    data class Load(val id: Long) : GenEvent_1248_()
    data class Update(val model: GenModel_1248_) : GenEvent_1248_()
    data class Delete(val id: Long) : GenEvent_1248_()
    data object Refresh : GenEvent_1248_()
    data class Search(val query: String) : GenEvent_1248_()
    data class Filter(val predicate: String) : GenEvent_1248_()
}

sealed class GenState_1248_ {
    data object Idle : GenState_1248_()
    data object Loading : GenState_1248_()
    data class Success(val items: List<GenModel_1248_>) : GenState_1248_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1248_()
    data class Partial(val items: List<GenModel_1248_>, val hasMore: Boolean) : GenState_1248_()
}

interface GenRepository_1248_ {
    suspend fun getAll(): List<GenModel_1248_>
    suspend fun getById(id: Long): GenModel_1248_?
    suspend fun save(model: GenModel_1248_): GenModel_1248_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1248_>
}

@Singleton
class GenRepositoryImpl_1248_ @Inject constructor() : GenRepository_1248_ {
    private val store = mutableMapOf<Long, GenModel_1248_>()
    override suspend fun getAll(): List<GenModel_1248_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1248_? = store[id]
    override suspend fun save(model: GenModel_1248_): GenModel_1248_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1248_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1248_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1248_ @Inject constructor(
    private val repository: GenRepositoryImpl_1248_
) : GenUseCase_1248_<Unit, List<GenModel_1248_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1248_> = repository.getAll()
}

class GenSaveUseCase_1248_ @Inject constructor(
    private val repository: GenRepositoryImpl_1248_
) : GenUseCase_1248_<GenModel_1248_, GenModel_1248_> {
    override suspend fun invoke(params: GenModel_1248_): GenModel_1248_ = repository.save(params)
}

class GenDeleteUseCase_1248_ @Inject constructor(
    private val repository: GenRepositoryImpl_1248_
) : GenUseCase_1248_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1248_ @Inject constructor(
    private val repository: GenRepositoryImpl_1248_
) : GenUseCase_1248_<String, List<GenModel_1248_>> {
    override suspend fun invoke(params: String): List<GenModel_1248_> = repository.search(params)
}

abstract class GenMapper_1248_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1248_ : GenMapper_1248_<GenModel_1248_, String>() {
    override fun map(input: GenModel_1248_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1248_ : GenMapper_1248_<String, GenModel_1248_>() {
    override fun map(input: String): GenModel_1248_ {
        val parts = input.split(":")
        return GenModel_1248_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1248_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1248_,
    private val saveUseCase: GenSaveUseCase_1248_,
    private val deleteUseCase: GenDeleteUseCase_1248_,
    private val searchUseCase: GenSearchUseCase_1248_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1248_>(GenState_1248_.Idle)
    val state: StateFlow<GenState_1248_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1248_) {
        when (event) {
            is GenEvent_1248_.Load -> loadAll()
            is GenEvent_1248_.Update -> save(event.model)
            is GenEvent_1248_.Delete -> delete(event.id)
            is GenEvent_1248_.Refresh -> loadAll()
            is GenEvent_1248_.Search -> search(event.query)
            is GenEvent_1248_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1248_.Loading; _state.value = GenState_1248_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1248_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1248_.Success(searchUseCase(query)) } }
}
