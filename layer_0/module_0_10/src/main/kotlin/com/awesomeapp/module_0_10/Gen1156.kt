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

data class GenModel_1156_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1156_ {
    data class Load(val id: Long) : GenEvent_1156_()
    data class Update(val model: GenModel_1156_) : GenEvent_1156_()
    data class Delete(val id: Long) : GenEvent_1156_()
    data object Refresh : GenEvent_1156_()
    data class Search(val query: String) : GenEvent_1156_()
    data class Filter(val predicate: String) : GenEvent_1156_()
}

sealed class GenState_1156_ {
    data object Idle : GenState_1156_()
    data object Loading : GenState_1156_()
    data class Success(val items: List<GenModel_1156_>) : GenState_1156_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1156_()
    data class Partial(val items: List<GenModel_1156_>, val hasMore: Boolean) : GenState_1156_()
}

interface GenRepository_1156_ {
    suspend fun getAll(): List<GenModel_1156_>
    suspend fun getById(id: Long): GenModel_1156_?
    suspend fun save(model: GenModel_1156_): GenModel_1156_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1156_>
}

@Singleton
class GenRepositoryImpl_1156_ @Inject constructor() : GenRepository_1156_ {
    private val store = mutableMapOf<Long, GenModel_1156_>()
    override suspend fun getAll(): List<GenModel_1156_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1156_? = store[id]
    override suspend fun save(model: GenModel_1156_): GenModel_1156_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1156_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1156_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1156_ @Inject constructor(
    private val repository: GenRepositoryImpl_1156_
) : GenUseCase_1156_<Unit, List<GenModel_1156_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1156_> = repository.getAll()
}

class GenSaveUseCase_1156_ @Inject constructor(
    private val repository: GenRepositoryImpl_1156_
) : GenUseCase_1156_<GenModel_1156_, GenModel_1156_> {
    override suspend fun invoke(params: GenModel_1156_): GenModel_1156_ = repository.save(params)
}

class GenDeleteUseCase_1156_ @Inject constructor(
    private val repository: GenRepositoryImpl_1156_
) : GenUseCase_1156_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1156_ @Inject constructor(
    private val repository: GenRepositoryImpl_1156_
) : GenUseCase_1156_<String, List<GenModel_1156_>> {
    override suspend fun invoke(params: String): List<GenModel_1156_> = repository.search(params)
}

abstract class GenMapper_1156_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1156_ : GenMapper_1156_<GenModel_1156_, String>() {
    override fun map(input: GenModel_1156_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1156_ : GenMapper_1156_<String, GenModel_1156_>() {
    override fun map(input: String): GenModel_1156_ {
        val parts = input.split(":")
        return GenModel_1156_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1156_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1156_,
    private val saveUseCase: GenSaveUseCase_1156_,
    private val deleteUseCase: GenDeleteUseCase_1156_,
    private val searchUseCase: GenSearchUseCase_1156_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1156_>(GenState_1156_.Idle)
    val state: StateFlow<GenState_1156_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1156_) {
        when (event) {
            is GenEvent_1156_.Load -> loadAll()
            is GenEvent_1156_.Update -> save(event.model)
            is GenEvent_1156_.Delete -> delete(event.id)
            is GenEvent_1156_.Refresh -> loadAll()
            is GenEvent_1156_.Search -> search(event.query)
            is GenEvent_1156_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1156_.Loading; _state.value = GenState_1156_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1156_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1156_.Success(searchUseCase(query)) } }
}
