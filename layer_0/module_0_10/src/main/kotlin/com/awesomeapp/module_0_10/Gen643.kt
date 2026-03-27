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

data class GenModel_643_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_643_ {
    data class Load(val id: Long) : GenEvent_643_()
    data class Update(val model: GenModel_643_) : GenEvent_643_()
    data class Delete(val id: Long) : GenEvent_643_()
    data object Refresh : GenEvent_643_()
    data class Search(val query: String) : GenEvent_643_()
    data class Filter(val predicate: String) : GenEvent_643_()
}

sealed class GenState_643_ {
    data object Idle : GenState_643_()
    data object Loading : GenState_643_()
    data class Success(val items: List<GenModel_643_>) : GenState_643_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_643_()
    data class Partial(val items: List<GenModel_643_>, val hasMore: Boolean) : GenState_643_()
}

interface GenRepository_643_ {
    suspend fun getAll(): List<GenModel_643_>
    suspend fun getById(id: Long): GenModel_643_?
    suspend fun save(model: GenModel_643_): GenModel_643_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_643_>
}

@Singleton
class GenRepositoryImpl_643_ @Inject constructor() : GenRepository_643_ {
    private val store = mutableMapOf<Long, GenModel_643_>()
    override suspend fun getAll(): List<GenModel_643_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_643_? = store[id]
    override suspend fun save(model: GenModel_643_): GenModel_643_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_643_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_643_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_643_ @Inject constructor(
    private val repository: GenRepositoryImpl_643_
) : GenUseCase_643_<Unit, List<GenModel_643_>> {
    override suspend fun invoke(params: Unit): List<GenModel_643_> = repository.getAll()
}

class GenSaveUseCase_643_ @Inject constructor(
    private val repository: GenRepositoryImpl_643_
) : GenUseCase_643_<GenModel_643_, GenModel_643_> {
    override suspend fun invoke(params: GenModel_643_): GenModel_643_ = repository.save(params)
}

class GenDeleteUseCase_643_ @Inject constructor(
    private val repository: GenRepositoryImpl_643_
) : GenUseCase_643_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_643_ @Inject constructor(
    private val repository: GenRepositoryImpl_643_
) : GenUseCase_643_<String, List<GenModel_643_>> {
    override suspend fun invoke(params: String): List<GenModel_643_> = repository.search(params)
}

abstract class GenMapper_643_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_643_ : GenMapper_643_<GenModel_643_, String>() {
    override fun map(input: GenModel_643_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_643_ : GenMapper_643_<String, GenModel_643_>() {
    override fun map(input: String): GenModel_643_ {
        val parts = input.split(":")
        return GenModel_643_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_643_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_643_,
    private val saveUseCase: GenSaveUseCase_643_,
    private val deleteUseCase: GenDeleteUseCase_643_,
    private val searchUseCase: GenSearchUseCase_643_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_643_>(GenState_643_.Idle)
    val state: StateFlow<GenState_643_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_643_) {
        when (event) {
            is GenEvent_643_.Load -> loadAll()
            is GenEvent_643_.Update -> save(event.model)
            is GenEvent_643_.Delete -> delete(event.id)
            is GenEvent_643_.Refresh -> loadAll()
            is GenEvent_643_.Search -> search(event.query)
            is GenEvent_643_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_643_.Loading; _state.value = GenState_643_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_643_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_643_.Success(searchUseCase(query)) } }
}
