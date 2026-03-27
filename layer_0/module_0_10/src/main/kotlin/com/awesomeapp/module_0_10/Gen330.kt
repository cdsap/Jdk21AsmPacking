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

data class GenModel_330_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_330_ {
    data class Load(val id: Long) : GenEvent_330_()
    data class Update(val model: GenModel_330_) : GenEvent_330_()
    data class Delete(val id: Long) : GenEvent_330_()
    data object Refresh : GenEvent_330_()
    data class Search(val query: String) : GenEvent_330_()
    data class Filter(val predicate: String) : GenEvent_330_()
}

sealed class GenState_330_ {
    data object Idle : GenState_330_()
    data object Loading : GenState_330_()
    data class Success(val items: List<GenModel_330_>) : GenState_330_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_330_()
    data class Partial(val items: List<GenModel_330_>, val hasMore: Boolean) : GenState_330_()
}

interface GenRepository_330_ {
    suspend fun getAll(): List<GenModel_330_>
    suspend fun getById(id: Long): GenModel_330_?
    suspend fun save(model: GenModel_330_): GenModel_330_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_330_>
}

@Singleton
class GenRepositoryImpl_330_ @Inject constructor() : GenRepository_330_ {
    private val store = mutableMapOf<Long, GenModel_330_>()
    override suspend fun getAll(): List<GenModel_330_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_330_? = store[id]
    override suspend fun save(model: GenModel_330_): GenModel_330_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_330_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_330_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_330_ @Inject constructor(
    private val repository: GenRepositoryImpl_330_
) : GenUseCase_330_<Unit, List<GenModel_330_>> {
    override suspend fun invoke(params: Unit): List<GenModel_330_> = repository.getAll()
}

class GenSaveUseCase_330_ @Inject constructor(
    private val repository: GenRepositoryImpl_330_
) : GenUseCase_330_<GenModel_330_, GenModel_330_> {
    override suspend fun invoke(params: GenModel_330_): GenModel_330_ = repository.save(params)
}

class GenDeleteUseCase_330_ @Inject constructor(
    private val repository: GenRepositoryImpl_330_
) : GenUseCase_330_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_330_ @Inject constructor(
    private val repository: GenRepositoryImpl_330_
) : GenUseCase_330_<String, List<GenModel_330_>> {
    override suspend fun invoke(params: String): List<GenModel_330_> = repository.search(params)
}

abstract class GenMapper_330_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_330_ : GenMapper_330_<GenModel_330_, String>() {
    override fun map(input: GenModel_330_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_330_ : GenMapper_330_<String, GenModel_330_>() {
    override fun map(input: String): GenModel_330_ {
        val parts = input.split(":")
        return GenModel_330_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_330_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_330_,
    private val saveUseCase: GenSaveUseCase_330_,
    private val deleteUseCase: GenDeleteUseCase_330_,
    private val searchUseCase: GenSearchUseCase_330_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_330_>(GenState_330_.Idle)
    val state: StateFlow<GenState_330_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_330_) {
        when (event) {
            is GenEvent_330_.Load -> loadAll()
            is GenEvent_330_.Update -> save(event.model)
            is GenEvent_330_.Delete -> delete(event.id)
            is GenEvent_330_.Refresh -> loadAll()
            is GenEvent_330_.Search -> search(event.query)
            is GenEvent_330_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_330_.Loading; _state.value = GenState_330_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_330_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_330_.Success(searchUseCase(query)) } }
}
