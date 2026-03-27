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

data class GenModel_153_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_153_ {
    data class Load(val id: Long) : GenEvent_153_()
    data class Update(val model: GenModel_153_) : GenEvent_153_()
    data class Delete(val id: Long) : GenEvent_153_()
    data object Refresh : GenEvent_153_()
    data class Search(val query: String) : GenEvent_153_()
    data class Filter(val predicate: String) : GenEvent_153_()
}

sealed class GenState_153_ {
    data object Idle : GenState_153_()
    data object Loading : GenState_153_()
    data class Success(val items: List<GenModel_153_>) : GenState_153_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_153_()
    data class Partial(val items: List<GenModel_153_>, val hasMore: Boolean) : GenState_153_()
}

interface GenRepository_153_ {
    suspend fun getAll(): List<GenModel_153_>
    suspend fun getById(id: Long): GenModel_153_?
    suspend fun save(model: GenModel_153_): GenModel_153_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_153_>
}

@Singleton
class GenRepositoryImpl_153_ @Inject constructor() : GenRepository_153_ {
    private val store = mutableMapOf<Long, GenModel_153_>()
    override suspend fun getAll(): List<GenModel_153_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_153_? = store[id]
    override suspend fun save(model: GenModel_153_): GenModel_153_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_153_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_153_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_153_ @Inject constructor(
    private val repository: GenRepositoryImpl_153_
) : GenUseCase_153_<Unit, List<GenModel_153_>> {
    override suspend fun invoke(params: Unit): List<GenModel_153_> = repository.getAll()
}

class GenSaveUseCase_153_ @Inject constructor(
    private val repository: GenRepositoryImpl_153_
) : GenUseCase_153_<GenModel_153_, GenModel_153_> {
    override suspend fun invoke(params: GenModel_153_): GenModel_153_ = repository.save(params)
}

class GenDeleteUseCase_153_ @Inject constructor(
    private val repository: GenRepositoryImpl_153_
) : GenUseCase_153_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_153_ @Inject constructor(
    private val repository: GenRepositoryImpl_153_
) : GenUseCase_153_<String, List<GenModel_153_>> {
    override suspend fun invoke(params: String): List<GenModel_153_> = repository.search(params)
}

abstract class GenMapper_153_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_153_ : GenMapper_153_<GenModel_153_, String>() {
    override fun map(input: GenModel_153_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_153_ : GenMapper_153_<String, GenModel_153_>() {
    override fun map(input: String): GenModel_153_ {
        val parts = input.split(":")
        return GenModel_153_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_153_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_153_,
    private val saveUseCase: GenSaveUseCase_153_,
    private val deleteUseCase: GenDeleteUseCase_153_,
    private val searchUseCase: GenSearchUseCase_153_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_153_>(GenState_153_.Idle)
    val state: StateFlow<GenState_153_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_153_) {
        when (event) {
            is GenEvent_153_.Load -> loadAll()
            is GenEvent_153_.Update -> save(event.model)
            is GenEvent_153_.Delete -> delete(event.id)
            is GenEvent_153_.Refresh -> loadAll()
            is GenEvent_153_.Search -> search(event.query)
            is GenEvent_153_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_153_.Loading; _state.value = GenState_153_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_153_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_153_.Success(searchUseCase(query)) } }
}
