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

data class GenModel_31_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_31_ {
    data class Load(val id: Long) : GenEvent_31_()
    data class Update(val model: GenModel_31_) : GenEvent_31_()
    data class Delete(val id: Long) : GenEvent_31_()
    data object Refresh : GenEvent_31_()
    data class Search(val query: String) : GenEvent_31_()
    data class Filter(val predicate: String) : GenEvent_31_()
}

sealed class GenState_31_ {
    data object Idle : GenState_31_()
    data object Loading : GenState_31_()
    data class Success(val items: List<GenModel_31_>) : GenState_31_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_31_()
    data class Partial(val items: List<GenModel_31_>, val hasMore: Boolean) : GenState_31_()
}

interface GenRepository_31_ {
    suspend fun getAll(): List<GenModel_31_>
    suspend fun getById(id: Long): GenModel_31_?
    suspend fun save(model: GenModel_31_): GenModel_31_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_31_>
}

@Singleton
class GenRepositoryImpl_31_ @Inject constructor() : GenRepository_31_ {
    private val store = mutableMapOf<Long, GenModel_31_>()
    override suspend fun getAll(): List<GenModel_31_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_31_? = store[id]
    override suspend fun save(model: GenModel_31_): GenModel_31_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_31_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_31_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_31_ @Inject constructor(
    private val repository: GenRepositoryImpl_31_
) : GenUseCase_31_<Unit, List<GenModel_31_>> {
    override suspend fun invoke(params: Unit): List<GenModel_31_> = repository.getAll()
}

class GenSaveUseCase_31_ @Inject constructor(
    private val repository: GenRepositoryImpl_31_
) : GenUseCase_31_<GenModel_31_, GenModel_31_> {
    override suspend fun invoke(params: GenModel_31_): GenModel_31_ = repository.save(params)
}

class GenDeleteUseCase_31_ @Inject constructor(
    private val repository: GenRepositoryImpl_31_
) : GenUseCase_31_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_31_ @Inject constructor(
    private val repository: GenRepositoryImpl_31_
) : GenUseCase_31_<String, List<GenModel_31_>> {
    override suspend fun invoke(params: String): List<GenModel_31_> = repository.search(params)
}

abstract class GenMapper_31_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_31_ : GenMapper_31_<GenModel_31_, String>() {
    override fun map(input: GenModel_31_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_31_ : GenMapper_31_<String, GenModel_31_>() {
    override fun map(input: String): GenModel_31_ {
        val parts = input.split(":")
        return GenModel_31_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_31_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_31_,
    private val saveUseCase: GenSaveUseCase_31_,
    private val deleteUseCase: GenDeleteUseCase_31_,
    private val searchUseCase: GenSearchUseCase_31_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_31_>(GenState_31_.Idle)
    val state: StateFlow<GenState_31_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_31_) {
        when (event) {
            is GenEvent_31_.Load -> loadAll()
            is GenEvent_31_.Update -> save(event.model)
            is GenEvent_31_.Delete -> delete(event.id)
            is GenEvent_31_.Refresh -> loadAll()
            is GenEvent_31_.Search -> search(event.query)
            is GenEvent_31_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_31_.Loading; _state.value = GenState_31_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_31_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_31_.Success(searchUseCase(query)) } }
}
