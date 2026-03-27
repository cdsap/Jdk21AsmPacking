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

data class GenModel_2155_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2155_ {
    data class Load(val id: Long) : GenEvent_2155_()
    data class Update(val model: GenModel_2155_) : GenEvent_2155_()
    data class Delete(val id: Long) : GenEvent_2155_()
    data object Refresh : GenEvent_2155_()
    data class Search(val query: String) : GenEvent_2155_()
    data class Filter(val predicate: String) : GenEvent_2155_()
}

sealed class GenState_2155_ {
    data object Idle : GenState_2155_()
    data object Loading : GenState_2155_()
    data class Success(val items: List<GenModel_2155_>) : GenState_2155_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2155_()
    data class Partial(val items: List<GenModel_2155_>, val hasMore: Boolean) : GenState_2155_()
}

interface GenRepository_2155_ {
    suspend fun getAll(): List<GenModel_2155_>
    suspend fun getById(id: Long): GenModel_2155_?
    suspend fun save(model: GenModel_2155_): GenModel_2155_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2155_>
}

@Singleton
class GenRepositoryImpl_2155_ @Inject constructor() : GenRepository_2155_ {
    private val store = mutableMapOf<Long, GenModel_2155_>()
    override suspend fun getAll(): List<GenModel_2155_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2155_? = store[id]
    override suspend fun save(model: GenModel_2155_): GenModel_2155_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2155_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2155_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2155_ @Inject constructor(
    private val repository: GenRepositoryImpl_2155_
) : GenUseCase_2155_<Unit, List<GenModel_2155_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2155_> = repository.getAll()
}

class GenSaveUseCase_2155_ @Inject constructor(
    private val repository: GenRepositoryImpl_2155_
) : GenUseCase_2155_<GenModel_2155_, GenModel_2155_> {
    override suspend fun invoke(params: GenModel_2155_): GenModel_2155_ = repository.save(params)
}

class GenDeleteUseCase_2155_ @Inject constructor(
    private val repository: GenRepositoryImpl_2155_
) : GenUseCase_2155_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2155_ @Inject constructor(
    private val repository: GenRepositoryImpl_2155_
) : GenUseCase_2155_<String, List<GenModel_2155_>> {
    override suspend fun invoke(params: String): List<GenModel_2155_> = repository.search(params)
}

abstract class GenMapper_2155_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2155_ : GenMapper_2155_<GenModel_2155_, String>() {
    override fun map(input: GenModel_2155_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2155_ : GenMapper_2155_<String, GenModel_2155_>() {
    override fun map(input: String): GenModel_2155_ {
        val parts = input.split(":")
        return GenModel_2155_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2155_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2155_,
    private val saveUseCase: GenSaveUseCase_2155_,
    private val deleteUseCase: GenDeleteUseCase_2155_,
    private val searchUseCase: GenSearchUseCase_2155_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2155_>(GenState_2155_.Idle)
    val state: StateFlow<GenState_2155_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2155_) {
        when (event) {
            is GenEvent_2155_.Load -> loadAll()
            is GenEvent_2155_.Update -> save(event.model)
            is GenEvent_2155_.Delete -> delete(event.id)
            is GenEvent_2155_.Refresh -> loadAll()
            is GenEvent_2155_.Search -> search(event.query)
            is GenEvent_2155_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2155_.Loading; _state.value = GenState_2155_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2155_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2155_.Success(searchUseCase(query)) } }
}
