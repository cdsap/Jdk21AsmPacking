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

data class GenModel_438_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_438_ {
    data class Load(val id: Long) : GenEvent_438_()
    data class Update(val model: GenModel_438_) : GenEvent_438_()
    data class Delete(val id: Long) : GenEvent_438_()
    data object Refresh : GenEvent_438_()
    data class Search(val query: String) : GenEvent_438_()
    data class Filter(val predicate: String) : GenEvent_438_()
}

sealed class GenState_438_ {
    data object Idle : GenState_438_()
    data object Loading : GenState_438_()
    data class Success(val items: List<GenModel_438_>) : GenState_438_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_438_()
    data class Partial(val items: List<GenModel_438_>, val hasMore: Boolean) : GenState_438_()
}

interface GenRepository_438_ {
    suspend fun getAll(): List<GenModel_438_>
    suspend fun getById(id: Long): GenModel_438_?
    suspend fun save(model: GenModel_438_): GenModel_438_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_438_>
}

@Singleton
class GenRepositoryImpl_438_ @Inject constructor() : GenRepository_438_ {
    private val store = mutableMapOf<Long, GenModel_438_>()
    override suspend fun getAll(): List<GenModel_438_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_438_? = store[id]
    override suspend fun save(model: GenModel_438_): GenModel_438_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_438_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_438_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_438_ @Inject constructor(
    private val repository: GenRepositoryImpl_438_
) : GenUseCase_438_<Unit, List<GenModel_438_>> {
    override suspend fun invoke(params: Unit): List<GenModel_438_> = repository.getAll()
}

class GenSaveUseCase_438_ @Inject constructor(
    private val repository: GenRepositoryImpl_438_
) : GenUseCase_438_<GenModel_438_, GenModel_438_> {
    override suspend fun invoke(params: GenModel_438_): GenModel_438_ = repository.save(params)
}

class GenDeleteUseCase_438_ @Inject constructor(
    private val repository: GenRepositoryImpl_438_
) : GenUseCase_438_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_438_ @Inject constructor(
    private val repository: GenRepositoryImpl_438_
) : GenUseCase_438_<String, List<GenModel_438_>> {
    override suspend fun invoke(params: String): List<GenModel_438_> = repository.search(params)
}

abstract class GenMapper_438_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_438_ : GenMapper_438_<GenModel_438_, String>() {
    override fun map(input: GenModel_438_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_438_ : GenMapper_438_<String, GenModel_438_>() {
    override fun map(input: String): GenModel_438_ {
        val parts = input.split(":")
        return GenModel_438_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_438_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_438_,
    private val saveUseCase: GenSaveUseCase_438_,
    private val deleteUseCase: GenDeleteUseCase_438_,
    private val searchUseCase: GenSearchUseCase_438_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_438_>(GenState_438_.Idle)
    val state: StateFlow<GenState_438_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_438_) {
        when (event) {
            is GenEvent_438_.Load -> loadAll()
            is GenEvent_438_.Update -> save(event.model)
            is GenEvent_438_.Delete -> delete(event.id)
            is GenEvent_438_.Refresh -> loadAll()
            is GenEvent_438_.Search -> search(event.query)
            is GenEvent_438_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_438_.Loading; _state.value = GenState_438_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_438_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_438_.Success(searchUseCase(query)) } }
}
