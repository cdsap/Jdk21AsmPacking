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

data class GenModel_402_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_402_ {
    data class Load(val id: Long) : GenEvent_402_()
    data class Update(val model: GenModel_402_) : GenEvent_402_()
    data class Delete(val id: Long) : GenEvent_402_()
    data object Refresh : GenEvent_402_()
    data class Search(val query: String) : GenEvent_402_()
    data class Filter(val predicate: String) : GenEvent_402_()
}

sealed class GenState_402_ {
    data object Idle : GenState_402_()
    data object Loading : GenState_402_()
    data class Success(val items: List<GenModel_402_>) : GenState_402_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_402_()
    data class Partial(val items: List<GenModel_402_>, val hasMore: Boolean) : GenState_402_()
}

interface GenRepository_402_ {
    suspend fun getAll(): List<GenModel_402_>
    suspend fun getById(id: Long): GenModel_402_?
    suspend fun save(model: GenModel_402_): GenModel_402_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_402_>
}

@Singleton
class GenRepositoryImpl_402_ @Inject constructor() : GenRepository_402_ {
    private val store = mutableMapOf<Long, GenModel_402_>()
    override suspend fun getAll(): List<GenModel_402_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_402_? = store[id]
    override suspend fun save(model: GenModel_402_): GenModel_402_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_402_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_402_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_402_ @Inject constructor(
    private val repository: GenRepositoryImpl_402_
) : GenUseCase_402_<Unit, List<GenModel_402_>> {
    override suspend fun invoke(params: Unit): List<GenModel_402_> = repository.getAll()
}

class GenSaveUseCase_402_ @Inject constructor(
    private val repository: GenRepositoryImpl_402_
) : GenUseCase_402_<GenModel_402_, GenModel_402_> {
    override suspend fun invoke(params: GenModel_402_): GenModel_402_ = repository.save(params)
}

class GenDeleteUseCase_402_ @Inject constructor(
    private val repository: GenRepositoryImpl_402_
) : GenUseCase_402_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_402_ @Inject constructor(
    private val repository: GenRepositoryImpl_402_
) : GenUseCase_402_<String, List<GenModel_402_>> {
    override suspend fun invoke(params: String): List<GenModel_402_> = repository.search(params)
}

abstract class GenMapper_402_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_402_ : GenMapper_402_<GenModel_402_, String>() {
    override fun map(input: GenModel_402_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_402_ : GenMapper_402_<String, GenModel_402_>() {
    override fun map(input: String): GenModel_402_ {
        val parts = input.split(":")
        return GenModel_402_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_402_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_402_,
    private val saveUseCase: GenSaveUseCase_402_,
    private val deleteUseCase: GenDeleteUseCase_402_,
    private val searchUseCase: GenSearchUseCase_402_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_402_>(GenState_402_.Idle)
    val state: StateFlow<GenState_402_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_402_) {
        when (event) {
            is GenEvent_402_.Load -> loadAll()
            is GenEvent_402_.Update -> save(event.model)
            is GenEvent_402_.Delete -> delete(event.id)
            is GenEvent_402_.Refresh -> loadAll()
            is GenEvent_402_.Search -> search(event.query)
            is GenEvent_402_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_402_.Loading; _state.value = GenState_402_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_402_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_402_.Success(searchUseCase(query)) } }
}
