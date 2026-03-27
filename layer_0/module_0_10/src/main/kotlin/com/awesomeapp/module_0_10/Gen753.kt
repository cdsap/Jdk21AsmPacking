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

data class GenModel_753_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_753_ {
    data class Load(val id: Long) : GenEvent_753_()
    data class Update(val model: GenModel_753_) : GenEvent_753_()
    data class Delete(val id: Long) : GenEvent_753_()
    data object Refresh : GenEvent_753_()
    data class Search(val query: String) : GenEvent_753_()
    data class Filter(val predicate: String) : GenEvent_753_()
}

sealed class GenState_753_ {
    data object Idle : GenState_753_()
    data object Loading : GenState_753_()
    data class Success(val items: List<GenModel_753_>) : GenState_753_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_753_()
    data class Partial(val items: List<GenModel_753_>, val hasMore: Boolean) : GenState_753_()
}

interface GenRepository_753_ {
    suspend fun getAll(): List<GenModel_753_>
    suspend fun getById(id: Long): GenModel_753_?
    suspend fun save(model: GenModel_753_): GenModel_753_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_753_>
}

@Singleton
class GenRepositoryImpl_753_ @Inject constructor() : GenRepository_753_ {
    private val store = mutableMapOf<Long, GenModel_753_>()
    override suspend fun getAll(): List<GenModel_753_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_753_? = store[id]
    override suspend fun save(model: GenModel_753_): GenModel_753_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_753_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_753_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_753_ @Inject constructor(
    private val repository: GenRepositoryImpl_753_
) : GenUseCase_753_<Unit, List<GenModel_753_>> {
    override suspend fun invoke(params: Unit): List<GenModel_753_> = repository.getAll()
}

class GenSaveUseCase_753_ @Inject constructor(
    private val repository: GenRepositoryImpl_753_
) : GenUseCase_753_<GenModel_753_, GenModel_753_> {
    override suspend fun invoke(params: GenModel_753_): GenModel_753_ = repository.save(params)
}

class GenDeleteUseCase_753_ @Inject constructor(
    private val repository: GenRepositoryImpl_753_
) : GenUseCase_753_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_753_ @Inject constructor(
    private val repository: GenRepositoryImpl_753_
) : GenUseCase_753_<String, List<GenModel_753_>> {
    override suspend fun invoke(params: String): List<GenModel_753_> = repository.search(params)
}

abstract class GenMapper_753_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_753_ : GenMapper_753_<GenModel_753_, String>() {
    override fun map(input: GenModel_753_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_753_ : GenMapper_753_<String, GenModel_753_>() {
    override fun map(input: String): GenModel_753_ {
        val parts = input.split(":")
        return GenModel_753_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_753_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_753_,
    private val saveUseCase: GenSaveUseCase_753_,
    private val deleteUseCase: GenDeleteUseCase_753_,
    private val searchUseCase: GenSearchUseCase_753_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_753_>(GenState_753_.Idle)
    val state: StateFlow<GenState_753_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_753_) {
        when (event) {
            is GenEvent_753_.Load -> loadAll()
            is GenEvent_753_.Update -> save(event.model)
            is GenEvent_753_.Delete -> delete(event.id)
            is GenEvent_753_.Refresh -> loadAll()
            is GenEvent_753_.Search -> search(event.query)
            is GenEvent_753_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_753_.Loading; _state.value = GenState_753_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_753_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_753_.Success(searchUseCase(query)) } }
}
