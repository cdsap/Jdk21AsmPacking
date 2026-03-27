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

data class GenModel_165_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_165_ {
    data class Load(val id: Long) : GenEvent_165_()
    data class Update(val model: GenModel_165_) : GenEvent_165_()
    data class Delete(val id: Long) : GenEvent_165_()
    data object Refresh : GenEvent_165_()
    data class Search(val query: String) : GenEvent_165_()
    data class Filter(val predicate: String) : GenEvent_165_()
}

sealed class GenState_165_ {
    data object Idle : GenState_165_()
    data object Loading : GenState_165_()
    data class Success(val items: List<GenModel_165_>) : GenState_165_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_165_()
    data class Partial(val items: List<GenModel_165_>, val hasMore: Boolean) : GenState_165_()
}

interface GenRepository_165_ {
    suspend fun getAll(): List<GenModel_165_>
    suspend fun getById(id: Long): GenModel_165_?
    suspend fun save(model: GenModel_165_): GenModel_165_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_165_>
}

@Singleton
class GenRepositoryImpl_165_ @Inject constructor() : GenRepository_165_ {
    private val store = mutableMapOf<Long, GenModel_165_>()
    override suspend fun getAll(): List<GenModel_165_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_165_? = store[id]
    override suspend fun save(model: GenModel_165_): GenModel_165_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_165_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_165_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_165_ @Inject constructor(
    private val repository: GenRepositoryImpl_165_
) : GenUseCase_165_<Unit, List<GenModel_165_>> {
    override suspend fun invoke(params: Unit): List<GenModel_165_> = repository.getAll()
}

class GenSaveUseCase_165_ @Inject constructor(
    private val repository: GenRepositoryImpl_165_
) : GenUseCase_165_<GenModel_165_, GenModel_165_> {
    override suspend fun invoke(params: GenModel_165_): GenModel_165_ = repository.save(params)
}

class GenDeleteUseCase_165_ @Inject constructor(
    private val repository: GenRepositoryImpl_165_
) : GenUseCase_165_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_165_ @Inject constructor(
    private val repository: GenRepositoryImpl_165_
) : GenUseCase_165_<String, List<GenModel_165_>> {
    override suspend fun invoke(params: String): List<GenModel_165_> = repository.search(params)
}

abstract class GenMapper_165_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_165_ : GenMapper_165_<GenModel_165_, String>() {
    override fun map(input: GenModel_165_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_165_ : GenMapper_165_<String, GenModel_165_>() {
    override fun map(input: String): GenModel_165_ {
        val parts = input.split(":")
        return GenModel_165_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_165_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_165_,
    private val saveUseCase: GenSaveUseCase_165_,
    private val deleteUseCase: GenDeleteUseCase_165_,
    private val searchUseCase: GenSearchUseCase_165_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_165_>(GenState_165_.Idle)
    val state: StateFlow<GenState_165_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_165_) {
        when (event) {
            is GenEvent_165_.Load -> loadAll()
            is GenEvent_165_.Update -> save(event.model)
            is GenEvent_165_.Delete -> delete(event.id)
            is GenEvent_165_.Refresh -> loadAll()
            is GenEvent_165_.Search -> search(event.query)
            is GenEvent_165_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_165_.Loading; _state.value = GenState_165_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_165_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_165_.Success(searchUseCase(query)) } }
}
