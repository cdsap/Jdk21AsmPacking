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

data class GenModel_3155_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3155_ {
    data class Load(val id: Long) : GenEvent_3155_()
    data class Update(val model: GenModel_3155_) : GenEvent_3155_()
    data class Delete(val id: Long) : GenEvent_3155_()
    data object Refresh : GenEvent_3155_()
    data class Search(val query: String) : GenEvent_3155_()
    data class Filter(val predicate: String) : GenEvent_3155_()
}

sealed class GenState_3155_ {
    data object Idle : GenState_3155_()
    data object Loading : GenState_3155_()
    data class Success(val items: List<GenModel_3155_>) : GenState_3155_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3155_()
    data class Partial(val items: List<GenModel_3155_>, val hasMore: Boolean) : GenState_3155_()
}

interface GenRepository_3155_ {
    suspend fun getAll(): List<GenModel_3155_>
    suspend fun getById(id: Long): GenModel_3155_?
    suspend fun save(model: GenModel_3155_): GenModel_3155_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3155_>
}

@Singleton
class GenRepositoryImpl_3155_ @Inject constructor() : GenRepository_3155_ {
    private val store = mutableMapOf<Long, GenModel_3155_>()
    override suspend fun getAll(): List<GenModel_3155_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3155_? = store[id]
    override suspend fun save(model: GenModel_3155_): GenModel_3155_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3155_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3155_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3155_ @Inject constructor(
    private val repository: GenRepositoryImpl_3155_
) : GenUseCase_3155_<Unit, List<GenModel_3155_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3155_> = repository.getAll()
}

class GenSaveUseCase_3155_ @Inject constructor(
    private val repository: GenRepositoryImpl_3155_
) : GenUseCase_3155_<GenModel_3155_, GenModel_3155_> {
    override suspend fun invoke(params: GenModel_3155_): GenModel_3155_ = repository.save(params)
}

class GenDeleteUseCase_3155_ @Inject constructor(
    private val repository: GenRepositoryImpl_3155_
) : GenUseCase_3155_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3155_ @Inject constructor(
    private val repository: GenRepositoryImpl_3155_
) : GenUseCase_3155_<String, List<GenModel_3155_>> {
    override suspend fun invoke(params: String): List<GenModel_3155_> = repository.search(params)
}

abstract class GenMapper_3155_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3155_ : GenMapper_3155_<GenModel_3155_, String>() {
    override fun map(input: GenModel_3155_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3155_ : GenMapper_3155_<String, GenModel_3155_>() {
    override fun map(input: String): GenModel_3155_ {
        val parts = input.split(":")
        return GenModel_3155_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3155_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3155_,
    private val saveUseCase: GenSaveUseCase_3155_,
    private val deleteUseCase: GenDeleteUseCase_3155_,
    private val searchUseCase: GenSearchUseCase_3155_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3155_>(GenState_3155_.Idle)
    val state: StateFlow<GenState_3155_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3155_) {
        when (event) {
            is GenEvent_3155_.Load -> loadAll()
            is GenEvent_3155_.Update -> save(event.model)
            is GenEvent_3155_.Delete -> delete(event.id)
            is GenEvent_3155_.Refresh -> loadAll()
            is GenEvent_3155_.Search -> search(event.query)
            is GenEvent_3155_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3155_.Loading; _state.value = GenState_3155_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3155_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3155_.Success(searchUseCase(query)) } }
}
