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

data class GenModel_815_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_815_ {
    data class Load(val id: Long) : GenEvent_815_()
    data class Update(val model: GenModel_815_) : GenEvent_815_()
    data class Delete(val id: Long) : GenEvent_815_()
    data object Refresh : GenEvent_815_()
    data class Search(val query: String) : GenEvent_815_()
    data class Filter(val predicate: String) : GenEvent_815_()
}

sealed class GenState_815_ {
    data object Idle : GenState_815_()
    data object Loading : GenState_815_()
    data class Success(val items: List<GenModel_815_>) : GenState_815_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_815_()
    data class Partial(val items: List<GenModel_815_>, val hasMore: Boolean) : GenState_815_()
}

interface GenRepository_815_ {
    suspend fun getAll(): List<GenModel_815_>
    suspend fun getById(id: Long): GenModel_815_?
    suspend fun save(model: GenModel_815_): GenModel_815_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_815_>
}

@Singleton
class GenRepositoryImpl_815_ @Inject constructor() : GenRepository_815_ {
    private val store = mutableMapOf<Long, GenModel_815_>()
    override suspend fun getAll(): List<GenModel_815_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_815_? = store[id]
    override suspend fun save(model: GenModel_815_): GenModel_815_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_815_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_815_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_815_ @Inject constructor(
    private val repository: GenRepositoryImpl_815_
) : GenUseCase_815_<Unit, List<GenModel_815_>> {
    override suspend fun invoke(params: Unit): List<GenModel_815_> = repository.getAll()
}

class GenSaveUseCase_815_ @Inject constructor(
    private val repository: GenRepositoryImpl_815_
) : GenUseCase_815_<GenModel_815_, GenModel_815_> {
    override suspend fun invoke(params: GenModel_815_): GenModel_815_ = repository.save(params)
}

class GenDeleteUseCase_815_ @Inject constructor(
    private val repository: GenRepositoryImpl_815_
) : GenUseCase_815_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_815_ @Inject constructor(
    private val repository: GenRepositoryImpl_815_
) : GenUseCase_815_<String, List<GenModel_815_>> {
    override suspend fun invoke(params: String): List<GenModel_815_> = repository.search(params)
}

abstract class GenMapper_815_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_815_ : GenMapper_815_<GenModel_815_, String>() {
    override fun map(input: GenModel_815_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_815_ : GenMapper_815_<String, GenModel_815_>() {
    override fun map(input: String): GenModel_815_ {
        val parts = input.split(":")
        return GenModel_815_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_815_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_815_,
    private val saveUseCase: GenSaveUseCase_815_,
    private val deleteUseCase: GenDeleteUseCase_815_,
    private val searchUseCase: GenSearchUseCase_815_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_815_>(GenState_815_.Idle)
    val state: StateFlow<GenState_815_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_815_) {
        when (event) {
            is GenEvent_815_.Load -> loadAll()
            is GenEvent_815_.Update -> save(event.model)
            is GenEvent_815_.Delete -> delete(event.id)
            is GenEvent_815_.Refresh -> loadAll()
            is GenEvent_815_.Search -> search(event.query)
            is GenEvent_815_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_815_.Loading; _state.value = GenState_815_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_815_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_815_.Success(searchUseCase(query)) } }
}
