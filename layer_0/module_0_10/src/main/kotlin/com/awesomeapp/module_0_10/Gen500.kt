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

data class GenModel_500_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_500_ {
    data class Load(val id: Long) : GenEvent_500_()
    data class Update(val model: GenModel_500_) : GenEvent_500_()
    data class Delete(val id: Long) : GenEvent_500_()
    data object Refresh : GenEvent_500_()
    data class Search(val query: String) : GenEvent_500_()
    data class Filter(val predicate: String) : GenEvent_500_()
}

sealed class GenState_500_ {
    data object Idle : GenState_500_()
    data object Loading : GenState_500_()
    data class Success(val items: List<GenModel_500_>) : GenState_500_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_500_()
    data class Partial(val items: List<GenModel_500_>, val hasMore: Boolean) : GenState_500_()
}

interface GenRepository_500_ {
    suspend fun getAll(): List<GenModel_500_>
    suspend fun getById(id: Long): GenModel_500_?
    suspend fun save(model: GenModel_500_): GenModel_500_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_500_>
}

@Singleton
class GenRepositoryImpl_500_ @Inject constructor() : GenRepository_500_ {
    private val store = mutableMapOf<Long, GenModel_500_>()
    override suspend fun getAll(): List<GenModel_500_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_500_? = store[id]
    override suspend fun save(model: GenModel_500_): GenModel_500_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_500_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_500_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_500_ @Inject constructor(
    private val repository: GenRepositoryImpl_500_
) : GenUseCase_500_<Unit, List<GenModel_500_>> {
    override suspend fun invoke(params: Unit): List<GenModel_500_> = repository.getAll()
}

class GenSaveUseCase_500_ @Inject constructor(
    private val repository: GenRepositoryImpl_500_
) : GenUseCase_500_<GenModel_500_, GenModel_500_> {
    override suspend fun invoke(params: GenModel_500_): GenModel_500_ = repository.save(params)
}

class GenDeleteUseCase_500_ @Inject constructor(
    private val repository: GenRepositoryImpl_500_
) : GenUseCase_500_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_500_ @Inject constructor(
    private val repository: GenRepositoryImpl_500_
) : GenUseCase_500_<String, List<GenModel_500_>> {
    override suspend fun invoke(params: String): List<GenModel_500_> = repository.search(params)
}

abstract class GenMapper_500_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_500_ : GenMapper_500_<GenModel_500_, String>() {
    override fun map(input: GenModel_500_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_500_ : GenMapper_500_<String, GenModel_500_>() {
    override fun map(input: String): GenModel_500_ {
        val parts = input.split(":")
        return GenModel_500_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_500_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_500_,
    private val saveUseCase: GenSaveUseCase_500_,
    private val deleteUseCase: GenDeleteUseCase_500_,
    private val searchUseCase: GenSearchUseCase_500_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_500_>(GenState_500_.Idle)
    val state: StateFlow<GenState_500_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_500_) {
        when (event) {
            is GenEvent_500_.Load -> loadAll()
            is GenEvent_500_.Update -> save(event.model)
            is GenEvent_500_.Delete -> delete(event.id)
            is GenEvent_500_.Refresh -> loadAll()
            is GenEvent_500_.Search -> search(event.query)
            is GenEvent_500_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_500_.Loading; _state.value = GenState_500_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_500_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_500_.Success(searchUseCase(query)) } }
}
