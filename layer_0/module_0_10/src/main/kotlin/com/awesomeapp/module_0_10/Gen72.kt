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

data class GenModel_72_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_72_ {
    data class Load(val id: Long) : GenEvent_72_()
    data class Update(val model: GenModel_72_) : GenEvent_72_()
    data class Delete(val id: Long) : GenEvent_72_()
    data object Refresh : GenEvent_72_()
    data class Search(val query: String) : GenEvent_72_()
    data class Filter(val predicate: String) : GenEvent_72_()
}

sealed class GenState_72_ {
    data object Idle : GenState_72_()
    data object Loading : GenState_72_()
    data class Success(val items: List<GenModel_72_>) : GenState_72_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_72_()
    data class Partial(val items: List<GenModel_72_>, val hasMore: Boolean) : GenState_72_()
}

interface GenRepository_72_ {
    suspend fun getAll(): List<GenModel_72_>
    suspend fun getById(id: Long): GenModel_72_?
    suspend fun save(model: GenModel_72_): GenModel_72_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_72_>
}

@Singleton
class GenRepositoryImpl_72_ @Inject constructor() : GenRepository_72_ {
    private val store = mutableMapOf<Long, GenModel_72_>()
    override suspend fun getAll(): List<GenModel_72_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_72_? = store[id]
    override suspend fun save(model: GenModel_72_): GenModel_72_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_72_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_72_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_72_ @Inject constructor(
    private val repository: GenRepositoryImpl_72_
) : GenUseCase_72_<Unit, List<GenModel_72_>> {
    override suspend fun invoke(params: Unit): List<GenModel_72_> = repository.getAll()
}

class GenSaveUseCase_72_ @Inject constructor(
    private val repository: GenRepositoryImpl_72_
) : GenUseCase_72_<GenModel_72_, GenModel_72_> {
    override suspend fun invoke(params: GenModel_72_): GenModel_72_ = repository.save(params)
}

class GenDeleteUseCase_72_ @Inject constructor(
    private val repository: GenRepositoryImpl_72_
) : GenUseCase_72_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_72_ @Inject constructor(
    private val repository: GenRepositoryImpl_72_
) : GenUseCase_72_<String, List<GenModel_72_>> {
    override suspend fun invoke(params: String): List<GenModel_72_> = repository.search(params)
}

abstract class GenMapper_72_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_72_ : GenMapper_72_<GenModel_72_, String>() {
    override fun map(input: GenModel_72_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_72_ : GenMapper_72_<String, GenModel_72_>() {
    override fun map(input: String): GenModel_72_ {
        val parts = input.split(":")
        return GenModel_72_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_72_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_72_,
    private val saveUseCase: GenSaveUseCase_72_,
    private val deleteUseCase: GenDeleteUseCase_72_,
    private val searchUseCase: GenSearchUseCase_72_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_72_>(GenState_72_.Idle)
    val state: StateFlow<GenState_72_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_72_) {
        when (event) {
            is GenEvent_72_.Load -> loadAll()
            is GenEvent_72_.Update -> save(event.model)
            is GenEvent_72_.Delete -> delete(event.id)
            is GenEvent_72_.Refresh -> loadAll()
            is GenEvent_72_.Search -> search(event.query)
            is GenEvent_72_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_72_.Loading; _state.value = GenState_72_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_72_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_72_.Success(searchUseCase(query)) } }
}
