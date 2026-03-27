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

data class GenModel_1735_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1735_ {
    data class Load(val id: Long) : GenEvent_1735_()
    data class Update(val model: GenModel_1735_) : GenEvent_1735_()
    data class Delete(val id: Long) : GenEvent_1735_()
    data object Refresh : GenEvent_1735_()
    data class Search(val query: String) : GenEvent_1735_()
    data class Filter(val predicate: String) : GenEvent_1735_()
}

sealed class GenState_1735_ {
    data object Idle : GenState_1735_()
    data object Loading : GenState_1735_()
    data class Success(val items: List<GenModel_1735_>) : GenState_1735_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1735_()
    data class Partial(val items: List<GenModel_1735_>, val hasMore: Boolean) : GenState_1735_()
}

interface GenRepository_1735_ {
    suspend fun getAll(): List<GenModel_1735_>
    suspend fun getById(id: Long): GenModel_1735_?
    suspend fun save(model: GenModel_1735_): GenModel_1735_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1735_>
}

@Singleton
class GenRepositoryImpl_1735_ @Inject constructor() : GenRepository_1735_ {
    private val store = mutableMapOf<Long, GenModel_1735_>()
    override suspend fun getAll(): List<GenModel_1735_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1735_? = store[id]
    override suspend fun save(model: GenModel_1735_): GenModel_1735_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1735_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1735_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1735_ @Inject constructor(
    private val repository: GenRepositoryImpl_1735_
) : GenUseCase_1735_<Unit, List<GenModel_1735_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1735_> = repository.getAll()
}

class GenSaveUseCase_1735_ @Inject constructor(
    private val repository: GenRepositoryImpl_1735_
) : GenUseCase_1735_<GenModel_1735_, GenModel_1735_> {
    override suspend fun invoke(params: GenModel_1735_): GenModel_1735_ = repository.save(params)
}

class GenDeleteUseCase_1735_ @Inject constructor(
    private val repository: GenRepositoryImpl_1735_
) : GenUseCase_1735_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1735_ @Inject constructor(
    private val repository: GenRepositoryImpl_1735_
) : GenUseCase_1735_<String, List<GenModel_1735_>> {
    override suspend fun invoke(params: String): List<GenModel_1735_> = repository.search(params)
}

abstract class GenMapper_1735_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1735_ : GenMapper_1735_<GenModel_1735_, String>() {
    override fun map(input: GenModel_1735_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1735_ : GenMapper_1735_<String, GenModel_1735_>() {
    override fun map(input: String): GenModel_1735_ {
        val parts = input.split(":")
        return GenModel_1735_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1735_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1735_,
    private val saveUseCase: GenSaveUseCase_1735_,
    private val deleteUseCase: GenDeleteUseCase_1735_,
    private val searchUseCase: GenSearchUseCase_1735_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1735_>(GenState_1735_.Idle)
    val state: StateFlow<GenState_1735_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1735_) {
        when (event) {
            is GenEvent_1735_.Load -> loadAll()
            is GenEvent_1735_.Update -> save(event.model)
            is GenEvent_1735_.Delete -> delete(event.id)
            is GenEvent_1735_.Refresh -> loadAll()
            is GenEvent_1735_.Search -> search(event.query)
            is GenEvent_1735_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1735_.Loading; _state.value = GenState_1735_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1735_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1735_.Success(searchUseCase(query)) } }
}
