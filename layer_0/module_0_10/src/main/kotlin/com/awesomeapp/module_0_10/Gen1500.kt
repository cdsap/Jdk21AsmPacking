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

data class GenModel_1500_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1500_ {
    data class Load(val id: Long) : GenEvent_1500_()
    data class Update(val model: GenModel_1500_) : GenEvent_1500_()
    data class Delete(val id: Long) : GenEvent_1500_()
    data object Refresh : GenEvent_1500_()
    data class Search(val query: String) : GenEvent_1500_()
    data class Filter(val predicate: String) : GenEvent_1500_()
}

sealed class GenState_1500_ {
    data object Idle : GenState_1500_()
    data object Loading : GenState_1500_()
    data class Success(val items: List<GenModel_1500_>) : GenState_1500_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1500_()
    data class Partial(val items: List<GenModel_1500_>, val hasMore: Boolean) : GenState_1500_()
}

interface GenRepository_1500_ {
    suspend fun getAll(): List<GenModel_1500_>
    suspend fun getById(id: Long): GenModel_1500_?
    suspend fun save(model: GenModel_1500_): GenModel_1500_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1500_>
}

@Singleton
class GenRepositoryImpl_1500_ @Inject constructor() : GenRepository_1500_ {
    private val store = mutableMapOf<Long, GenModel_1500_>()
    override suspend fun getAll(): List<GenModel_1500_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1500_? = store[id]
    override suspend fun save(model: GenModel_1500_): GenModel_1500_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1500_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1500_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1500_ @Inject constructor(
    private val repository: GenRepositoryImpl_1500_
) : GenUseCase_1500_<Unit, List<GenModel_1500_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1500_> = repository.getAll()
}

class GenSaveUseCase_1500_ @Inject constructor(
    private val repository: GenRepositoryImpl_1500_
) : GenUseCase_1500_<GenModel_1500_, GenModel_1500_> {
    override suspend fun invoke(params: GenModel_1500_): GenModel_1500_ = repository.save(params)
}

class GenDeleteUseCase_1500_ @Inject constructor(
    private val repository: GenRepositoryImpl_1500_
) : GenUseCase_1500_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1500_ @Inject constructor(
    private val repository: GenRepositoryImpl_1500_
) : GenUseCase_1500_<String, List<GenModel_1500_>> {
    override suspend fun invoke(params: String): List<GenModel_1500_> = repository.search(params)
}

abstract class GenMapper_1500_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1500_ : GenMapper_1500_<GenModel_1500_, String>() {
    override fun map(input: GenModel_1500_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1500_ : GenMapper_1500_<String, GenModel_1500_>() {
    override fun map(input: String): GenModel_1500_ {
        val parts = input.split(":")
        return GenModel_1500_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1500_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1500_,
    private val saveUseCase: GenSaveUseCase_1500_,
    private val deleteUseCase: GenDeleteUseCase_1500_,
    private val searchUseCase: GenSearchUseCase_1500_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1500_>(GenState_1500_.Idle)
    val state: StateFlow<GenState_1500_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1500_) {
        when (event) {
            is GenEvent_1500_.Load -> loadAll()
            is GenEvent_1500_.Update -> save(event.model)
            is GenEvent_1500_.Delete -> delete(event.id)
            is GenEvent_1500_.Refresh -> loadAll()
            is GenEvent_1500_.Search -> search(event.query)
            is GenEvent_1500_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1500_.Loading; _state.value = GenState_1500_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1500_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1500_.Success(searchUseCase(query)) } }
}
