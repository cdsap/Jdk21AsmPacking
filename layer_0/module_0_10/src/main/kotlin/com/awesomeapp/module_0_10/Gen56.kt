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

data class GenModel_56_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_56_ {
    data class Load(val id: Long) : GenEvent_56_()
    data class Update(val model: GenModel_56_) : GenEvent_56_()
    data class Delete(val id: Long) : GenEvent_56_()
    data object Refresh : GenEvent_56_()
    data class Search(val query: String) : GenEvent_56_()
    data class Filter(val predicate: String) : GenEvent_56_()
}

sealed class GenState_56_ {
    data object Idle : GenState_56_()
    data object Loading : GenState_56_()
    data class Success(val items: List<GenModel_56_>) : GenState_56_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_56_()
    data class Partial(val items: List<GenModel_56_>, val hasMore: Boolean) : GenState_56_()
}

interface GenRepository_56_ {
    suspend fun getAll(): List<GenModel_56_>
    suspend fun getById(id: Long): GenModel_56_?
    suspend fun save(model: GenModel_56_): GenModel_56_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_56_>
}

@Singleton
class GenRepositoryImpl_56_ @Inject constructor() : GenRepository_56_ {
    private val store = mutableMapOf<Long, GenModel_56_>()
    override suspend fun getAll(): List<GenModel_56_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_56_? = store[id]
    override suspend fun save(model: GenModel_56_): GenModel_56_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_56_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_56_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_56_ @Inject constructor(
    private val repository: GenRepositoryImpl_56_
) : GenUseCase_56_<Unit, List<GenModel_56_>> {
    override suspend fun invoke(params: Unit): List<GenModel_56_> = repository.getAll()
}

class GenSaveUseCase_56_ @Inject constructor(
    private val repository: GenRepositoryImpl_56_
) : GenUseCase_56_<GenModel_56_, GenModel_56_> {
    override suspend fun invoke(params: GenModel_56_): GenModel_56_ = repository.save(params)
}

class GenDeleteUseCase_56_ @Inject constructor(
    private val repository: GenRepositoryImpl_56_
) : GenUseCase_56_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_56_ @Inject constructor(
    private val repository: GenRepositoryImpl_56_
) : GenUseCase_56_<String, List<GenModel_56_>> {
    override suspend fun invoke(params: String): List<GenModel_56_> = repository.search(params)
}

abstract class GenMapper_56_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_56_ : GenMapper_56_<GenModel_56_, String>() {
    override fun map(input: GenModel_56_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_56_ : GenMapper_56_<String, GenModel_56_>() {
    override fun map(input: String): GenModel_56_ {
        val parts = input.split(":")
        return GenModel_56_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_56_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_56_,
    private val saveUseCase: GenSaveUseCase_56_,
    private val deleteUseCase: GenDeleteUseCase_56_,
    private val searchUseCase: GenSearchUseCase_56_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_56_>(GenState_56_.Idle)
    val state: StateFlow<GenState_56_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_56_) {
        when (event) {
            is GenEvent_56_.Load -> loadAll()
            is GenEvent_56_.Update -> save(event.model)
            is GenEvent_56_.Delete -> delete(event.id)
            is GenEvent_56_.Refresh -> loadAll()
            is GenEvent_56_.Search -> search(event.query)
            is GenEvent_56_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_56_.Loading; _state.value = GenState_56_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_56_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_56_.Success(searchUseCase(query)) } }
}
