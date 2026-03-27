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

data class GenModel_865_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_865_ {
    data class Load(val id: Long) : GenEvent_865_()
    data class Update(val model: GenModel_865_) : GenEvent_865_()
    data class Delete(val id: Long) : GenEvent_865_()
    data object Refresh : GenEvent_865_()
    data class Search(val query: String) : GenEvent_865_()
    data class Filter(val predicate: String) : GenEvent_865_()
}

sealed class GenState_865_ {
    data object Idle : GenState_865_()
    data object Loading : GenState_865_()
    data class Success(val items: List<GenModel_865_>) : GenState_865_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_865_()
    data class Partial(val items: List<GenModel_865_>, val hasMore: Boolean) : GenState_865_()
}

interface GenRepository_865_ {
    suspend fun getAll(): List<GenModel_865_>
    suspend fun getById(id: Long): GenModel_865_?
    suspend fun save(model: GenModel_865_): GenModel_865_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_865_>
}

@Singleton
class GenRepositoryImpl_865_ @Inject constructor() : GenRepository_865_ {
    private val store = mutableMapOf<Long, GenModel_865_>()
    override suspend fun getAll(): List<GenModel_865_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_865_? = store[id]
    override suspend fun save(model: GenModel_865_): GenModel_865_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_865_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_865_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_865_ @Inject constructor(
    private val repository: GenRepositoryImpl_865_
) : GenUseCase_865_<Unit, List<GenModel_865_>> {
    override suspend fun invoke(params: Unit): List<GenModel_865_> = repository.getAll()
}

class GenSaveUseCase_865_ @Inject constructor(
    private val repository: GenRepositoryImpl_865_
) : GenUseCase_865_<GenModel_865_, GenModel_865_> {
    override suspend fun invoke(params: GenModel_865_): GenModel_865_ = repository.save(params)
}

class GenDeleteUseCase_865_ @Inject constructor(
    private val repository: GenRepositoryImpl_865_
) : GenUseCase_865_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_865_ @Inject constructor(
    private val repository: GenRepositoryImpl_865_
) : GenUseCase_865_<String, List<GenModel_865_>> {
    override suspend fun invoke(params: String): List<GenModel_865_> = repository.search(params)
}

abstract class GenMapper_865_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_865_ : GenMapper_865_<GenModel_865_, String>() {
    override fun map(input: GenModel_865_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_865_ : GenMapper_865_<String, GenModel_865_>() {
    override fun map(input: String): GenModel_865_ {
        val parts = input.split(":")
        return GenModel_865_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_865_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_865_,
    private val saveUseCase: GenSaveUseCase_865_,
    private val deleteUseCase: GenDeleteUseCase_865_,
    private val searchUseCase: GenSearchUseCase_865_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_865_>(GenState_865_.Idle)
    val state: StateFlow<GenState_865_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_865_) {
        when (event) {
            is GenEvent_865_.Load -> loadAll()
            is GenEvent_865_.Update -> save(event.model)
            is GenEvent_865_.Delete -> delete(event.id)
            is GenEvent_865_.Refresh -> loadAll()
            is GenEvent_865_.Search -> search(event.query)
            is GenEvent_865_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_865_.Loading; _state.value = GenState_865_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_865_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_865_.Success(searchUseCase(query)) } }
}
