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

data class GenModel_273_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_273_ {
    data class Load(val id: Long) : GenEvent_273_()
    data class Update(val model: GenModel_273_) : GenEvent_273_()
    data class Delete(val id: Long) : GenEvent_273_()
    data object Refresh : GenEvent_273_()
    data class Search(val query: String) : GenEvent_273_()
    data class Filter(val predicate: String) : GenEvent_273_()
}

sealed class GenState_273_ {
    data object Idle : GenState_273_()
    data object Loading : GenState_273_()
    data class Success(val items: List<GenModel_273_>) : GenState_273_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_273_()
    data class Partial(val items: List<GenModel_273_>, val hasMore: Boolean) : GenState_273_()
}

interface GenRepository_273_ {
    suspend fun getAll(): List<GenModel_273_>
    suspend fun getById(id: Long): GenModel_273_?
    suspend fun save(model: GenModel_273_): GenModel_273_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_273_>
}

@Singleton
class GenRepositoryImpl_273_ @Inject constructor() : GenRepository_273_ {
    private val store = mutableMapOf<Long, GenModel_273_>()
    override suspend fun getAll(): List<GenModel_273_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_273_? = store[id]
    override suspend fun save(model: GenModel_273_): GenModel_273_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_273_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_273_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_273_ @Inject constructor(
    private val repository: GenRepositoryImpl_273_
) : GenUseCase_273_<Unit, List<GenModel_273_>> {
    override suspend fun invoke(params: Unit): List<GenModel_273_> = repository.getAll()
}

class GenSaveUseCase_273_ @Inject constructor(
    private val repository: GenRepositoryImpl_273_
) : GenUseCase_273_<GenModel_273_, GenModel_273_> {
    override suspend fun invoke(params: GenModel_273_): GenModel_273_ = repository.save(params)
}

class GenDeleteUseCase_273_ @Inject constructor(
    private val repository: GenRepositoryImpl_273_
) : GenUseCase_273_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_273_ @Inject constructor(
    private val repository: GenRepositoryImpl_273_
) : GenUseCase_273_<String, List<GenModel_273_>> {
    override suspend fun invoke(params: String): List<GenModel_273_> = repository.search(params)
}

abstract class GenMapper_273_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_273_ : GenMapper_273_<GenModel_273_, String>() {
    override fun map(input: GenModel_273_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_273_ : GenMapper_273_<String, GenModel_273_>() {
    override fun map(input: String): GenModel_273_ {
        val parts = input.split(":")
        return GenModel_273_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_273_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_273_,
    private val saveUseCase: GenSaveUseCase_273_,
    private val deleteUseCase: GenDeleteUseCase_273_,
    private val searchUseCase: GenSearchUseCase_273_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_273_>(GenState_273_.Idle)
    val state: StateFlow<GenState_273_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_273_) {
        when (event) {
            is GenEvent_273_.Load -> loadAll()
            is GenEvent_273_.Update -> save(event.model)
            is GenEvent_273_.Delete -> delete(event.id)
            is GenEvent_273_.Refresh -> loadAll()
            is GenEvent_273_.Search -> search(event.query)
            is GenEvent_273_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_273_.Loading; _state.value = GenState_273_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_273_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_273_.Success(searchUseCase(query)) } }
}
