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

data class GenModel_23_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_23_ {
    data class Load(val id: Long) : GenEvent_23_()
    data class Update(val model: GenModel_23_) : GenEvent_23_()
    data class Delete(val id: Long) : GenEvent_23_()
    data object Refresh : GenEvent_23_()
    data class Search(val query: String) : GenEvent_23_()
    data class Filter(val predicate: String) : GenEvent_23_()
}

sealed class GenState_23_ {
    data object Idle : GenState_23_()
    data object Loading : GenState_23_()
    data class Success(val items: List<GenModel_23_>) : GenState_23_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_23_()
    data class Partial(val items: List<GenModel_23_>, val hasMore: Boolean) : GenState_23_()
}

interface GenRepository_23_ {
    suspend fun getAll(): List<GenModel_23_>
    suspend fun getById(id: Long): GenModel_23_?
    suspend fun save(model: GenModel_23_): GenModel_23_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_23_>
}

@Singleton
class GenRepositoryImpl_23_ @Inject constructor() : GenRepository_23_ {
    private val store = mutableMapOf<Long, GenModel_23_>()
    override suspend fun getAll(): List<GenModel_23_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_23_? = store[id]
    override suspend fun save(model: GenModel_23_): GenModel_23_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_23_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_23_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_23_ @Inject constructor(
    private val repository: GenRepositoryImpl_23_
) : GenUseCase_23_<Unit, List<GenModel_23_>> {
    override suspend fun invoke(params: Unit): List<GenModel_23_> = repository.getAll()
}

class GenSaveUseCase_23_ @Inject constructor(
    private val repository: GenRepositoryImpl_23_
) : GenUseCase_23_<GenModel_23_, GenModel_23_> {
    override suspend fun invoke(params: GenModel_23_): GenModel_23_ = repository.save(params)
}

class GenDeleteUseCase_23_ @Inject constructor(
    private val repository: GenRepositoryImpl_23_
) : GenUseCase_23_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_23_ @Inject constructor(
    private val repository: GenRepositoryImpl_23_
) : GenUseCase_23_<String, List<GenModel_23_>> {
    override suspend fun invoke(params: String): List<GenModel_23_> = repository.search(params)
}

abstract class GenMapper_23_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_23_ : GenMapper_23_<GenModel_23_, String>() {
    override fun map(input: GenModel_23_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_23_ : GenMapper_23_<String, GenModel_23_>() {
    override fun map(input: String): GenModel_23_ {
        val parts = input.split(":")
        return GenModel_23_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_23_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_23_,
    private val saveUseCase: GenSaveUseCase_23_,
    private val deleteUseCase: GenDeleteUseCase_23_,
    private val searchUseCase: GenSearchUseCase_23_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_23_>(GenState_23_.Idle)
    val state: StateFlow<GenState_23_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_23_) {
        when (event) {
            is GenEvent_23_.Load -> loadAll()
            is GenEvent_23_.Update -> save(event.model)
            is GenEvent_23_.Delete -> delete(event.id)
            is GenEvent_23_.Refresh -> loadAll()
            is GenEvent_23_.Search -> search(event.query)
            is GenEvent_23_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_23_.Loading; _state.value = GenState_23_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_23_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_23_.Success(searchUseCase(query)) } }
}
