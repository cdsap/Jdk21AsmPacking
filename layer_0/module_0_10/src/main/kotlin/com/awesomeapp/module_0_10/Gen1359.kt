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

data class GenModel_1359_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1359_ {
    data class Load(val id: Long) : GenEvent_1359_()
    data class Update(val model: GenModel_1359_) : GenEvent_1359_()
    data class Delete(val id: Long) : GenEvent_1359_()
    data object Refresh : GenEvent_1359_()
    data class Search(val query: String) : GenEvent_1359_()
    data class Filter(val predicate: String) : GenEvent_1359_()
}

sealed class GenState_1359_ {
    data object Idle : GenState_1359_()
    data object Loading : GenState_1359_()
    data class Success(val items: List<GenModel_1359_>) : GenState_1359_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1359_()
    data class Partial(val items: List<GenModel_1359_>, val hasMore: Boolean) : GenState_1359_()
}

interface GenRepository_1359_ {
    suspend fun getAll(): List<GenModel_1359_>
    suspend fun getById(id: Long): GenModel_1359_?
    suspend fun save(model: GenModel_1359_): GenModel_1359_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1359_>
}

@Singleton
class GenRepositoryImpl_1359_ @Inject constructor() : GenRepository_1359_ {
    private val store = mutableMapOf<Long, GenModel_1359_>()
    override suspend fun getAll(): List<GenModel_1359_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1359_? = store[id]
    override suspend fun save(model: GenModel_1359_): GenModel_1359_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1359_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1359_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1359_ @Inject constructor(
    private val repository: GenRepositoryImpl_1359_
) : GenUseCase_1359_<Unit, List<GenModel_1359_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1359_> = repository.getAll()
}

class GenSaveUseCase_1359_ @Inject constructor(
    private val repository: GenRepositoryImpl_1359_
) : GenUseCase_1359_<GenModel_1359_, GenModel_1359_> {
    override suspend fun invoke(params: GenModel_1359_): GenModel_1359_ = repository.save(params)
}

class GenDeleteUseCase_1359_ @Inject constructor(
    private val repository: GenRepositoryImpl_1359_
) : GenUseCase_1359_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1359_ @Inject constructor(
    private val repository: GenRepositoryImpl_1359_
) : GenUseCase_1359_<String, List<GenModel_1359_>> {
    override suspend fun invoke(params: String): List<GenModel_1359_> = repository.search(params)
}

abstract class GenMapper_1359_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1359_ : GenMapper_1359_<GenModel_1359_, String>() {
    override fun map(input: GenModel_1359_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1359_ : GenMapper_1359_<String, GenModel_1359_>() {
    override fun map(input: String): GenModel_1359_ {
        val parts = input.split(":")
        return GenModel_1359_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1359_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1359_,
    private val saveUseCase: GenSaveUseCase_1359_,
    private val deleteUseCase: GenDeleteUseCase_1359_,
    private val searchUseCase: GenSearchUseCase_1359_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1359_>(GenState_1359_.Idle)
    val state: StateFlow<GenState_1359_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1359_) {
        when (event) {
            is GenEvent_1359_.Load -> loadAll()
            is GenEvent_1359_.Update -> save(event.model)
            is GenEvent_1359_.Delete -> delete(event.id)
            is GenEvent_1359_.Refresh -> loadAll()
            is GenEvent_1359_.Search -> search(event.query)
            is GenEvent_1359_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1359_.Loading; _state.value = GenState_1359_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1359_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1359_.Success(searchUseCase(query)) } }
}
