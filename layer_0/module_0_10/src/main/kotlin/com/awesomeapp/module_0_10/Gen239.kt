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

data class GenModel_239_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_239_ {
    data class Load(val id: Long) : GenEvent_239_()
    data class Update(val model: GenModel_239_) : GenEvent_239_()
    data class Delete(val id: Long) : GenEvent_239_()
    data object Refresh : GenEvent_239_()
    data class Search(val query: String) : GenEvent_239_()
    data class Filter(val predicate: String) : GenEvent_239_()
}

sealed class GenState_239_ {
    data object Idle : GenState_239_()
    data object Loading : GenState_239_()
    data class Success(val items: List<GenModel_239_>) : GenState_239_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_239_()
    data class Partial(val items: List<GenModel_239_>, val hasMore: Boolean) : GenState_239_()
}

interface GenRepository_239_ {
    suspend fun getAll(): List<GenModel_239_>
    suspend fun getById(id: Long): GenModel_239_?
    suspend fun save(model: GenModel_239_): GenModel_239_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_239_>
}

@Singleton
class GenRepositoryImpl_239_ @Inject constructor() : GenRepository_239_ {
    private val store = mutableMapOf<Long, GenModel_239_>()
    override suspend fun getAll(): List<GenModel_239_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_239_? = store[id]
    override suspend fun save(model: GenModel_239_): GenModel_239_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_239_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_239_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_239_ @Inject constructor(
    private val repository: GenRepositoryImpl_239_
) : GenUseCase_239_<Unit, List<GenModel_239_>> {
    override suspend fun invoke(params: Unit): List<GenModel_239_> = repository.getAll()
}

class GenSaveUseCase_239_ @Inject constructor(
    private val repository: GenRepositoryImpl_239_
) : GenUseCase_239_<GenModel_239_, GenModel_239_> {
    override suspend fun invoke(params: GenModel_239_): GenModel_239_ = repository.save(params)
}

class GenDeleteUseCase_239_ @Inject constructor(
    private val repository: GenRepositoryImpl_239_
) : GenUseCase_239_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_239_ @Inject constructor(
    private val repository: GenRepositoryImpl_239_
) : GenUseCase_239_<String, List<GenModel_239_>> {
    override suspend fun invoke(params: String): List<GenModel_239_> = repository.search(params)
}

abstract class GenMapper_239_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_239_ : GenMapper_239_<GenModel_239_, String>() {
    override fun map(input: GenModel_239_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_239_ : GenMapper_239_<String, GenModel_239_>() {
    override fun map(input: String): GenModel_239_ {
        val parts = input.split(":")
        return GenModel_239_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_239_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_239_,
    private val saveUseCase: GenSaveUseCase_239_,
    private val deleteUseCase: GenDeleteUseCase_239_,
    private val searchUseCase: GenSearchUseCase_239_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_239_>(GenState_239_.Idle)
    val state: StateFlow<GenState_239_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_239_) {
        when (event) {
            is GenEvent_239_.Load -> loadAll()
            is GenEvent_239_.Update -> save(event.model)
            is GenEvent_239_.Delete -> delete(event.id)
            is GenEvent_239_.Refresh -> loadAll()
            is GenEvent_239_.Search -> search(event.query)
            is GenEvent_239_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_239_.Loading; _state.value = GenState_239_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_239_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_239_.Success(searchUseCase(query)) } }
}
