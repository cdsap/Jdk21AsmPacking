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

data class GenModel_324_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_324_ {
    data class Load(val id: Long) : GenEvent_324_()
    data class Update(val model: GenModel_324_) : GenEvent_324_()
    data class Delete(val id: Long) : GenEvent_324_()
    data object Refresh : GenEvent_324_()
    data class Search(val query: String) : GenEvent_324_()
    data class Filter(val predicate: String) : GenEvent_324_()
}

sealed class GenState_324_ {
    data object Idle : GenState_324_()
    data object Loading : GenState_324_()
    data class Success(val items: List<GenModel_324_>) : GenState_324_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_324_()
    data class Partial(val items: List<GenModel_324_>, val hasMore: Boolean) : GenState_324_()
}

interface GenRepository_324_ {
    suspend fun getAll(): List<GenModel_324_>
    suspend fun getById(id: Long): GenModel_324_?
    suspend fun save(model: GenModel_324_): GenModel_324_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_324_>
}

@Singleton
class GenRepositoryImpl_324_ @Inject constructor() : GenRepository_324_ {
    private val store = mutableMapOf<Long, GenModel_324_>()
    override suspend fun getAll(): List<GenModel_324_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_324_? = store[id]
    override suspend fun save(model: GenModel_324_): GenModel_324_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_324_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_324_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_324_ @Inject constructor(
    private val repository: GenRepositoryImpl_324_
) : GenUseCase_324_<Unit, List<GenModel_324_>> {
    override suspend fun invoke(params: Unit): List<GenModel_324_> = repository.getAll()
}

class GenSaveUseCase_324_ @Inject constructor(
    private val repository: GenRepositoryImpl_324_
) : GenUseCase_324_<GenModel_324_, GenModel_324_> {
    override suspend fun invoke(params: GenModel_324_): GenModel_324_ = repository.save(params)
}

class GenDeleteUseCase_324_ @Inject constructor(
    private val repository: GenRepositoryImpl_324_
) : GenUseCase_324_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_324_ @Inject constructor(
    private val repository: GenRepositoryImpl_324_
) : GenUseCase_324_<String, List<GenModel_324_>> {
    override suspend fun invoke(params: String): List<GenModel_324_> = repository.search(params)
}

abstract class GenMapper_324_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_324_ : GenMapper_324_<GenModel_324_, String>() {
    override fun map(input: GenModel_324_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_324_ : GenMapper_324_<String, GenModel_324_>() {
    override fun map(input: String): GenModel_324_ {
        val parts = input.split(":")
        return GenModel_324_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_324_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_324_,
    private val saveUseCase: GenSaveUseCase_324_,
    private val deleteUseCase: GenDeleteUseCase_324_,
    private val searchUseCase: GenSearchUseCase_324_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_324_>(GenState_324_.Idle)
    val state: StateFlow<GenState_324_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_324_) {
        when (event) {
            is GenEvent_324_.Load -> loadAll()
            is GenEvent_324_.Update -> save(event.model)
            is GenEvent_324_.Delete -> delete(event.id)
            is GenEvent_324_.Refresh -> loadAll()
            is GenEvent_324_.Search -> search(event.query)
            is GenEvent_324_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_324_.Loading; _state.value = GenState_324_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_324_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_324_.Success(searchUseCase(query)) } }
}
