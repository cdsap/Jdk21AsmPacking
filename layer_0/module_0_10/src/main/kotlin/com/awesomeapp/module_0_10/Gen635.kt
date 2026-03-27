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

data class GenModel_635_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_635_ {
    data class Load(val id: Long) : GenEvent_635_()
    data class Update(val model: GenModel_635_) : GenEvent_635_()
    data class Delete(val id: Long) : GenEvent_635_()
    data object Refresh : GenEvent_635_()
    data class Search(val query: String) : GenEvent_635_()
    data class Filter(val predicate: String) : GenEvent_635_()
}

sealed class GenState_635_ {
    data object Idle : GenState_635_()
    data object Loading : GenState_635_()
    data class Success(val items: List<GenModel_635_>) : GenState_635_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_635_()
    data class Partial(val items: List<GenModel_635_>, val hasMore: Boolean) : GenState_635_()
}

interface GenRepository_635_ {
    suspend fun getAll(): List<GenModel_635_>
    suspend fun getById(id: Long): GenModel_635_?
    suspend fun save(model: GenModel_635_): GenModel_635_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_635_>
}

@Singleton
class GenRepositoryImpl_635_ @Inject constructor() : GenRepository_635_ {
    private val store = mutableMapOf<Long, GenModel_635_>()
    override suspend fun getAll(): List<GenModel_635_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_635_? = store[id]
    override suspend fun save(model: GenModel_635_): GenModel_635_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_635_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_635_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_635_ @Inject constructor(
    private val repository: GenRepositoryImpl_635_
) : GenUseCase_635_<Unit, List<GenModel_635_>> {
    override suspend fun invoke(params: Unit): List<GenModel_635_> = repository.getAll()
}

class GenSaveUseCase_635_ @Inject constructor(
    private val repository: GenRepositoryImpl_635_
) : GenUseCase_635_<GenModel_635_, GenModel_635_> {
    override suspend fun invoke(params: GenModel_635_): GenModel_635_ = repository.save(params)
}

class GenDeleteUseCase_635_ @Inject constructor(
    private val repository: GenRepositoryImpl_635_
) : GenUseCase_635_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_635_ @Inject constructor(
    private val repository: GenRepositoryImpl_635_
) : GenUseCase_635_<String, List<GenModel_635_>> {
    override suspend fun invoke(params: String): List<GenModel_635_> = repository.search(params)
}

abstract class GenMapper_635_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_635_ : GenMapper_635_<GenModel_635_, String>() {
    override fun map(input: GenModel_635_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_635_ : GenMapper_635_<String, GenModel_635_>() {
    override fun map(input: String): GenModel_635_ {
        val parts = input.split(":")
        return GenModel_635_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_635_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_635_,
    private val saveUseCase: GenSaveUseCase_635_,
    private val deleteUseCase: GenDeleteUseCase_635_,
    private val searchUseCase: GenSearchUseCase_635_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_635_>(GenState_635_.Idle)
    val state: StateFlow<GenState_635_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_635_) {
        when (event) {
            is GenEvent_635_.Load -> loadAll()
            is GenEvent_635_.Update -> save(event.model)
            is GenEvent_635_.Delete -> delete(event.id)
            is GenEvent_635_.Refresh -> loadAll()
            is GenEvent_635_.Search -> search(event.query)
            is GenEvent_635_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_635_.Loading; _state.value = GenState_635_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_635_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_635_.Success(searchUseCase(query)) } }
}
