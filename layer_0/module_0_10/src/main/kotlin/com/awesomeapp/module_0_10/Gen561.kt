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

data class GenModel_561_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_561_ {
    data class Load(val id: Long) : GenEvent_561_()
    data class Update(val model: GenModel_561_) : GenEvent_561_()
    data class Delete(val id: Long) : GenEvent_561_()
    data object Refresh : GenEvent_561_()
    data class Search(val query: String) : GenEvent_561_()
    data class Filter(val predicate: String) : GenEvent_561_()
}

sealed class GenState_561_ {
    data object Idle : GenState_561_()
    data object Loading : GenState_561_()
    data class Success(val items: List<GenModel_561_>) : GenState_561_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_561_()
    data class Partial(val items: List<GenModel_561_>, val hasMore: Boolean) : GenState_561_()
}

interface GenRepository_561_ {
    suspend fun getAll(): List<GenModel_561_>
    suspend fun getById(id: Long): GenModel_561_?
    suspend fun save(model: GenModel_561_): GenModel_561_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_561_>
}

@Singleton
class GenRepositoryImpl_561_ @Inject constructor() : GenRepository_561_ {
    private val store = mutableMapOf<Long, GenModel_561_>()
    override suspend fun getAll(): List<GenModel_561_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_561_? = store[id]
    override suspend fun save(model: GenModel_561_): GenModel_561_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_561_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_561_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_561_ @Inject constructor(
    private val repository: GenRepositoryImpl_561_
) : GenUseCase_561_<Unit, List<GenModel_561_>> {
    override suspend fun invoke(params: Unit): List<GenModel_561_> = repository.getAll()
}

class GenSaveUseCase_561_ @Inject constructor(
    private val repository: GenRepositoryImpl_561_
) : GenUseCase_561_<GenModel_561_, GenModel_561_> {
    override suspend fun invoke(params: GenModel_561_): GenModel_561_ = repository.save(params)
}

class GenDeleteUseCase_561_ @Inject constructor(
    private val repository: GenRepositoryImpl_561_
) : GenUseCase_561_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_561_ @Inject constructor(
    private val repository: GenRepositoryImpl_561_
) : GenUseCase_561_<String, List<GenModel_561_>> {
    override suspend fun invoke(params: String): List<GenModel_561_> = repository.search(params)
}

abstract class GenMapper_561_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_561_ : GenMapper_561_<GenModel_561_, String>() {
    override fun map(input: GenModel_561_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_561_ : GenMapper_561_<String, GenModel_561_>() {
    override fun map(input: String): GenModel_561_ {
        val parts = input.split(":")
        return GenModel_561_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_561_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_561_,
    private val saveUseCase: GenSaveUseCase_561_,
    private val deleteUseCase: GenDeleteUseCase_561_,
    private val searchUseCase: GenSearchUseCase_561_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_561_>(GenState_561_.Idle)
    val state: StateFlow<GenState_561_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_561_) {
        when (event) {
            is GenEvent_561_.Load -> loadAll()
            is GenEvent_561_.Update -> save(event.model)
            is GenEvent_561_.Delete -> delete(event.id)
            is GenEvent_561_.Refresh -> loadAll()
            is GenEvent_561_.Search -> search(event.query)
            is GenEvent_561_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_561_.Loading; _state.value = GenState_561_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_561_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_561_.Success(searchUseCase(query)) } }
}
