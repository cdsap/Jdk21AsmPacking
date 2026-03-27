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

data class GenModel_374_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_374_ {
    data class Load(val id: Long) : GenEvent_374_()
    data class Update(val model: GenModel_374_) : GenEvent_374_()
    data class Delete(val id: Long) : GenEvent_374_()
    data object Refresh : GenEvent_374_()
    data class Search(val query: String) : GenEvent_374_()
    data class Filter(val predicate: String) : GenEvent_374_()
}

sealed class GenState_374_ {
    data object Idle : GenState_374_()
    data object Loading : GenState_374_()
    data class Success(val items: List<GenModel_374_>) : GenState_374_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_374_()
    data class Partial(val items: List<GenModel_374_>, val hasMore: Boolean) : GenState_374_()
}

interface GenRepository_374_ {
    suspend fun getAll(): List<GenModel_374_>
    suspend fun getById(id: Long): GenModel_374_?
    suspend fun save(model: GenModel_374_): GenModel_374_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_374_>
}

@Singleton
class GenRepositoryImpl_374_ @Inject constructor() : GenRepository_374_ {
    private val store = mutableMapOf<Long, GenModel_374_>()
    override suspend fun getAll(): List<GenModel_374_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_374_? = store[id]
    override suspend fun save(model: GenModel_374_): GenModel_374_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_374_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_374_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_374_ @Inject constructor(
    private val repository: GenRepositoryImpl_374_
) : GenUseCase_374_<Unit, List<GenModel_374_>> {
    override suspend fun invoke(params: Unit): List<GenModel_374_> = repository.getAll()
}

class GenSaveUseCase_374_ @Inject constructor(
    private val repository: GenRepositoryImpl_374_
) : GenUseCase_374_<GenModel_374_, GenModel_374_> {
    override suspend fun invoke(params: GenModel_374_): GenModel_374_ = repository.save(params)
}

class GenDeleteUseCase_374_ @Inject constructor(
    private val repository: GenRepositoryImpl_374_
) : GenUseCase_374_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_374_ @Inject constructor(
    private val repository: GenRepositoryImpl_374_
) : GenUseCase_374_<String, List<GenModel_374_>> {
    override suspend fun invoke(params: String): List<GenModel_374_> = repository.search(params)
}

abstract class GenMapper_374_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_374_ : GenMapper_374_<GenModel_374_, String>() {
    override fun map(input: GenModel_374_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_374_ : GenMapper_374_<String, GenModel_374_>() {
    override fun map(input: String): GenModel_374_ {
        val parts = input.split(":")
        return GenModel_374_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_374_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_374_,
    private val saveUseCase: GenSaveUseCase_374_,
    private val deleteUseCase: GenDeleteUseCase_374_,
    private val searchUseCase: GenSearchUseCase_374_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_374_>(GenState_374_.Idle)
    val state: StateFlow<GenState_374_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_374_) {
        when (event) {
            is GenEvent_374_.Load -> loadAll()
            is GenEvent_374_.Update -> save(event.model)
            is GenEvent_374_.Delete -> delete(event.id)
            is GenEvent_374_.Refresh -> loadAll()
            is GenEvent_374_.Search -> search(event.query)
            is GenEvent_374_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_374_.Loading; _state.value = GenState_374_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_374_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_374_.Success(searchUseCase(query)) } }
}
