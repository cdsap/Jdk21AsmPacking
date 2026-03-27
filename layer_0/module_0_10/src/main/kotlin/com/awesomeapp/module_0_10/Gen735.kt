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

data class GenModel_735_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_735_ {
    data class Load(val id: Long) : GenEvent_735_()
    data class Update(val model: GenModel_735_) : GenEvent_735_()
    data class Delete(val id: Long) : GenEvent_735_()
    data object Refresh : GenEvent_735_()
    data class Search(val query: String) : GenEvent_735_()
    data class Filter(val predicate: String) : GenEvent_735_()
}

sealed class GenState_735_ {
    data object Idle : GenState_735_()
    data object Loading : GenState_735_()
    data class Success(val items: List<GenModel_735_>) : GenState_735_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_735_()
    data class Partial(val items: List<GenModel_735_>, val hasMore: Boolean) : GenState_735_()
}

interface GenRepository_735_ {
    suspend fun getAll(): List<GenModel_735_>
    suspend fun getById(id: Long): GenModel_735_?
    suspend fun save(model: GenModel_735_): GenModel_735_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_735_>
}

@Singleton
class GenRepositoryImpl_735_ @Inject constructor() : GenRepository_735_ {
    private val store = mutableMapOf<Long, GenModel_735_>()
    override suspend fun getAll(): List<GenModel_735_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_735_? = store[id]
    override suspend fun save(model: GenModel_735_): GenModel_735_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_735_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_735_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_735_ @Inject constructor(
    private val repository: GenRepositoryImpl_735_
) : GenUseCase_735_<Unit, List<GenModel_735_>> {
    override suspend fun invoke(params: Unit): List<GenModel_735_> = repository.getAll()
}

class GenSaveUseCase_735_ @Inject constructor(
    private val repository: GenRepositoryImpl_735_
) : GenUseCase_735_<GenModel_735_, GenModel_735_> {
    override suspend fun invoke(params: GenModel_735_): GenModel_735_ = repository.save(params)
}

class GenDeleteUseCase_735_ @Inject constructor(
    private val repository: GenRepositoryImpl_735_
) : GenUseCase_735_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_735_ @Inject constructor(
    private val repository: GenRepositoryImpl_735_
) : GenUseCase_735_<String, List<GenModel_735_>> {
    override suspend fun invoke(params: String): List<GenModel_735_> = repository.search(params)
}

abstract class GenMapper_735_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_735_ : GenMapper_735_<GenModel_735_, String>() {
    override fun map(input: GenModel_735_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_735_ : GenMapper_735_<String, GenModel_735_>() {
    override fun map(input: String): GenModel_735_ {
        val parts = input.split(":")
        return GenModel_735_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_735_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_735_,
    private val saveUseCase: GenSaveUseCase_735_,
    private val deleteUseCase: GenDeleteUseCase_735_,
    private val searchUseCase: GenSearchUseCase_735_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_735_>(GenState_735_.Idle)
    val state: StateFlow<GenState_735_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_735_) {
        when (event) {
            is GenEvent_735_.Load -> loadAll()
            is GenEvent_735_.Update -> save(event.model)
            is GenEvent_735_.Delete -> delete(event.id)
            is GenEvent_735_.Refresh -> loadAll()
            is GenEvent_735_.Search -> search(event.query)
            is GenEvent_735_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_735_.Loading; _state.value = GenState_735_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_735_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_735_.Success(searchUseCase(query)) } }
}
