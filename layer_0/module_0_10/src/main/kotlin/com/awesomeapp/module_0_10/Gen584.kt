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

data class GenModel_584_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_584_ {
    data class Load(val id: Long) : GenEvent_584_()
    data class Update(val model: GenModel_584_) : GenEvent_584_()
    data class Delete(val id: Long) : GenEvent_584_()
    data object Refresh : GenEvent_584_()
    data class Search(val query: String) : GenEvent_584_()
    data class Filter(val predicate: String) : GenEvent_584_()
}

sealed class GenState_584_ {
    data object Idle : GenState_584_()
    data object Loading : GenState_584_()
    data class Success(val items: List<GenModel_584_>) : GenState_584_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_584_()
    data class Partial(val items: List<GenModel_584_>, val hasMore: Boolean) : GenState_584_()
}

interface GenRepository_584_ {
    suspend fun getAll(): List<GenModel_584_>
    suspend fun getById(id: Long): GenModel_584_?
    suspend fun save(model: GenModel_584_): GenModel_584_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_584_>
}

@Singleton
class GenRepositoryImpl_584_ @Inject constructor() : GenRepository_584_ {
    private val store = mutableMapOf<Long, GenModel_584_>()
    override suspend fun getAll(): List<GenModel_584_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_584_? = store[id]
    override suspend fun save(model: GenModel_584_): GenModel_584_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_584_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_584_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_584_ @Inject constructor(
    private val repository: GenRepositoryImpl_584_
) : GenUseCase_584_<Unit, List<GenModel_584_>> {
    override suspend fun invoke(params: Unit): List<GenModel_584_> = repository.getAll()
}

class GenSaveUseCase_584_ @Inject constructor(
    private val repository: GenRepositoryImpl_584_
) : GenUseCase_584_<GenModel_584_, GenModel_584_> {
    override suspend fun invoke(params: GenModel_584_): GenModel_584_ = repository.save(params)
}

class GenDeleteUseCase_584_ @Inject constructor(
    private val repository: GenRepositoryImpl_584_
) : GenUseCase_584_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_584_ @Inject constructor(
    private val repository: GenRepositoryImpl_584_
) : GenUseCase_584_<String, List<GenModel_584_>> {
    override suspend fun invoke(params: String): List<GenModel_584_> = repository.search(params)
}

abstract class GenMapper_584_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_584_ : GenMapper_584_<GenModel_584_, String>() {
    override fun map(input: GenModel_584_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_584_ : GenMapper_584_<String, GenModel_584_>() {
    override fun map(input: String): GenModel_584_ {
        val parts = input.split(":")
        return GenModel_584_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_584_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_584_,
    private val saveUseCase: GenSaveUseCase_584_,
    private val deleteUseCase: GenDeleteUseCase_584_,
    private val searchUseCase: GenSearchUseCase_584_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_584_>(GenState_584_.Idle)
    val state: StateFlow<GenState_584_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_584_) {
        when (event) {
            is GenEvent_584_.Load -> loadAll()
            is GenEvent_584_.Update -> save(event.model)
            is GenEvent_584_.Delete -> delete(event.id)
            is GenEvent_584_.Refresh -> loadAll()
            is GenEvent_584_.Search -> search(event.query)
            is GenEvent_584_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_584_.Loading; _state.value = GenState_584_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_584_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_584_.Success(searchUseCase(query)) } }
}
