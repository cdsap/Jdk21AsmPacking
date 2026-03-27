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

data class GenModel_52_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_52_ {
    data class Load(val id: Long) : GenEvent_52_()
    data class Update(val model: GenModel_52_) : GenEvent_52_()
    data class Delete(val id: Long) : GenEvent_52_()
    data object Refresh : GenEvent_52_()
    data class Search(val query: String) : GenEvent_52_()
    data class Filter(val predicate: String) : GenEvent_52_()
}

sealed class GenState_52_ {
    data object Idle : GenState_52_()
    data object Loading : GenState_52_()
    data class Success(val items: List<GenModel_52_>) : GenState_52_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_52_()
    data class Partial(val items: List<GenModel_52_>, val hasMore: Boolean) : GenState_52_()
}

interface GenRepository_52_ {
    suspend fun getAll(): List<GenModel_52_>
    suspend fun getById(id: Long): GenModel_52_?
    suspend fun save(model: GenModel_52_): GenModel_52_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_52_>
}

@Singleton
class GenRepositoryImpl_52_ @Inject constructor() : GenRepository_52_ {
    private val store = mutableMapOf<Long, GenModel_52_>()
    override suspend fun getAll(): List<GenModel_52_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_52_? = store[id]
    override suspend fun save(model: GenModel_52_): GenModel_52_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_52_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_52_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_52_ @Inject constructor(
    private val repository: GenRepositoryImpl_52_
) : GenUseCase_52_<Unit, List<GenModel_52_>> {
    override suspend fun invoke(params: Unit): List<GenModel_52_> = repository.getAll()
}

class GenSaveUseCase_52_ @Inject constructor(
    private val repository: GenRepositoryImpl_52_
) : GenUseCase_52_<GenModel_52_, GenModel_52_> {
    override suspend fun invoke(params: GenModel_52_): GenModel_52_ = repository.save(params)
}

class GenDeleteUseCase_52_ @Inject constructor(
    private val repository: GenRepositoryImpl_52_
) : GenUseCase_52_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_52_ @Inject constructor(
    private val repository: GenRepositoryImpl_52_
) : GenUseCase_52_<String, List<GenModel_52_>> {
    override suspend fun invoke(params: String): List<GenModel_52_> = repository.search(params)
}

abstract class GenMapper_52_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_52_ : GenMapper_52_<GenModel_52_, String>() {
    override fun map(input: GenModel_52_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_52_ : GenMapper_52_<String, GenModel_52_>() {
    override fun map(input: String): GenModel_52_ {
        val parts = input.split(":")
        return GenModel_52_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_52_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_52_,
    private val saveUseCase: GenSaveUseCase_52_,
    private val deleteUseCase: GenDeleteUseCase_52_,
    private val searchUseCase: GenSearchUseCase_52_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_52_>(GenState_52_.Idle)
    val state: StateFlow<GenState_52_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_52_) {
        when (event) {
            is GenEvent_52_.Load -> loadAll()
            is GenEvent_52_.Update -> save(event.model)
            is GenEvent_52_.Delete -> delete(event.id)
            is GenEvent_52_.Refresh -> loadAll()
            is GenEvent_52_.Search -> search(event.query)
            is GenEvent_52_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_52_.Loading; _state.value = GenState_52_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_52_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_52_.Success(searchUseCase(query)) } }
}
