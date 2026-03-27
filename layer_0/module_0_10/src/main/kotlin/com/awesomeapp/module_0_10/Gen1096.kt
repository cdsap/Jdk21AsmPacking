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

data class GenModel_1096_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1096_ {
    data class Load(val id: Long) : GenEvent_1096_()
    data class Update(val model: GenModel_1096_) : GenEvent_1096_()
    data class Delete(val id: Long) : GenEvent_1096_()
    data object Refresh : GenEvent_1096_()
    data class Search(val query: String) : GenEvent_1096_()
    data class Filter(val predicate: String) : GenEvent_1096_()
}

sealed class GenState_1096_ {
    data object Idle : GenState_1096_()
    data object Loading : GenState_1096_()
    data class Success(val items: List<GenModel_1096_>) : GenState_1096_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1096_()
    data class Partial(val items: List<GenModel_1096_>, val hasMore: Boolean) : GenState_1096_()
}

interface GenRepository_1096_ {
    suspend fun getAll(): List<GenModel_1096_>
    suspend fun getById(id: Long): GenModel_1096_?
    suspend fun save(model: GenModel_1096_): GenModel_1096_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1096_>
}

@Singleton
class GenRepositoryImpl_1096_ @Inject constructor() : GenRepository_1096_ {
    private val store = mutableMapOf<Long, GenModel_1096_>()
    override suspend fun getAll(): List<GenModel_1096_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1096_? = store[id]
    override suspend fun save(model: GenModel_1096_): GenModel_1096_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1096_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1096_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1096_ @Inject constructor(
    private val repository: GenRepositoryImpl_1096_
) : GenUseCase_1096_<Unit, List<GenModel_1096_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1096_> = repository.getAll()
}

class GenSaveUseCase_1096_ @Inject constructor(
    private val repository: GenRepositoryImpl_1096_
) : GenUseCase_1096_<GenModel_1096_, GenModel_1096_> {
    override suspend fun invoke(params: GenModel_1096_): GenModel_1096_ = repository.save(params)
}

class GenDeleteUseCase_1096_ @Inject constructor(
    private val repository: GenRepositoryImpl_1096_
) : GenUseCase_1096_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1096_ @Inject constructor(
    private val repository: GenRepositoryImpl_1096_
) : GenUseCase_1096_<String, List<GenModel_1096_>> {
    override suspend fun invoke(params: String): List<GenModel_1096_> = repository.search(params)
}

abstract class GenMapper_1096_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1096_ : GenMapper_1096_<GenModel_1096_, String>() {
    override fun map(input: GenModel_1096_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1096_ : GenMapper_1096_<String, GenModel_1096_>() {
    override fun map(input: String): GenModel_1096_ {
        val parts = input.split(":")
        return GenModel_1096_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1096_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1096_,
    private val saveUseCase: GenSaveUseCase_1096_,
    private val deleteUseCase: GenDeleteUseCase_1096_,
    private val searchUseCase: GenSearchUseCase_1096_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1096_>(GenState_1096_.Idle)
    val state: StateFlow<GenState_1096_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1096_) {
        when (event) {
            is GenEvent_1096_.Load -> loadAll()
            is GenEvent_1096_.Update -> save(event.model)
            is GenEvent_1096_.Delete -> delete(event.id)
            is GenEvent_1096_.Refresh -> loadAll()
            is GenEvent_1096_.Search -> search(event.query)
            is GenEvent_1096_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1096_.Loading; _state.value = GenState_1096_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1096_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1096_.Success(searchUseCase(query)) } }
}
