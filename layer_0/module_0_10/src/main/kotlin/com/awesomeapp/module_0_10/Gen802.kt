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

data class GenModel_802_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_802_ {
    data class Load(val id: Long) : GenEvent_802_()
    data class Update(val model: GenModel_802_) : GenEvent_802_()
    data class Delete(val id: Long) : GenEvent_802_()
    data object Refresh : GenEvent_802_()
    data class Search(val query: String) : GenEvent_802_()
    data class Filter(val predicate: String) : GenEvent_802_()
}

sealed class GenState_802_ {
    data object Idle : GenState_802_()
    data object Loading : GenState_802_()
    data class Success(val items: List<GenModel_802_>) : GenState_802_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_802_()
    data class Partial(val items: List<GenModel_802_>, val hasMore: Boolean) : GenState_802_()
}

interface GenRepository_802_ {
    suspend fun getAll(): List<GenModel_802_>
    suspend fun getById(id: Long): GenModel_802_?
    suspend fun save(model: GenModel_802_): GenModel_802_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_802_>
}

@Singleton
class GenRepositoryImpl_802_ @Inject constructor() : GenRepository_802_ {
    private val store = mutableMapOf<Long, GenModel_802_>()
    override suspend fun getAll(): List<GenModel_802_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_802_? = store[id]
    override suspend fun save(model: GenModel_802_): GenModel_802_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_802_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_802_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_802_ @Inject constructor(
    private val repository: GenRepositoryImpl_802_
) : GenUseCase_802_<Unit, List<GenModel_802_>> {
    override suspend fun invoke(params: Unit): List<GenModel_802_> = repository.getAll()
}

class GenSaveUseCase_802_ @Inject constructor(
    private val repository: GenRepositoryImpl_802_
) : GenUseCase_802_<GenModel_802_, GenModel_802_> {
    override suspend fun invoke(params: GenModel_802_): GenModel_802_ = repository.save(params)
}

class GenDeleteUseCase_802_ @Inject constructor(
    private val repository: GenRepositoryImpl_802_
) : GenUseCase_802_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_802_ @Inject constructor(
    private val repository: GenRepositoryImpl_802_
) : GenUseCase_802_<String, List<GenModel_802_>> {
    override suspend fun invoke(params: String): List<GenModel_802_> = repository.search(params)
}

abstract class GenMapper_802_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_802_ : GenMapper_802_<GenModel_802_, String>() {
    override fun map(input: GenModel_802_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_802_ : GenMapper_802_<String, GenModel_802_>() {
    override fun map(input: String): GenModel_802_ {
        val parts = input.split(":")
        return GenModel_802_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_802_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_802_,
    private val saveUseCase: GenSaveUseCase_802_,
    private val deleteUseCase: GenDeleteUseCase_802_,
    private val searchUseCase: GenSearchUseCase_802_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_802_>(GenState_802_.Idle)
    val state: StateFlow<GenState_802_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_802_) {
        when (event) {
            is GenEvent_802_.Load -> loadAll()
            is GenEvent_802_.Update -> save(event.model)
            is GenEvent_802_.Delete -> delete(event.id)
            is GenEvent_802_.Refresh -> loadAll()
            is GenEvent_802_.Search -> search(event.query)
            is GenEvent_802_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_802_.Loading; _state.value = GenState_802_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_802_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_802_.Success(searchUseCase(query)) } }
}
