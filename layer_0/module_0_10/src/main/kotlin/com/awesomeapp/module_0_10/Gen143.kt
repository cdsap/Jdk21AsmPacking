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

data class GenModel_143_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_143_ {
    data class Load(val id: Long) : GenEvent_143_()
    data class Update(val model: GenModel_143_) : GenEvent_143_()
    data class Delete(val id: Long) : GenEvent_143_()
    data object Refresh : GenEvent_143_()
    data class Search(val query: String) : GenEvent_143_()
    data class Filter(val predicate: String) : GenEvent_143_()
}

sealed class GenState_143_ {
    data object Idle : GenState_143_()
    data object Loading : GenState_143_()
    data class Success(val items: List<GenModel_143_>) : GenState_143_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_143_()
    data class Partial(val items: List<GenModel_143_>, val hasMore: Boolean) : GenState_143_()
}

interface GenRepository_143_ {
    suspend fun getAll(): List<GenModel_143_>
    suspend fun getById(id: Long): GenModel_143_?
    suspend fun save(model: GenModel_143_): GenModel_143_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_143_>
}

@Singleton
class GenRepositoryImpl_143_ @Inject constructor() : GenRepository_143_ {
    private val store = mutableMapOf<Long, GenModel_143_>()
    override suspend fun getAll(): List<GenModel_143_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_143_? = store[id]
    override suspend fun save(model: GenModel_143_): GenModel_143_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_143_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_143_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_143_ @Inject constructor(
    private val repository: GenRepositoryImpl_143_
) : GenUseCase_143_<Unit, List<GenModel_143_>> {
    override suspend fun invoke(params: Unit): List<GenModel_143_> = repository.getAll()
}

class GenSaveUseCase_143_ @Inject constructor(
    private val repository: GenRepositoryImpl_143_
) : GenUseCase_143_<GenModel_143_, GenModel_143_> {
    override suspend fun invoke(params: GenModel_143_): GenModel_143_ = repository.save(params)
}

class GenDeleteUseCase_143_ @Inject constructor(
    private val repository: GenRepositoryImpl_143_
) : GenUseCase_143_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_143_ @Inject constructor(
    private val repository: GenRepositoryImpl_143_
) : GenUseCase_143_<String, List<GenModel_143_>> {
    override suspend fun invoke(params: String): List<GenModel_143_> = repository.search(params)
}

abstract class GenMapper_143_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_143_ : GenMapper_143_<GenModel_143_, String>() {
    override fun map(input: GenModel_143_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_143_ : GenMapper_143_<String, GenModel_143_>() {
    override fun map(input: String): GenModel_143_ {
        val parts = input.split(":")
        return GenModel_143_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_143_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_143_,
    private val saveUseCase: GenSaveUseCase_143_,
    private val deleteUseCase: GenDeleteUseCase_143_,
    private val searchUseCase: GenSearchUseCase_143_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_143_>(GenState_143_.Idle)
    val state: StateFlow<GenState_143_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_143_) {
        when (event) {
            is GenEvent_143_.Load -> loadAll()
            is GenEvent_143_.Update -> save(event.model)
            is GenEvent_143_.Delete -> delete(event.id)
            is GenEvent_143_.Refresh -> loadAll()
            is GenEvent_143_.Search -> search(event.query)
            is GenEvent_143_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_143_.Loading; _state.value = GenState_143_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_143_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_143_.Success(searchUseCase(query)) } }
}
