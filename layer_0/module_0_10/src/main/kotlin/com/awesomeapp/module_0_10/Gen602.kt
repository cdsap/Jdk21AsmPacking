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

data class GenModel_602_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_602_ {
    data class Load(val id: Long) : GenEvent_602_()
    data class Update(val model: GenModel_602_) : GenEvent_602_()
    data class Delete(val id: Long) : GenEvent_602_()
    data object Refresh : GenEvent_602_()
    data class Search(val query: String) : GenEvent_602_()
    data class Filter(val predicate: String) : GenEvent_602_()
}

sealed class GenState_602_ {
    data object Idle : GenState_602_()
    data object Loading : GenState_602_()
    data class Success(val items: List<GenModel_602_>) : GenState_602_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_602_()
    data class Partial(val items: List<GenModel_602_>, val hasMore: Boolean) : GenState_602_()
}

interface GenRepository_602_ {
    suspend fun getAll(): List<GenModel_602_>
    suspend fun getById(id: Long): GenModel_602_?
    suspend fun save(model: GenModel_602_): GenModel_602_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_602_>
}

@Singleton
class GenRepositoryImpl_602_ @Inject constructor() : GenRepository_602_ {
    private val store = mutableMapOf<Long, GenModel_602_>()
    override suspend fun getAll(): List<GenModel_602_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_602_? = store[id]
    override suspend fun save(model: GenModel_602_): GenModel_602_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_602_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_602_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_602_ @Inject constructor(
    private val repository: GenRepositoryImpl_602_
) : GenUseCase_602_<Unit, List<GenModel_602_>> {
    override suspend fun invoke(params: Unit): List<GenModel_602_> = repository.getAll()
}

class GenSaveUseCase_602_ @Inject constructor(
    private val repository: GenRepositoryImpl_602_
) : GenUseCase_602_<GenModel_602_, GenModel_602_> {
    override suspend fun invoke(params: GenModel_602_): GenModel_602_ = repository.save(params)
}

class GenDeleteUseCase_602_ @Inject constructor(
    private val repository: GenRepositoryImpl_602_
) : GenUseCase_602_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_602_ @Inject constructor(
    private val repository: GenRepositoryImpl_602_
) : GenUseCase_602_<String, List<GenModel_602_>> {
    override suspend fun invoke(params: String): List<GenModel_602_> = repository.search(params)
}

abstract class GenMapper_602_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_602_ : GenMapper_602_<GenModel_602_, String>() {
    override fun map(input: GenModel_602_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_602_ : GenMapper_602_<String, GenModel_602_>() {
    override fun map(input: String): GenModel_602_ {
        val parts = input.split(":")
        return GenModel_602_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_602_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_602_,
    private val saveUseCase: GenSaveUseCase_602_,
    private val deleteUseCase: GenDeleteUseCase_602_,
    private val searchUseCase: GenSearchUseCase_602_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_602_>(GenState_602_.Idle)
    val state: StateFlow<GenState_602_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_602_) {
        when (event) {
            is GenEvent_602_.Load -> loadAll()
            is GenEvent_602_.Update -> save(event.model)
            is GenEvent_602_.Delete -> delete(event.id)
            is GenEvent_602_.Refresh -> loadAll()
            is GenEvent_602_.Search -> search(event.query)
            is GenEvent_602_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_602_.Loading; _state.value = GenState_602_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_602_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_602_.Success(searchUseCase(query)) } }
}
