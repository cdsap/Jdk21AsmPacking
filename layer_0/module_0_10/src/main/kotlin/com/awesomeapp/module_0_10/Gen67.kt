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

data class GenModel_67_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_67_ {
    data class Load(val id: Long) : GenEvent_67_()
    data class Update(val model: GenModel_67_) : GenEvent_67_()
    data class Delete(val id: Long) : GenEvent_67_()
    data object Refresh : GenEvent_67_()
    data class Search(val query: String) : GenEvent_67_()
    data class Filter(val predicate: String) : GenEvent_67_()
}

sealed class GenState_67_ {
    data object Idle : GenState_67_()
    data object Loading : GenState_67_()
    data class Success(val items: List<GenModel_67_>) : GenState_67_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_67_()
    data class Partial(val items: List<GenModel_67_>, val hasMore: Boolean) : GenState_67_()
}

interface GenRepository_67_ {
    suspend fun getAll(): List<GenModel_67_>
    suspend fun getById(id: Long): GenModel_67_?
    suspend fun save(model: GenModel_67_): GenModel_67_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_67_>
}

@Singleton
class GenRepositoryImpl_67_ @Inject constructor() : GenRepository_67_ {
    private val store = mutableMapOf<Long, GenModel_67_>()
    override suspend fun getAll(): List<GenModel_67_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_67_? = store[id]
    override suspend fun save(model: GenModel_67_): GenModel_67_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_67_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_67_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_67_ @Inject constructor(
    private val repository: GenRepositoryImpl_67_
) : GenUseCase_67_<Unit, List<GenModel_67_>> {
    override suspend fun invoke(params: Unit): List<GenModel_67_> = repository.getAll()
}

class GenSaveUseCase_67_ @Inject constructor(
    private val repository: GenRepositoryImpl_67_
) : GenUseCase_67_<GenModel_67_, GenModel_67_> {
    override suspend fun invoke(params: GenModel_67_): GenModel_67_ = repository.save(params)
}

class GenDeleteUseCase_67_ @Inject constructor(
    private val repository: GenRepositoryImpl_67_
) : GenUseCase_67_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_67_ @Inject constructor(
    private val repository: GenRepositoryImpl_67_
) : GenUseCase_67_<String, List<GenModel_67_>> {
    override suspend fun invoke(params: String): List<GenModel_67_> = repository.search(params)
}

abstract class GenMapper_67_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_67_ : GenMapper_67_<GenModel_67_, String>() {
    override fun map(input: GenModel_67_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_67_ : GenMapper_67_<String, GenModel_67_>() {
    override fun map(input: String): GenModel_67_ {
        val parts = input.split(":")
        return GenModel_67_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_67_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_67_,
    private val saveUseCase: GenSaveUseCase_67_,
    private val deleteUseCase: GenDeleteUseCase_67_,
    private val searchUseCase: GenSearchUseCase_67_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_67_>(GenState_67_.Idle)
    val state: StateFlow<GenState_67_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_67_) {
        when (event) {
            is GenEvent_67_.Load -> loadAll()
            is GenEvent_67_.Update -> save(event.model)
            is GenEvent_67_.Delete -> delete(event.id)
            is GenEvent_67_.Refresh -> loadAll()
            is GenEvent_67_.Search -> search(event.query)
            is GenEvent_67_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_67_.Loading; _state.value = GenState_67_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_67_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_67_.Success(searchUseCase(query)) } }
}
