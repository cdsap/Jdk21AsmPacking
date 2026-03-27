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

data class GenModel_243_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_243_ {
    data class Load(val id: Long) : GenEvent_243_()
    data class Update(val model: GenModel_243_) : GenEvent_243_()
    data class Delete(val id: Long) : GenEvent_243_()
    data object Refresh : GenEvent_243_()
    data class Search(val query: String) : GenEvent_243_()
    data class Filter(val predicate: String) : GenEvent_243_()
}

sealed class GenState_243_ {
    data object Idle : GenState_243_()
    data object Loading : GenState_243_()
    data class Success(val items: List<GenModel_243_>) : GenState_243_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_243_()
    data class Partial(val items: List<GenModel_243_>, val hasMore: Boolean) : GenState_243_()
}

interface GenRepository_243_ {
    suspend fun getAll(): List<GenModel_243_>
    suspend fun getById(id: Long): GenModel_243_?
    suspend fun save(model: GenModel_243_): GenModel_243_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_243_>
}

@Singleton
class GenRepositoryImpl_243_ @Inject constructor() : GenRepository_243_ {
    private val store = mutableMapOf<Long, GenModel_243_>()
    override suspend fun getAll(): List<GenModel_243_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_243_? = store[id]
    override suspend fun save(model: GenModel_243_): GenModel_243_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_243_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_243_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_243_ @Inject constructor(
    private val repository: GenRepositoryImpl_243_
) : GenUseCase_243_<Unit, List<GenModel_243_>> {
    override suspend fun invoke(params: Unit): List<GenModel_243_> = repository.getAll()
}

class GenSaveUseCase_243_ @Inject constructor(
    private val repository: GenRepositoryImpl_243_
) : GenUseCase_243_<GenModel_243_, GenModel_243_> {
    override suspend fun invoke(params: GenModel_243_): GenModel_243_ = repository.save(params)
}

class GenDeleteUseCase_243_ @Inject constructor(
    private val repository: GenRepositoryImpl_243_
) : GenUseCase_243_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_243_ @Inject constructor(
    private val repository: GenRepositoryImpl_243_
) : GenUseCase_243_<String, List<GenModel_243_>> {
    override suspend fun invoke(params: String): List<GenModel_243_> = repository.search(params)
}

abstract class GenMapper_243_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_243_ : GenMapper_243_<GenModel_243_, String>() {
    override fun map(input: GenModel_243_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_243_ : GenMapper_243_<String, GenModel_243_>() {
    override fun map(input: String): GenModel_243_ {
        val parts = input.split(":")
        return GenModel_243_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_243_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_243_,
    private val saveUseCase: GenSaveUseCase_243_,
    private val deleteUseCase: GenDeleteUseCase_243_,
    private val searchUseCase: GenSearchUseCase_243_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_243_>(GenState_243_.Idle)
    val state: StateFlow<GenState_243_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_243_) {
        when (event) {
            is GenEvent_243_.Load -> loadAll()
            is GenEvent_243_.Update -> save(event.model)
            is GenEvent_243_.Delete -> delete(event.id)
            is GenEvent_243_.Refresh -> loadAll()
            is GenEvent_243_.Search -> search(event.query)
            is GenEvent_243_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_243_.Loading; _state.value = GenState_243_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_243_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_243_.Success(searchUseCase(query)) } }
}
