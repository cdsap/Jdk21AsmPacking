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

data class GenModel_1865_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1865_ {
    data class Load(val id: Long) : GenEvent_1865_()
    data class Update(val model: GenModel_1865_) : GenEvent_1865_()
    data class Delete(val id: Long) : GenEvent_1865_()
    data object Refresh : GenEvent_1865_()
    data class Search(val query: String) : GenEvent_1865_()
    data class Filter(val predicate: String) : GenEvent_1865_()
}

sealed class GenState_1865_ {
    data object Idle : GenState_1865_()
    data object Loading : GenState_1865_()
    data class Success(val items: List<GenModel_1865_>) : GenState_1865_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1865_()
    data class Partial(val items: List<GenModel_1865_>, val hasMore: Boolean) : GenState_1865_()
}

interface GenRepository_1865_ {
    suspend fun getAll(): List<GenModel_1865_>
    suspend fun getById(id: Long): GenModel_1865_?
    suspend fun save(model: GenModel_1865_): GenModel_1865_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1865_>
}

@Singleton
class GenRepositoryImpl_1865_ @Inject constructor() : GenRepository_1865_ {
    private val store = mutableMapOf<Long, GenModel_1865_>()
    override suspend fun getAll(): List<GenModel_1865_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1865_? = store[id]
    override suspend fun save(model: GenModel_1865_): GenModel_1865_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1865_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1865_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1865_ @Inject constructor(
    private val repository: GenRepositoryImpl_1865_
) : GenUseCase_1865_<Unit, List<GenModel_1865_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1865_> = repository.getAll()
}

class GenSaveUseCase_1865_ @Inject constructor(
    private val repository: GenRepositoryImpl_1865_
) : GenUseCase_1865_<GenModel_1865_, GenModel_1865_> {
    override suspend fun invoke(params: GenModel_1865_): GenModel_1865_ = repository.save(params)
}

class GenDeleteUseCase_1865_ @Inject constructor(
    private val repository: GenRepositoryImpl_1865_
) : GenUseCase_1865_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1865_ @Inject constructor(
    private val repository: GenRepositoryImpl_1865_
) : GenUseCase_1865_<String, List<GenModel_1865_>> {
    override suspend fun invoke(params: String): List<GenModel_1865_> = repository.search(params)
}

abstract class GenMapper_1865_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1865_ : GenMapper_1865_<GenModel_1865_, String>() {
    override fun map(input: GenModel_1865_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1865_ : GenMapper_1865_<String, GenModel_1865_>() {
    override fun map(input: String): GenModel_1865_ {
        val parts = input.split(":")
        return GenModel_1865_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1865_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1865_,
    private val saveUseCase: GenSaveUseCase_1865_,
    private val deleteUseCase: GenDeleteUseCase_1865_,
    private val searchUseCase: GenSearchUseCase_1865_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1865_>(GenState_1865_.Idle)
    val state: StateFlow<GenState_1865_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1865_) {
        when (event) {
            is GenEvent_1865_.Load -> loadAll()
            is GenEvent_1865_.Update -> save(event.model)
            is GenEvent_1865_.Delete -> delete(event.id)
            is GenEvent_1865_.Refresh -> loadAll()
            is GenEvent_1865_.Search -> search(event.query)
            is GenEvent_1865_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1865_.Loading; _state.value = GenState_1865_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1865_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1865_.Success(searchUseCase(query)) } }
}
