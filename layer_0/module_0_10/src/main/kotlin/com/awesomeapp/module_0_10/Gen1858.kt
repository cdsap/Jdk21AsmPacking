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

data class GenModel_1858_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1858_ {
    data class Load(val id: Long) : GenEvent_1858_()
    data class Update(val model: GenModel_1858_) : GenEvent_1858_()
    data class Delete(val id: Long) : GenEvent_1858_()
    data object Refresh : GenEvent_1858_()
    data class Search(val query: String) : GenEvent_1858_()
    data class Filter(val predicate: String) : GenEvent_1858_()
}

sealed class GenState_1858_ {
    data object Idle : GenState_1858_()
    data object Loading : GenState_1858_()
    data class Success(val items: List<GenModel_1858_>) : GenState_1858_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1858_()
    data class Partial(val items: List<GenModel_1858_>, val hasMore: Boolean) : GenState_1858_()
}

interface GenRepository_1858_ {
    suspend fun getAll(): List<GenModel_1858_>
    suspend fun getById(id: Long): GenModel_1858_?
    suspend fun save(model: GenModel_1858_): GenModel_1858_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1858_>
}

@Singleton
class GenRepositoryImpl_1858_ @Inject constructor() : GenRepository_1858_ {
    private val store = mutableMapOf<Long, GenModel_1858_>()
    override suspend fun getAll(): List<GenModel_1858_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1858_? = store[id]
    override suspend fun save(model: GenModel_1858_): GenModel_1858_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1858_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1858_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1858_ @Inject constructor(
    private val repository: GenRepositoryImpl_1858_
) : GenUseCase_1858_<Unit, List<GenModel_1858_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1858_> = repository.getAll()
}

class GenSaveUseCase_1858_ @Inject constructor(
    private val repository: GenRepositoryImpl_1858_
) : GenUseCase_1858_<GenModel_1858_, GenModel_1858_> {
    override suspend fun invoke(params: GenModel_1858_): GenModel_1858_ = repository.save(params)
}

class GenDeleteUseCase_1858_ @Inject constructor(
    private val repository: GenRepositoryImpl_1858_
) : GenUseCase_1858_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1858_ @Inject constructor(
    private val repository: GenRepositoryImpl_1858_
) : GenUseCase_1858_<String, List<GenModel_1858_>> {
    override suspend fun invoke(params: String): List<GenModel_1858_> = repository.search(params)
}

abstract class GenMapper_1858_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1858_ : GenMapper_1858_<GenModel_1858_, String>() {
    override fun map(input: GenModel_1858_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1858_ : GenMapper_1858_<String, GenModel_1858_>() {
    override fun map(input: String): GenModel_1858_ {
        val parts = input.split(":")
        return GenModel_1858_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1858_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1858_,
    private val saveUseCase: GenSaveUseCase_1858_,
    private val deleteUseCase: GenDeleteUseCase_1858_,
    private val searchUseCase: GenSearchUseCase_1858_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1858_>(GenState_1858_.Idle)
    val state: StateFlow<GenState_1858_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1858_) {
        when (event) {
            is GenEvent_1858_.Load -> loadAll()
            is GenEvent_1858_.Update -> save(event.model)
            is GenEvent_1858_.Delete -> delete(event.id)
            is GenEvent_1858_.Refresh -> loadAll()
            is GenEvent_1858_.Search -> search(event.query)
            is GenEvent_1858_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1858_.Loading; _state.value = GenState_1858_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1858_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1858_.Success(searchUseCase(query)) } }
}
