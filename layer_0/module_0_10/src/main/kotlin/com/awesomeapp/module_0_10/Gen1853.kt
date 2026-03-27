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

data class GenModel_1853_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1853_ {
    data class Load(val id: Long) : GenEvent_1853_()
    data class Update(val model: GenModel_1853_) : GenEvent_1853_()
    data class Delete(val id: Long) : GenEvent_1853_()
    data object Refresh : GenEvent_1853_()
    data class Search(val query: String) : GenEvent_1853_()
    data class Filter(val predicate: String) : GenEvent_1853_()
}

sealed class GenState_1853_ {
    data object Idle : GenState_1853_()
    data object Loading : GenState_1853_()
    data class Success(val items: List<GenModel_1853_>) : GenState_1853_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1853_()
    data class Partial(val items: List<GenModel_1853_>, val hasMore: Boolean) : GenState_1853_()
}

interface GenRepository_1853_ {
    suspend fun getAll(): List<GenModel_1853_>
    suspend fun getById(id: Long): GenModel_1853_?
    suspend fun save(model: GenModel_1853_): GenModel_1853_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1853_>
}

@Singleton
class GenRepositoryImpl_1853_ @Inject constructor() : GenRepository_1853_ {
    private val store = mutableMapOf<Long, GenModel_1853_>()
    override suspend fun getAll(): List<GenModel_1853_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1853_? = store[id]
    override suspend fun save(model: GenModel_1853_): GenModel_1853_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1853_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1853_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1853_ @Inject constructor(
    private val repository: GenRepositoryImpl_1853_
) : GenUseCase_1853_<Unit, List<GenModel_1853_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1853_> = repository.getAll()
}

class GenSaveUseCase_1853_ @Inject constructor(
    private val repository: GenRepositoryImpl_1853_
) : GenUseCase_1853_<GenModel_1853_, GenModel_1853_> {
    override suspend fun invoke(params: GenModel_1853_): GenModel_1853_ = repository.save(params)
}

class GenDeleteUseCase_1853_ @Inject constructor(
    private val repository: GenRepositoryImpl_1853_
) : GenUseCase_1853_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1853_ @Inject constructor(
    private val repository: GenRepositoryImpl_1853_
) : GenUseCase_1853_<String, List<GenModel_1853_>> {
    override suspend fun invoke(params: String): List<GenModel_1853_> = repository.search(params)
}

abstract class GenMapper_1853_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1853_ : GenMapper_1853_<GenModel_1853_, String>() {
    override fun map(input: GenModel_1853_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1853_ : GenMapper_1853_<String, GenModel_1853_>() {
    override fun map(input: String): GenModel_1853_ {
        val parts = input.split(":")
        return GenModel_1853_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1853_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1853_,
    private val saveUseCase: GenSaveUseCase_1853_,
    private val deleteUseCase: GenDeleteUseCase_1853_,
    private val searchUseCase: GenSearchUseCase_1853_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1853_>(GenState_1853_.Idle)
    val state: StateFlow<GenState_1853_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1853_) {
        when (event) {
            is GenEvent_1853_.Load -> loadAll()
            is GenEvent_1853_.Update -> save(event.model)
            is GenEvent_1853_.Delete -> delete(event.id)
            is GenEvent_1853_.Refresh -> loadAll()
            is GenEvent_1853_.Search -> search(event.query)
            is GenEvent_1853_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1853_.Loading; _state.value = GenState_1853_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1853_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1853_.Success(searchUseCase(query)) } }
}
