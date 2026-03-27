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

data class GenModel_853_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_853_ {
    data class Load(val id: Long) : GenEvent_853_()
    data class Update(val model: GenModel_853_) : GenEvent_853_()
    data class Delete(val id: Long) : GenEvent_853_()
    data object Refresh : GenEvent_853_()
    data class Search(val query: String) : GenEvent_853_()
    data class Filter(val predicate: String) : GenEvent_853_()
}

sealed class GenState_853_ {
    data object Idle : GenState_853_()
    data object Loading : GenState_853_()
    data class Success(val items: List<GenModel_853_>) : GenState_853_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_853_()
    data class Partial(val items: List<GenModel_853_>, val hasMore: Boolean) : GenState_853_()
}

interface GenRepository_853_ {
    suspend fun getAll(): List<GenModel_853_>
    suspend fun getById(id: Long): GenModel_853_?
    suspend fun save(model: GenModel_853_): GenModel_853_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_853_>
}

@Singleton
class GenRepositoryImpl_853_ @Inject constructor() : GenRepository_853_ {
    private val store = mutableMapOf<Long, GenModel_853_>()
    override suspend fun getAll(): List<GenModel_853_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_853_? = store[id]
    override suspend fun save(model: GenModel_853_): GenModel_853_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_853_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_853_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_853_ @Inject constructor(
    private val repository: GenRepositoryImpl_853_
) : GenUseCase_853_<Unit, List<GenModel_853_>> {
    override suspend fun invoke(params: Unit): List<GenModel_853_> = repository.getAll()
}

class GenSaveUseCase_853_ @Inject constructor(
    private val repository: GenRepositoryImpl_853_
) : GenUseCase_853_<GenModel_853_, GenModel_853_> {
    override suspend fun invoke(params: GenModel_853_): GenModel_853_ = repository.save(params)
}

class GenDeleteUseCase_853_ @Inject constructor(
    private val repository: GenRepositoryImpl_853_
) : GenUseCase_853_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_853_ @Inject constructor(
    private val repository: GenRepositoryImpl_853_
) : GenUseCase_853_<String, List<GenModel_853_>> {
    override suspend fun invoke(params: String): List<GenModel_853_> = repository.search(params)
}

abstract class GenMapper_853_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_853_ : GenMapper_853_<GenModel_853_, String>() {
    override fun map(input: GenModel_853_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_853_ : GenMapper_853_<String, GenModel_853_>() {
    override fun map(input: String): GenModel_853_ {
        val parts = input.split(":")
        return GenModel_853_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_853_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_853_,
    private val saveUseCase: GenSaveUseCase_853_,
    private val deleteUseCase: GenDeleteUseCase_853_,
    private val searchUseCase: GenSearchUseCase_853_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_853_>(GenState_853_.Idle)
    val state: StateFlow<GenState_853_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_853_) {
        when (event) {
            is GenEvent_853_.Load -> loadAll()
            is GenEvent_853_.Update -> save(event.model)
            is GenEvent_853_.Delete -> delete(event.id)
            is GenEvent_853_.Refresh -> loadAll()
            is GenEvent_853_.Search -> search(event.query)
            is GenEvent_853_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_853_.Loading; _state.value = GenState_853_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_853_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_853_.Success(searchUseCase(query)) } }
}
