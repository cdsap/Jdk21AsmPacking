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

data class GenModel_990_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_990_ {
    data class Load(val id: Long) : GenEvent_990_()
    data class Update(val model: GenModel_990_) : GenEvent_990_()
    data class Delete(val id: Long) : GenEvent_990_()
    data object Refresh : GenEvent_990_()
    data class Search(val query: String) : GenEvent_990_()
    data class Filter(val predicate: String) : GenEvent_990_()
}

sealed class GenState_990_ {
    data object Idle : GenState_990_()
    data object Loading : GenState_990_()
    data class Success(val items: List<GenModel_990_>) : GenState_990_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_990_()
    data class Partial(val items: List<GenModel_990_>, val hasMore: Boolean) : GenState_990_()
}

interface GenRepository_990_ {
    suspend fun getAll(): List<GenModel_990_>
    suspend fun getById(id: Long): GenModel_990_?
    suspend fun save(model: GenModel_990_): GenModel_990_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_990_>
}

@Singleton
class GenRepositoryImpl_990_ @Inject constructor() : GenRepository_990_ {
    private val store = mutableMapOf<Long, GenModel_990_>()
    override suspend fun getAll(): List<GenModel_990_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_990_? = store[id]
    override suspend fun save(model: GenModel_990_): GenModel_990_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_990_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_990_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_990_ @Inject constructor(
    private val repository: GenRepositoryImpl_990_
) : GenUseCase_990_<Unit, List<GenModel_990_>> {
    override suspend fun invoke(params: Unit): List<GenModel_990_> = repository.getAll()
}

class GenSaveUseCase_990_ @Inject constructor(
    private val repository: GenRepositoryImpl_990_
) : GenUseCase_990_<GenModel_990_, GenModel_990_> {
    override suspend fun invoke(params: GenModel_990_): GenModel_990_ = repository.save(params)
}

class GenDeleteUseCase_990_ @Inject constructor(
    private val repository: GenRepositoryImpl_990_
) : GenUseCase_990_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_990_ @Inject constructor(
    private val repository: GenRepositoryImpl_990_
) : GenUseCase_990_<String, List<GenModel_990_>> {
    override suspend fun invoke(params: String): List<GenModel_990_> = repository.search(params)
}

abstract class GenMapper_990_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_990_ : GenMapper_990_<GenModel_990_, String>() {
    override fun map(input: GenModel_990_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_990_ : GenMapper_990_<String, GenModel_990_>() {
    override fun map(input: String): GenModel_990_ {
        val parts = input.split(":")
        return GenModel_990_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_990_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_990_,
    private val saveUseCase: GenSaveUseCase_990_,
    private val deleteUseCase: GenDeleteUseCase_990_,
    private val searchUseCase: GenSearchUseCase_990_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_990_>(GenState_990_.Idle)
    val state: StateFlow<GenState_990_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_990_) {
        when (event) {
            is GenEvent_990_.Load -> loadAll()
            is GenEvent_990_.Update -> save(event.model)
            is GenEvent_990_.Delete -> delete(event.id)
            is GenEvent_990_.Refresh -> loadAll()
            is GenEvent_990_.Search -> search(event.query)
            is GenEvent_990_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_990_.Loading; _state.value = GenState_990_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_990_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_990_.Success(searchUseCase(query)) } }
}
