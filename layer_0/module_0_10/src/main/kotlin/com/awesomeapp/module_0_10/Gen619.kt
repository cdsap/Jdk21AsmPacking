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

data class GenModel_619_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_619_ {
    data class Load(val id: Long) : GenEvent_619_()
    data class Update(val model: GenModel_619_) : GenEvent_619_()
    data class Delete(val id: Long) : GenEvent_619_()
    data object Refresh : GenEvent_619_()
    data class Search(val query: String) : GenEvent_619_()
    data class Filter(val predicate: String) : GenEvent_619_()
}

sealed class GenState_619_ {
    data object Idle : GenState_619_()
    data object Loading : GenState_619_()
    data class Success(val items: List<GenModel_619_>) : GenState_619_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_619_()
    data class Partial(val items: List<GenModel_619_>, val hasMore: Boolean) : GenState_619_()
}

interface GenRepository_619_ {
    suspend fun getAll(): List<GenModel_619_>
    suspend fun getById(id: Long): GenModel_619_?
    suspend fun save(model: GenModel_619_): GenModel_619_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_619_>
}

@Singleton
class GenRepositoryImpl_619_ @Inject constructor() : GenRepository_619_ {
    private val store = mutableMapOf<Long, GenModel_619_>()
    override suspend fun getAll(): List<GenModel_619_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_619_? = store[id]
    override suspend fun save(model: GenModel_619_): GenModel_619_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_619_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_619_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_619_ @Inject constructor(
    private val repository: GenRepositoryImpl_619_
) : GenUseCase_619_<Unit, List<GenModel_619_>> {
    override suspend fun invoke(params: Unit): List<GenModel_619_> = repository.getAll()
}

class GenSaveUseCase_619_ @Inject constructor(
    private val repository: GenRepositoryImpl_619_
) : GenUseCase_619_<GenModel_619_, GenModel_619_> {
    override suspend fun invoke(params: GenModel_619_): GenModel_619_ = repository.save(params)
}

class GenDeleteUseCase_619_ @Inject constructor(
    private val repository: GenRepositoryImpl_619_
) : GenUseCase_619_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_619_ @Inject constructor(
    private val repository: GenRepositoryImpl_619_
) : GenUseCase_619_<String, List<GenModel_619_>> {
    override suspend fun invoke(params: String): List<GenModel_619_> = repository.search(params)
}

abstract class GenMapper_619_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_619_ : GenMapper_619_<GenModel_619_, String>() {
    override fun map(input: GenModel_619_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_619_ : GenMapper_619_<String, GenModel_619_>() {
    override fun map(input: String): GenModel_619_ {
        val parts = input.split(":")
        return GenModel_619_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_619_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_619_,
    private val saveUseCase: GenSaveUseCase_619_,
    private val deleteUseCase: GenDeleteUseCase_619_,
    private val searchUseCase: GenSearchUseCase_619_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_619_>(GenState_619_.Idle)
    val state: StateFlow<GenState_619_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_619_) {
        when (event) {
            is GenEvent_619_.Load -> loadAll()
            is GenEvent_619_.Update -> save(event.model)
            is GenEvent_619_.Delete -> delete(event.id)
            is GenEvent_619_.Refresh -> loadAll()
            is GenEvent_619_.Search -> search(event.query)
            is GenEvent_619_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_619_.Loading; _state.value = GenState_619_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_619_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_619_.Success(searchUseCase(query)) } }
}
