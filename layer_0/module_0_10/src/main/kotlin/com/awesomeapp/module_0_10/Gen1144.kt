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

data class GenModel_1144_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1144_ {
    data class Load(val id: Long) : GenEvent_1144_()
    data class Update(val model: GenModel_1144_) : GenEvent_1144_()
    data class Delete(val id: Long) : GenEvent_1144_()
    data object Refresh : GenEvent_1144_()
    data class Search(val query: String) : GenEvent_1144_()
    data class Filter(val predicate: String) : GenEvent_1144_()
}

sealed class GenState_1144_ {
    data object Idle : GenState_1144_()
    data object Loading : GenState_1144_()
    data class Success(val items: List<GenModel_1144_>) : GenState_1144_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1144_()
    data class Partial(val items: List<GenModel_1144_>, val hasMore: Boolean) : GenState_1144_()
}

interface GenRepository_1144_ {
    suspend fun getAll(): List<GenModel_1144_>
    suspend fun getById(id: Long): GenModel_1144_?
    suspend fun save(model: GenModel_1144_): GenModel_1144_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1144_>
}

@Singleton
class GenRepositoryImpl_1144_ @Inject constructor() : GenRepository_1144_ {
    private val store = mutableMapOf<Long, GenModel_1144_>()
    override suspend fun getAll(): List<GenModel_1144_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1144_? = store[id]
    override suspend fun save(model: GenModel_1144_): GenModel_1144_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1144_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1144_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1144_ @Inject constructor(
    private val repository: GenRepositoryImpl_1144_
) : GenUseCase_1144_<Unit, List<GenModel_1144_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1144_> = repository.getAll()
}

class GenSaveUseCase_1144_ @Inject constructor(
    private val repository: GenRepositoryImpl_1144_
) : GenUseCase_1144_<GenModel_1144_, GenModel_1144_> {
    override suspend fun invoke(params: GenModel_1144_): GenModel_1144_ = repository.save(params)
}

class GenDeleteUseCase_1144_ @Inject constructor(
    private val repository: GenRepositoryImpl_1144_
) : GenUseCase_1144_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1144_ @Inject constructor(
    private val repository: GenRepositoryImpl_1144_
) : GenUseCase_1144_<String, List<GenModel_1144_>> {
    override suspend fun invoke(params: String): List<GenModel_1144_> = repository.search(params)
}

abstract class GenMapper_1144_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1144_ : GenMapper_1144_<GenModel_1144_, String>() {
    override fun map(input: GenModel_1144_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1144_ : GenMapper_1144_<String, GenModel_1144_>() {
    override fun map(input: String): GenModel_1144_ {
        val parts = input.split(":")
        return GenModel_1144_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1144_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1144_,
    private val saveUseCase: GenSaveUseCase_1144_,
    private val deleteUseCase: GenDeleteUseCase_1144_,
    private val searchUseCase: GenSearchUseCase_1144_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1144_>(GenState_1144_.Idle)
    val state: StateFlow<GenState_1144_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1144_) {
        when (event) {
            is GenEvent_1144_.Load -> loadAll()
            is GenEvent_1144_.Update -> save(event.model)
            is GenEvent_1144_.Delete -> delete(event.id)
            is GenEvent_1144_.Refresh -> loadAll()
            is GenEvent_1144_.Search -> search(event.query)
            is GenEvent_1144_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1144_.Loading; _state.value = GenState_1144_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1144_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1144_.Success(searchUseCase(query)) } }
}
