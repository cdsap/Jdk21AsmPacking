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

data class GenModel_700_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_700_ {
    data class Load(val id: Long) : GenEvent_700_()
    data class Update(val model: GenModel_700_) : GenEvent_700_()
    data class Delete(val id: Long) : GenEvent_700_()
    data object Refresh : GenEvent_700_()
    data class Search(val query: String) : GenEvent_700_()
    data class Filter(val predicate: String) : GenEvent_700_()
}

sealed class GenState_700_ {
    data object Idle : GenState_700_()
    data object Loading : GenState_700_()
    data class Success(val items: List<GenModel_700_>) : GenState_700_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_700_()
    data class Partial(val items: List<GenModel_700_>, val hasMore: Boolean) : GenState_700_()
}

interface GenRepository_700_ {
    suspend fun getAll(): List<GenModel_700_>
    suspend fun getById(id: Long): GenModel_700_?
    suspend fun save(model: GenModel_700_): GenModel_700_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_700_>
}

@Singleton
class GenRepositoryImpl_700_ @Inject constructor() : GenRepository_700_ {
    private val store = mutableMapOf<Long, GenModel_700_>()
    override suspend fun getAll(): List<GenModel_700_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_700_? = store[id]
    override suspend fun save(model: GenModel_700_): GenModel_700_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_700_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_700_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_700_ @Inject constructor(
    private val repository: GenRepositoryImpl_700_
) : GenUseCase_700_<Unit, List<GenModel_700_>> {
    override suspend fun invoke(params: Unit): List<GenModel_700_> = repository.getAll()
}

class GenSaveUseCase_700_ @Inject constructor(
    private val repository: GenRepositoryImpl_700_
) : GenUseCase_700_<GenModel_700_, GenModel_700_> {
    override suspend fun invoke(params: GenModel_700_): GenModel_700_ = repository.save(params)
}

class GenDeleteUseCase_700_ @Inject constructor(
    private val repository: GenRepositoryImpl_700_
) : GenUseCase_700_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_700_ @Inject constructor(
    private val repository: GenRepositoryImpl_700_
) : GenUseCase_700_<String, List<GenModel_700_>> {
    override suspend fun invoke(params: String): List<GenModel_700_> = repository.search(params)
}

abstract class GenMapper_700_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_700_ : GenMapper_700_<GenModel_700_, String>() {
    override fun map(input: GenModel_700_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_700_ : GenMapper_700_<String, GenModel_700_>() {
    override fun map(input: String): GenModel_700_ {
        val parts = input.split(":")
        return GenModel_700_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_700_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_700_,
    private val saveUseCase: GenSaveUseCase_700_,
    private val deleteUseCase: GenDeleteUseCase_700_,
    private val searchUseCase: GenSearchUseCase_700_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_700_>(GenState_700_.Idle)
    val state: StateFlow<GenState_700_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_700_) {
        when (event) {
            is GenEvent_700_.Load -> loadAll()
            is GenEvent_700_.Update -> save(event.model)
            is GenEvent_700_.Delete -> delete(event.id)
            is GenEvent_700_.Refresh -> loadAll()
            is GenEvent_700_.Search -> search(event.query)
            is GenEvent_700_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_700_.Loading; _state.value = GenState_700_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_700_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_700_.Success(searchUseCase(query)) } }
}
