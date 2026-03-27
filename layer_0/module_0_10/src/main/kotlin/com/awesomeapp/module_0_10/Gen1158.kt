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

data class GenModel_1158_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1158_ {
    data class Load(val id: Long) : GenEvent_1158_()
    data class Update(val model: GenModel_1158_) : GenEvent_1158_()
    data class Delete(val id: Long) : GenEvent_1158_()
    data object Refresh : GenEvent_1158_()
    data class Search(val query: String) : GenEvent_1158_()
    data class Filter(val predicate: String) : GenEvent_1158_()
}

sealed class GenState_1158_ {
    data object Idle : GenState_1158_()
    data object Loading : GenState_1158_()
    data class Success(val items: List<GenModel_1158_>) : GenState_1158_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1158_()
    data class Partial(val items: List<GenModel_1158_>, val hasMore: Boolean) : GenState_1158_()
}

interface GenRepository_1158_ {
    suspend fun getAll(): List<GenModel_1158_>
    suspend fun getById(id: Long): GenModel_1158_?
    suspend fun save(model: GenModel_1158_): GenModel_1158_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1158_>
}

@Singleton
class GenRepositoryImpl_1158_ @Inject constructor() : GenRepository_1158_ {
    private val store = mutableMapOf<Long, GenModel_1158_>()
    override suspend fun getAll(): List<GenModel_1158_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1158_? = store[id]
    override suspend fun save(model: GenModel_1158_): GenModel_1158_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1158_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1158_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1158_ @Inject constructor(
    private val repository: GenRepositoryImpl_1158_
) : GenUseCase_1158_<Unit, List<GenModel_1158_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1158_> = repository.getAll()
}

class GenSaveUseCase_1158_ @Inject constructor(
    private val repository: GenRepositoryImpl_1158_
) : GenUseCase_1158_<GenModel_1158_, GenModel_1158_> {
    override suspend fun invoke(params: GenModel_1158_): GenModel_1158_ = repository.save(params)
}

class GenDeleteUseCase_1158_ @Inject constructor(
    private val repository: GenRepositoryImpl_1158_
) : GenUseCase_1158_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1158_ @Inject constructor(
    private val repository: GenRepositoryImpl_1158_
) : GenUseCase_1158_<String, List<GenModel_1158_>> {
    override suspend fun invoke(params: String): List<GenModel_1158_> = repository.search(params)
}

abstract class GenMapper_1158_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1158_ : GenMapper_1158_<GenModel_1158_, String>() {
    override fun map(input: GenModel_1158_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1158_ : GenMapper_1158_<String, GenModel_1158_>() {
    override fun map(input: String): GenModel_1158_ {
        val parts = input.split(":")
        return GenModel_1158_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1158_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1158_,
    private val saveUseCase: GenSaveUseCase_1158_,
    private val deleteUseCase: GenDeleteUseCase_1158_,
    private val searchUseCase: GenSearchUseCase_1158_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1158_>(GenState_1158_.Idle)
    val state: StateFlow<GenState_1158_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1158_) {
        when (event) {
            is GenEvent_1158_.Load -> loadAll()
            is GenEvent_1158_.Update -> save(event.model)
            is GenEvent_1158_.Delete -> delete(event.id)
            is GenEvent_1158_.Refresh -> loadAll()
            is GenEvent_1158_.Search -> search(event.query)
            is GenEvent_1158_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1158_.Loading; _state.value = GenState_1158_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1158_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1158_.Success(searchUseCase(query)) } }
}
