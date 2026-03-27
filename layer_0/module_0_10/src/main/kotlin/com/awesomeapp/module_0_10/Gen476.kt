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

data class GenModel_476_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_476_ {
    data class Load(val id: Long) : GenEvent_476_()
    data class Update(val model: GenModel_476_) : GenEvent_476_()
    data class Delete(val id: Long) : GenEvent_476_()
    data object Refresh : GenEvent_476_()
    data class Search(val query: String) : GenEvent_476_()
    data class Filter(val predicate: String) : GenEvent_476_()
}

sealed class GenState_476_ {
    data object Idle : GenState_476_()
    data object Loading : GenState_476_()
    data class Success(val items: List<GenModel_476_>) : GenState_476_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_476_()
    data class Partial(val items: List<GenModel_476_>, val hasMore: Boolean) : GenState_476_()
}

interface GenRepository_476_ {
    suspend fun getAll(): List<GenModel_476_>
    suspend fun getById(id: Long): GenModel_476_?
    suspend fun save(model: GenModel_476_): GenModel_476_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_476_>
}

@Singleton
class GenRepositoryImpl_476_ @Inject constructor() : GenRepository_476_ {
    private val store = mutableMapOf<Long, GenModel_476_>()
    override suspend fun getAll(): List<GenModel_476_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_476_? = store[id]
    override suspend fun save(model: GenModel_476_): GenModel_476_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_476_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_476_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_476_ @Inject constructor(
    private val repository: GenRepositoryImpl_476_
) : GenUseCase_476_<Unit, List<GenModel_476_>> {
    override suspend fun invoke(params: Unit): List<GenModel_476_> = repository.getAll()
}

class GenSaveUseCase_476_ @Inject constructor(
    private val repository: GenRepositoryImpl_476_
) : GenUseCase_476_<GenModel_476_, GenModel_476_> {
    override suspend fun invoke(params: GenModel_476_): GenModel_476_ = repository.save(params)
}

class GenDeleteUseCase_476_ @Inject constructor(
    private val repository: GenRepositoryImpl_476_
) : GenUseCase_476_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_476_ @Inject constructor(
    private val repository: GenRepositoryImpl_476_
) : GenUseCase_476_<String, List<GenModel_476_>> {
    override suspend fun invoke(params: String): List<GenModel_476_> = repository.search(params)
}

abstract class GenMapper_476_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_476_ : GenMapper_476_<GenModel_476_, String>() {
    override fun map(input: GenModel_476_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_476_ : GenMapper_476_<String, GenModel_476_>() {
    override fun map(input: String): GenModel_476_ {
        val parts = input.split(":")
        return GenModel_476_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_476_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_476_,
    private val saveUseCase: GenSaveUseCase_476_,
    private val deleteUseCase: GenDeleteUseCase_476_,
    private val searchUseCase: GenSearchUseCase_476_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_476_>(GenState_476_.Idle)
    val state: StateFlow<GenState_476_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_476_) {
        when (event) {
            is GenEvent_476_.Load -> loadAll()
            is GenEvent_476_.Update -> save(event.model)
            is GenEvent_476_.Delete -> delete(event.id)
            is GenEvent_476_.Refresh -> loadAll()
            is GenEvent_476_.Search -> search(event.query)
            is GenEvent_476_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_476_.Loading; _state.value = GenState_476_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_476_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_476_.Success(searchUseCase(query)) } }
}
