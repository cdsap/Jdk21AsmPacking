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

data class GenModel_756_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_756_ {
    data class Load(val id: Long) : GenEvent_756_()
    data class Update(val model: GenModel_756_) : GenEvent_756_()
    data class Delete(val id: Long) : GenEvent_756_()
    data object Refresh : GenEvent_756_()
    data class Search(val query: String) : GenEvent_756_()
    data class Filter(val predicate: String) : GenEvent_756_()
}

sealed class GenState_756_ {
    data object Idle : GenState_756_()
    data object Loading : GenState_756_()
    data class Success(val items: List<GenModel_756_>) : GenState_756_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_756_()
    data class Partial(val items: List<GenModel_756_>, val hasMore: Boolean) : GenState_756_()
}

interface GenRepository_756_ {
    suspend fun getAll(): List<GenModel_756_>
    suspend fun getById(id: Long): GenModel_756_?
    suspend fun save(model: GenModel_756_): GenModel_756_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_756_>
}

@Singleton
class GenRepositoryImpl_756_ @Inject constructor() : GenRepository_756_ {
    private val store = mutableMapOf<Long, GenModel_756_>()
    override suspend fun getAll(): List<GenModel_756_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_756_? = store[id]
    override suspend fun save(model: GenModel_756_): GenModel_756_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_756_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_756_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_756_ @Inject constructor(
    private val repository: GenRepositoryImpl_756_
) : GenUseCase_756_<Unit, List<GenModel_756_>> {
    override suspend fun invoke(params: Unit): List<GenModel_756_> = repository.getAll()
}

class GenSaveUseCase_756_ @Inject constructor(
    private val repository: GenRepositoryImpl_756_
) : GenUseCase_756_<GenModel_756_, GenModel_756_> {
    override suspend fun invoke(params: GenModel_756_): GenModel_756_ = repository.save(params)
}

class GenDeleteUseCase_756_ @Inject constructor(
    private val repository: GenRepositoryImpl_756_
) : GenUseCase_756_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_756_ @Inject constructor(
    private val repository: GenRepositoryImpl_756_
) : GenUseCase_756_<String, List<GenModel_756_>> {
    override suspend fun invoke(params: String): List<GenModel_756_> = repository.search(params)
}

abstract class GenMapper_756_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_756_ : GenMapper_756_<GenModel_756_, String>() {
    override fun map(input: GenModel_756_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_756_ : GenMapper_756_<String, GenModel_756_>() {
    override fun map(input: String): GenModel_756_ {
        val parts = input.split(":")
        return GenModel_756_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_756_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_756_,
    private val saveUseCase: GenSaveUseCase_756_,
    private val deleteUseCase: GenDeleteUseCase_756_,
    private val searchUseCase: GenSearchUseCase_756_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_756_>(GenState_756_.Idle)
    val state: StateFlow<GenState_756_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_756_) {
        when (event) {
            is GenEvent_756_.Load -> loadAll()
            is GenEvent_756_.Update -> save(event.model)
            is GenEvent_756_.Delete -> delete(event.id)
            is GenEvent_756_.Refresh -> loadAll()
            is GenEvent_756_.Search -> search(event.query)
            is GenEvent_756_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_756_.Loading; _state.value = GenState_756_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_756_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_756_.Success(searchUseCase(query)) } }
}
