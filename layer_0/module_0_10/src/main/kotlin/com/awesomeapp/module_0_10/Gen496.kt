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

data class GenModel_496_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_496_ {
    data class Load(val id: Long) : GenEvent_496_()
    data class Update(val model: GenModel_496_) : GenEvent_496_()
    data class Delete(val id: Long) : GenEvent_496_()
    data object Refresh : GenEvent_496_()
    data class Search(val query: String) : GenEvent_496_()
    data class Filter(val predicate: String) : GenEvent_496_()
}

sealed class GenState_496_ {
    data object Idle : GenState_496_()
    data object Loading : GenState_496_()
    data class Success(val items: List<GenModel_496_>) : GenState_496_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_496_()
    data class Partial(val items: List<GenModel_496_>, val hasMore: Boolean) : GenState_496_()
}

interface GenRepository_496_ {
    suspend fun getAll(): List<GenModel_496_>
    suspend fun getById(id: Long): GenModel_496_?
    suspend fun save(model: GenModel_496_): GenModel_496_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_496_>
}

@Singleton
class GenRepositoryImpl_496_ @Inject constructor() : GenRepository_496_ {
    private val store = mutableMapOf<Long, GenModel_496_>()
    override suspend fun getAll(): List<GenModel_496_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_496_? = store[id]
    override suspend fun save(model: GenModel_496_): GenModel_496_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_496_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_496_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_496_ @Inject constructor(
    private val repository: GenRepositoryImpl_496_
) : GenUseCase_496_<Unit, List<GenModel_496_>> {
    override suspend fun invoke(params: Unit): List<GenModel_496_> = repository.getAll()
}

class GenSaveUseCase_496_ @Inject constructor(
    private val repository: GenRepositoryImpl_496_
) : GenUseCase_496_<GenModel_496_, GenModel_496_> {
    override suspend fun invoke(params: GenModel_496_): GenModel_496_ = repository.save(params)
}

class GenDeleteUseCase_496_ @Inject constructor(
    private val repository: GenRepositoryImpl_496_
) : GenUseCase_496_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_496_ @Inject constructor(
    private val repository: GenRepositoryImpl_496_
) : GenUseCase_496_<String, List<GenModel_496_>> {
    override suspend fun invoke(params: String): List<GenModel_496_> = repository.search(params)
}

abstract class GenMapper_496_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_496_ : GenMapper_496_<GenModel_496_, String>() {
    override fun map(input: GenModel_496_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_496_ : GenMapper_496_<String, GenModel_496_>() {
    override fun map(input: String): GenModel_496_ {
        val parts = input.split(":")
        return GenModel_496_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_496_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_496_,
    private val saveUseCase: GenSaveUseCase_496_,
    private val deleteUseCase: GenDeleteUseCase_496_,
    private val searchUseCase: GenSearchUseCase_496_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_496_>(GenState_496_.Idle)
    val state: StateFlow<GenState_496_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_496_) {
        when (event) {
            is GenEvent_496_.Load -> loadAll()
            is GenEvent_496_.Update -> save(event.model)
            is GenEvent_496_.Delete -> delete(event.id)
            is GenEvent_496_.Refresh -> loadAll()
            is GenEvent_496_.Search -> search(event.query)
            is GenEvent_496_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_496_.Loading; _state.value = GenState_496_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_496_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_496_.Success(searchUseCase(query)) } }
}
