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

data class GenModel_709_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_709_ {
    data class Load(val id: Long) : GenEvent_709_()
    data class Update(val model: GenModel_709_) : GenEvent_709_()
    data class Delete(val id: Long) : GenEvent_709_()
    data object Refresh : GenEvent_709_()
    data class Search(val query: String) : GenEvent_709_()
    data class Filter(val predicate: String) : GenEvent_709_()
}

sealed class GenState_709_ {
    data object Idle : GenState_709_()
    data object Loading : GenState_709_()
    data class Success(val items: List<GenModel_709_>) : GenState_709_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_709_()
    data class Partial(val items: List<GenModel_709_>, val hasMore: Boolean) : GenState_709_()
}

interface GenRepository_709_ {
    suspend fun getAll(): List<GenModel_709_>
    suspend fun getById(id: Long): GenModel_709_?
    suspend fun save(model: GenModel_709_): GenModel_709_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_709_>
}

@Singleton
class GenRepositoryImpl_709_ @Inject constructor() : GenRepository_709_ {
    private val store = mutableMapOf<Long, GenModel_709_>()
    override suspend fun getAll(): List<GenModel_709_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_709_? = store[id]
    override suspend fun save(model: GenModel_709_): GenModel_709_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_709_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_709_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_709_ @Inject constructor(
    private val repository: GenRepositoryImpl_709_
) : GenUseCase_709_<Unit, List<GenModel_709_>> {
    override suspend fun invoke(params: Unit): List<GenModel_709_> = repository.getAll()
}

class GenSaveUseCase_709_ @Inject constructor(
    private val repository: GenRepositoryImpl_709_
) : GenUseCase_709_<GenModel_709_, GenModel_709_> {
    override suspend fun invoke(params: GenModel_709_): GenModel_709_ = repository.save(params)
}

class GenDeleteUseCase_709_ @Inject constructor(
    private val repository: GenRepositoryImpl_709_
) : GenUseCase_709_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_709_ @Inject constructor(
    private val repository: GenRepositoryImpl_709_
) : GenUseCase_709_<String, List<GenModel_709_>> {
    override suspend fun invoke(params: String): List<GenModel_709_> = repository.search(params)
}

abstract class GenMapper_709_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_709_ : GenMapper_709_<GenModel_709_, String>() {
    override fun map(input: GenModel_709_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_709_ : GenMapper_709_<String, GenModel_709_>() {
    override fun map(input: String): GenModel_709_ {
        val parts = input.split(":")
        return GenModel_709_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_709_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_709_,
    private val saveUseCase: GenSaveUseCase_709_,
    private val deleteUseCase: GenDeleteUseCase_709_,
    private val searchUseCase: GenSearchUseCase_709_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_709_>(GenState_709_.Idle)
    val state: StateFlow<GenState_709_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_709_) {
        when (event) {
            is GenEvent_709_.Load -> loadAll()
            is GenEvent_709_.Update -> save(event.model)
            is GenEvent_709_.Delete -> delete(event.id)
            is GenEvent_709_.Refresh -> loadAll()
            is GenEvent_709_.Search -> search(event.query)
            is GenEvent_709_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_709_.Loading; _state.value = GenState_709_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_709_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_709_.Success(searchUseCase(query)) } }
}
