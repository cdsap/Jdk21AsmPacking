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

data class GenModel_706_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_706_ {
    data class Load(val id: Long) : GenEvent_706_()
    data class Update(val model: GenModel_706_) : GenEvent_706_()
    data class Delete(val id: Long) : GenEvent_706_()
    data object Refresh : GenEvent_706_()
    data class Search(val query: String) : GenEvent_706_()
    data class Filter(val predicate: String) : GenEvent_706_()
}

sealed class GenState_706_ {
    data object Idle : GenState_706_()
    data object Loading : GenState_706_()
    data class Success(val items: List<GenModel_706_>) : GenState_706_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_706_()
    data class Partial(val items: List<GenModel_706_>, val hasMore: Boolean) : GenState_706_()
}

interface GenRepository_706_ {
    suspend fun getAll(): List<GenModel_706_>
    suspend fun getById(id: Long): GenModel_706_?
    suspend fun save(model: GenModel_706_): GenModel_706_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_706_>
}

@Singleton
class GenRepositoryImpl_706_ @Inject constructor() : GenRepository_706_ {
    private val store = mutableMapOf<Long, GenModel_706_>()
    override suspend fun getAll(): List<GenModel_706_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_706_? = store[id]
    override suspend fun save(model: GenModel_706_): GenModel_706_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_706_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_706_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_706_ @Inject constructor(
    private val repository: GenRepositoryImpl_706_
) : GenUseCase_706_<Unit, List<GenModel_706_>> {
    override suspend fun invoke(params: Unit): List<GenModel_706_> = repository.getAll()
}

class GenSaveUseCase_706_ @Inject constructor(
    private val repository: GenRepositoryImpl_706_
) : GenUseCase_706_<GenModel_706_, GenModel_706_> {
    override suspend fun invoke(params: GenModel_706_): GenModel_706_ = repository.save(params)
}

class GenDeleteUseCase_706_ @Inject constructor(
    private val repository: GenRepositoryImpl_706_
) : GenUseCase_706_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_706_ @Inject constructor(
    private val repository: GenRepositoryImpl_706_
) : GenUseCase_706_<String, List<GenModel_706_>> {
    override suspend fun invoke(params: String): List<GenModel_706_> = repository.search(params)
}

abstract class GenMapper_706_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_706_ : GenMapper_706_<GenModel_706_, String>() {
    override fun map(input: GenModel_706_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_706_ : GenMapper_706_<String, GenModel_706_>() {
    override fun map(input: String): GenModel_706_ {
        val parts = input.split(":")
        return GenModel_706_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_706_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_706_,
    private val saveUseCase: GenSaveUseCase_706_,
    private val deleteUseCase: GenDeleteUseCase_706_,
    private val searchUseCase: GenSearchUseCase_706_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_706_>(GenState_706_.Idle)
    val state: StateFlow<GenState_706_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_706_) {
        when (event) {
            is GenEvent_706_.Load -> loadAll()
            is GenEvent_706_.Update -> save(event.model)
            is GenEvent_706_.Delete -> delete(event.id)
            is GenEvent_706_.Refresh -> loadAll()
            is GenEvent_706_.Search -> search(event.query)
            is GenEvent_706_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_706_.Loading; _state.value = GenState_706_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_706_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_706_.Success(searchUseCase(query)) } }
}
