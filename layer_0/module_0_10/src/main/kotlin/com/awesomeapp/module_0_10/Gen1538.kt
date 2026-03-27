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

data class GenModel_1538_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1538_ {
    data class Load(val id: Long) : GenEvent_1538_()
    data class Update(val model: GenModel_1538_) : GenEvent_1538_()
    data class Delete(val id: Long) : GenEvent_1538_()
    data object Refresh : GenEvent_1538_()
    data class Search(val query: String) : GenEvent_1538_()
    data class Filter(val predicate: String) : GenEvent_1538_()
}

sealed class GenState_1538_ {
    data object Idle : GenState_1538_()
    data object Loading : GenState_1538_()
    data class Success(val items: List<GenModel_1538_>) : GenState_1538_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1538_()
    data class Partial(val items: List<GenModel_1538_>, val hasMore: Boolean) : GenState_1538_()
}

interface GenRepository_1538_ {
    suspend fun getAll(): List<GenModel_1538_>
    suspend fun getById(id: Long): GenModel_1538_?
    suspend fun save(model: GenModel_1538_): GenModel_1538_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1538_>
}

@Singleton
class GenRepositoryImpl_1538_ @Inject constructor() : GenRepository_1538_ {
    private val store = mutableMapOf<Long, GenModel_1538_>()
    override suspend fun getAll(): List<GenModel_1538_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1538_? = store[id]
    override suspend fun save(model: GenModel_1538_): GenModel_1538_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1538_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1538_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1538_ @Inject constructor(
    private val repository: GenRepositoryImpl_1538_
) : GenUseCase_1538_<Unit, List<GenModel_1538_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1538_> = repository.getAll()
}

class GenSaveUseCase_1538_ @Inject constructor(
    private val repository: GenRepositoryImpl_1538_
) : GenUseCase_1538_<GenModel_1538_, GenModel_1538_> {
    override suspend fun invoke(params: GenModel_1538_): GenModel_1538_ = repository.save(params)
}

class GenDeleteUseCase_1538_ @Inject constructor(
    private val repository: GenRepositoryImpl_1538_
) : GenUseCase_1538_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1538_ @Inject constructor(
    private val repository: GenRepositoryImpl_1538_
) : GenUseCase_1538_<String, List<GenModel_1538_>> {
    override suspend fun invoke(params: String): List<GenModel_1538_> = repository.search(params)
}

abstract class GenMapper_1538_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1538_ : GenMapper_1538_<GenModel_1538_, String>() {
    override fun map(input: GenModel_1538_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1538_ : GenMapper_1538_<String, GenModel_1538_>() {
    override fun map(input: String): GenModel_1538_ {
        val parts = input.split(":")
        return GenModel_1538_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1538_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1538_,
    private val saveUseCase: GenSaveUseCase_1538_,
    private val deleteUseCase: GenDeleteUseCase_1538_,
    private val searchUseCase: GenSearchUseCase_1538_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1538_>(GenState_1538_.Idle)
    val state: StateFlow<GenState_1538_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1538_) {
        when (event) {
            is GenEvent_1538_.Load -> loadAll()
            is GenEvent_1538_.Update -> save(event.model)
            is GenEvent_1538_.Delete -> delete(event.id)
            is GenEvent_1538_.Refresh -> loadAll()
            is GenEvent_1538_.Search -> search(event.query)
            is GenEvent_1538_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1538_.Loading; _state.value = GenState_1538_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1538_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1538_.Success(searchUseCase(query)) } }
}
