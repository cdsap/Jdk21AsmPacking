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

data class GenModel_2351_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2351_ {
    data class Load(val id: Long) : GenEvent_2351_()
    data class Update(val model: GenModel_2351_) : GenEvent_2351_()
    data class Delete(val id: Long) : GenEvent_2351_()
    data object Refresh : GenEvent_2351_()
    data class Search(val query: String) : GenEvent_2351_()
    data class Filter(val predicate: String) : GenEvent_2351_()
}

sealed class GenState_2351_ {
    data object Idle : GenState_2351_()
    data object Loading : GenState_2351_()
    data class Success(val items: List<GenModel_2351_>) : GenState_2351_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2351_()
    data class Partial(val items: List<GenModel_2351_>, val hasMore: Boolean) : GenState_2351_()
}

interface GenRepository_2351_ {
    suspend fun getAll(): List<GenModel_2351_>
    suspend fun getById(id: Long): GenModel_2351_?
    suspend fun save(model: GenModel_2351_): GenModel_2351_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2351_>
}

@Singleton
class GenRepositoryImpl_2351_ @Inject constructor() : GenRepository_2351_ {
    private val store = mutableMapOf<Long, GenModel_2351_>()
    override suspend fun getAll(): List<GenModel_2351_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2351_? = store[id]
    override suspend fun save(model: GenModel_2351_): GenModel_2351_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2351_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2351_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2351_ @Inject constructor(
    private val repository: GenRepositoryImpl_2351_
) : GenUseCase_2351_<Unit, List<GenModel_2351_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2351_> = repository.getAll()
}

class GenSaveUseCase_2351_ @Inject constructor(
    private val repository: GenRepositoryImpl_2351_
) : GenUseCase_2351_<GenModel_2351_, GenModel_2351_> {
    override suspend fun invoke(params: GenModel_2351_): GenModel_2351_ = repository.save(params)
}

class GenDeleteUseCase_2351_ @Inject constructor(
    private val repository: GenRepositoryImpl_2351_
) : GenUseCase_2351_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2351_ @Inject constructor(
    private val repository: GenRepositoryImpl_2351_
) : GenUseCase_2351_<String, List<GenModel_2351_>> {
    override suspend fun invoke(params: String): List<GenModel_2351_> = repository.search(params)
}

abstract class GenMapper_2351_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2351_ : GenMapper_2351_<GenModel_2351_, String>() {
    override fun map(input: GenModel_2351_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2351_ : GenMapper_2351_<String, GenModel_2351_>() {
    override fun map(input: String): GenModel_2351_ {
        val parts = input.split(":")
        return GenModel_2351_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2351_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2351_,
    private val saveUseCase: GenSaveUseCase_2351_,
    private val deleteUseCase: GenDeleteUseCase_2351_,
    private val searchUseCase: GenSearchUseCase_2351_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2351_>(GenState_2351_.Idle)
    val state: StateFlow<GenState_2351_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2351_) {
        when (event) {
            is GenEvent_2351_.Load -> loadAll()
            is GenEvent_2351_.Update -> save(event.model)
            is GenEvent_2351_.Delete -> delete(event.id)
            is GenEvent_2351_.Refresh -> loadAll()
            is GenEvent_2351_.Search -> search(event.query)
            is GenEvent_2351_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2351_.Loading; _state.value = GenState_2351_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2351_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2351_.Success(searchUseCase(query)) } }
}
