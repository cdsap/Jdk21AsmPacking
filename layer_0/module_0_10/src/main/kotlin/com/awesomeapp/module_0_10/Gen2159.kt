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

data class GenModel_2159_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2159_ {
    data class Load(val id: Long) : GenEvent_2159_()
    data class Update(val model: GenModel_2159_) : GenEvent_2159_()
    data class Delete(val id: Long) : GenEvent_2159_()
    data object Refresh : GenEvent_2159_()
    data class Search(val query: String) : GenEvent_2159_()
    data class Filter(val predicate: String) : GenEvent_2159_()
}

sealed class GenState_2159_ {
    data object Idle : GenState_2159_()
    data object Loading : GenState_2159_()
    data class Success(val items: List<GenModel_2159_>) : GenState_2159_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2159_()
    data class Partial(val items: List<GenModel_2159_>, val hasMore: Boolean) : GenState_2159_()
}

interface GenRepository_2159_ {
    suspend fun getAll(): List<GenModel_2159_>
    suspend fun getById(id: Long): GenModel_2159_?
    suspend fun save(model: GenModel_2159_): GenModel_2159_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2159_>
}

@Singleton
class GenRepositoryImpl_2159_ @Inject constructor() : GenRepository_2159_ {
    private val store = mutableMapOf<Long, GenModel_2159_>()
    override suspend fun getAll(): List<GenModel_2159_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2159_? = store[id]
    override suspend fun save(model: GenModel_2159_): GenModel_2159_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2159_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2159_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2159_ @Inject constructor(
    private val repository: GenRepositoryImpl_2159_
) : GenUseCase_2159_<Unit, List<GenModel_2159_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2159_> = repository.getAll()
}

class GenSaveUseCase_2159_ @Inject constructor(
    private val repository: GenRepositoryImpl_2159_
) : GenUseCase_2159_<GenModel_2159_, GenModel_2159_> {
    override suspend fun invoke(params: GenModel_2159_): GenModel_2159_ = repository.save(params)
}

class GenDeleteUseCase_2159_ @Inject constructor(
    private val repository: GenRepositoryImpl_2159_
) : GenUseCase_2159_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2159_ @Inject constructor(
    private val repository: GenRepositoryImpl_2159_
) : GenUseCase_2159_<String, List<GenModel_2159_>> {
    override suspend fun invoke(params: String): List<GenModel_2159_> = repository.search(params)
}

abstract class GenMapper_2159_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2159_ : GenMapper_2159_<GenModel_2159_, String>() {
    override fun map(input: GenModel_2159_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2159_ : GenMapper_2159_<String, GenModel_2159_>() {
    override fun map(input: String): GenModel_2159_ {
        val parts = input.split(":")
        return GenModel_2159_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2159_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2159_,
    private val saveUseCase: GenSaveUseCase_2159_,
    private val deleteUseCase: GenDeleteUseCase_2159_,
    private val searchUseCase: GenSearchUseCase_2159_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2159_>(GenState_2159_.Idle)
    val state: StateFlow<GenState_2159_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2159_) {
        when (event) {
            is GenEvent_2159_.Load -> loadAll()
            is GenEvent_2159_.Update -> save(event.model)
            is GenEvent_2159_.Delete -> delete(event.id)
            is GenEvent_2159_.Refresh -> loadAll()
            is GenEvent_2159_.Search -> search(event.query)
            is GenEvent_2159_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2159_.Loading; _state.value = GenState_2159_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2159_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2159_.Success(searchUseCase(query)) } }
}
