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

data class GenModel_1071_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1071_ {
    data class Load(val id: Long) : GenEvent_1071_()
    data class Update(val model: GenModel_1071_) : GenEvent_1071_()
    data class Delete(val id: Long) : GenEvent_1071_()
    data object Refresh : GenEvent_1071_()
    data class Search(val query: String) : GenEvent_1071_()
    data class Filter(val predicate: String) : GenEvent_1071_()
}

sealed class GenState_1071_ {
    data object Idle : GenState_1071_()
    data object Loading : GenState_1071_()
    data class Success(val items: List<GenModel_1071_>) : GenState_1071_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1071_()
    data class Partial(val items: List<GenModel_1071_>, val hasMore: Boolean) : GenState_1071_()
}

interface GenRepository_1071_ {
    suspend fun getAll(): List<GenModel_1071_>
    suspend fun getById(id: Long): GenModel_1071_?
    suspend fun save(model: GenModel_1071_): GenModel_1071_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1071_>
}

@Singleton
class GenRepositoryImpl_1071_ @Inject constructor() : GenRepository_1071_ {
    private val store = mutableMapOf<Long, GenModel_1071_>()
    override suspend fun getAll(): List<GenModel_1071_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1071_? = store[id]
    override suspend fun save(model: GenModel_1071_): GenModel_1071_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1071_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1071_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1071_ @Inject constructor(
    private val repository: GenRepositoryImpl_1071_
) : GenUseCase_1071_<Unit, List<GenModel_1071_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1071_> = repository.getAll()
}

class GenSaveUseCase_1071_ @Inject constructor(
    private val repository: GenRepositoryImpl_1071_
) : GenUseCase_1071_<GenModel_1071_, GenModel_1071_> {
    override suspend fun invoke(params: GenModel_1071_): GenModel_1071_ = repository.save(params)
}

class GenDeleteUseCase_1071_ @Inject constructor(
    private val repository: GenRepositoryImpl_1071_
) : GenUseCase_1071_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1071_ @Inject constructor(
    private val repository: GenRepositoryImpl_1071_
) : GenUseCase_1071_<String, List<GenModel_1071_>> {
    override suspend fun invoke(params: String): List<GenModel_1071_> = repository.search(params)
}

abstract class GenMapper_1071_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1071_ : GenMapper_1071_<GenModel_1071_, String>() {
    override fun map(input: GenModel_1071_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1071_ : GenMapper_1071_<String, GenModel_1071_>() {
    override fun map(input: String): GenModel_1071_ {
        val parts = input.split(":")
        return GenModel_1071_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1071_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1071_,
    private val saveUseCase: GenSaveUseCase_1071_,
    private val deleteUseCase: GenDeleteUseCase_1071_,
    private val searchUseCase: GenSearchUseCase_1071_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1071_>(GenState_1071_.Idle)
    val state: StateFlow<GenState_1071_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1071_) {
        when (event) {
            is GenEvent_1071_.Load -> loadAll()
            is GenEvent_1071_.Update -> save(event.model)
            is GenEvent_1071_.Delete -> delete(event.id)
            is GenEvent_1071_.Refresh -> loadAll()
            is GenEvent_1071_.Search -> search(event.query)
            is GenEvent_1071_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1071_.Loading; _state.value = GenState_1071_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1071_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1071_.Success(searchUseCase(query)) } }
}
