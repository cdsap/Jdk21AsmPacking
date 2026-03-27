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

data class GenModel_158_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_158_ {
    data class Load(val id: Long) : GenEvent_158_()
    data class Update(val model: GenModel_158_) : GenEvent_158_()
    data class Delete(val id: Long) : GenEvent_158_()
    data object Refresh : GenEvent_158_()
    data class Search(val query: String) : GenEvent_158_()
    data class Filter(val predicate: String) : GenEvent_158_()
}

sealed class GenState_158_ {
    data object Idle : GenState_158_()
    data object Loading : GenState_158_()
    data class Success(val items: List<GenModel_158_>) : GenState_158_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_158_()
    data class Partial(val items: List<GenModel_158_>, val hasMore: Boolean) : GenState_158_()
}

interface GenRepository_158_ {
    suspend fun getAll(): List<GenModel_158_>
    suspend fun getById(id: Long): GenModel_158_?
    suspend fun save(model: GenModel_158_): GenModel_158_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_158_>
}

@Singleton
class GenRepositoryImpl_158_ @Inject constructor() : GenRepository_158_ {
    private val store = mutableMapOf<Long, GenModel_158_>()
    override suspend fun getAll(): List<GenModel_158_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_158_? = store[id]
    override suspend fun save(model: GenModel_158_): GenModel_158_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_158_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_158_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_158_ @Inject constructor(
    private val repository: GenRepositoryImpl_158_
) : GenUseCase_158_<Unit, List<GenModel_158_>> {
    override suspend fun invoke(params: Unit): List<GenModel_158_> = repository.getAll()
}

class GenSaveUseCase_158_ @Inject constructor(
    private val repository: GenRepositoryImpl_158_
) : GenUseCase_158_<GenModel_158_, GenModel_158_> {
    override suspend fun invoke(params: GenModel_158_): GenModel_158_ = repository.save(params)
}

class GenDeleteUseCase_158_ @Inject constructor(
    private val repository: GenRepositoryImpl_158_
) : GenUseCase_158_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_158_ @Inject constructor(
    private val repository: GenRepositoryImpl_158_
) : GenUseCase_158_<String, List<GenModel_158_>> {
    override suspend fun invoke(params: String): List<GenModel_158_> = repository.search(params)
}

abstract class GenMapper_158_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_158_ : GenMapper_158_<GenModel_158_, String>() {
    override fun map(input: GenModel_158_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_158_ : GenMapper_158_<String, GenModel_158_>() {
    override fun map(input: String): GenModel_158_ {
        val parts = input.split(":")
        return GenModel_158_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_158_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_158_,
    private val saveUseCase: GenSaveUseCase_158_,
    private val deleteUseCase: GenDeleteUseCase_158_,
    private val searchUseCase: GenSearchUseCase_158_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_158_>(GenState_158_.Idle)
    val state: StateFlow<GenState_158_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_158_) {
        when (event) {
            is GenEvent_158_.Load -> loadAll()
            is GenEvent_158_.Update -> save(event.model)
            is GenEvent_158_.Delete -> delete(event.id)
            is GenEvent_158_.Refresh -> loadAll()
            is GenEvent_158_.Search -> search(event.query)
            is GenEvent_158_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_158_.Loading; _state.value = GenState_158_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_158_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_158_.Success(searchUseCase(query)) } }
}
