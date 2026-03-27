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

data class GenModel_2090_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2090_ {
    data class Load(val id: Long) : GenEvent_2090_()
    data class Update(val model: GenModel_2090_) : GenEvent_2090_()
    data class Delete(val id: Long) : GenEvent_2090_()
    data object Refresh : GenEvent_2090_()
    data class Search(val query: String) : GenEvent_2090_()
    data class Filter(val predicate: String) : GenEvent_2090_()
}

sealed class GenState_2090_ {
    data object Idle : GenState_2090_()
    data object Loading : GenState_2090_()
    data class Success(val items: List<GenModel_2090_>) : GenState_2090_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2090_()
    data class Partial(val items: List<GenModel_2090_>, val hasMore: Boolean) : GenState_2090_()
}

interface GenRepository_2090_ {
    suspend fun getAll(): List<GenModel_2090_>
    suspend fun getById(id: Long): GenModel_2090_?
    suspend fun save(model: GenModel_2090_): GenModel_2090_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2090_>
}

@Singleton
class GenRepositoryImpl_2090_ @Inject constructor() : GenRepository_2090_ {
    private val store = mutableMapOf<Long, GenModel_2090_>()
    override suspend fun getAll(): List<GenModel_2090_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2090_? = store[id]
    override suspend fun save(model: GenModel_2090_): GenModel_2090_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2090_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2090_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2090_ @Inject constructor(
    private val repository: GenRepositoryImpl_2090_
) : GenUseCase_2090_<Unit, List<GenModel_2090_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2090_> = repository.getAll()
}

class GenSaveUseCase_2090_ @Inject constructor(
    private val repository: GenRepositoryImpl_2090_
) : GenUseCase_2090_<GenModel_2090_, GenModel_2090_> {
    override suspend fun invoke(params: GenModel_2090_): GenModel_2090_ = repository.save(params)
}

class GenDeleteUseCase_2090_ @Inject constructor(
    private val repository: GenRepositoryImpl_2090_
) : GenUseCase_2090_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2090_ @Inject constructor(
    private val repository: GenRepositoryImpl_2090_
) : GenUseCase_2090_<String, List<GenModel_2090_>> {
    override suspend fun invoke(params: String): List<GenModel_2090_> = repository.search(params)
}

abstract class GenMapper_2090_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2090_ : GenMapper_2090_<GenModel_2090_, String>() {
    override fun map(input: GenModel_2090_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2090_ : GenMapper_2090_<String, GenModel_2090_>() {
    override fun map(input: String): GenModel_2090_ {
        val parts = input.split(":")
        return GenModel_2090_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2090_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2090_,
    private val saveUseCase: GenSaveUseCase_2090_,
    private val deleteUseCase: GenDeleteUseCase_2090_,
    private val searchUseCase: GenSearchUseCase_2090_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2090_>(GenState_2090_.Idle)
    val state: StateFlow<GenState_2090_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2090_) {
        when (event) {
            is GenEvent_2090_.Load -> loadAll()
            is GenEvent_2090_.Update -> save(event.model)
            is GenEvent_2090_.Delete -> delete(event.id)
            is GenEvent_2090_.Refresh -> loadAll()
            is GenEvent_2090_.Search -> search(event.query)
            is GenEvent_2090_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2090_.Loading; _state.value = GenState_2090_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2090_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2090_.Success(searchUseCase(query)) } }
}
