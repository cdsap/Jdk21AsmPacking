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

data class GenModel_975_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_975_ {
    data class Load(val id: Long) : GenEvent_975_()
    data class Update(val model: GenModel_975_) : GenEvent_975_()
    data class Delete(val id: Long) : GenEvent_975_()
    data object Refresh : GenEvent_975_()
    data class Search(val query: String) : GenEvent_975_()
    data class Filter(val predicate: String) : GenEvent_975_()
}

sealed class GenState_975_ {
    data object Idle : GenState_975_()
    data object Loading : GenState_975_()
    data class Success(val items: List<GenModel_975_>) : GenState_975_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_975_()
    data class Partial(val items: List<GenModel_975_>, val hasMore: Boolean) : GenState_975_()
}

interface GenRepository_975_ {
    suspend fun getAll(): List<GenModel_975_>
    suspend fun getById(id: Long): GenModel_975_?
    suspend fun save(model: GenModel_975_): GenModel_975_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_975_>
}

@Singleton
class GenRepositoryImpl_975_ @Inject constructor() : GenRepository_975_ {
    private val store = mutableMapOf<Long, GenModel_975_>()
    override suspend fun getAll(): List<GenModel_975_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_975_? = store[id]
    override suspend fun save(model: GenModel_975_): GenModel_975_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_975_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_975_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_975_ @Inject constructor(
    private val repository: GenRepositoryImpl_975_
) : GenUseCase_975_<Unit, List<GenModel_975_>> {
    override suspend fun invoke(params: Unit): List<GenModel_975_> = repository.getAll()
}

class GenSaveUseCase_975_ @Inject constructor(
    private val repository: GenRepositoryImpl_975_
) : GenUseCase_975_<GenModel_975_, GenModel_975_> {
    override suspend fun invoke(params: GenModel_975_): GenModel_975_ = repository.save(params)
}

class GenDeleteUseCase_975_ @Inject constructor(
    private val repository: GenRepositoryImpl_975_
) : GenUseCase_975_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_975_ @Inject constructor(
    private val repository: GenRepositoryImpl_975_
) : GenUseCase_975_<String, List<GenModel_975_>> {
    override suspend fun invoke(params: String): List<GenModel_975_> = repository.search(params)
}

abstract class GenMapper_975_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_975_ : GenMapper_975_<GenModel_975_, String>() {
    override fun map(input: GenModel_975_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_975_ : GenMapper_975_<String, GenModel_975_>() {
    override fun map(input: String): GenModel_975_ {
        val parts = input.split(":")
        return GenModel_975_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_975_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_975_,
    private val saveUseCase: GenSaveUseCase_975_,
    private val deleteUseCase: GenDeleteUseCase_975_,
    private val searchUseCase: GenSearchUseCase_975_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_975_>(GenState_975_.Idle)
    val state: StateFlow<GenState_975_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_975_) {
        when (event) {
            is GenEvent_975_.Load -> loadAll()
            is GenEvent_975_.Update -> save(event.model)
            is GenEvent_975_.Delete -> delete(event.id)
            is GenEvent_975_.Refresh -> loadAll()
            is GenEvent_975_.Search -> search(event.query)
            is GenEvent_975_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_975_.Loading; _state.value = GenState_975_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_975_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_975_.Success(searchUseCase(query)) } }
}
