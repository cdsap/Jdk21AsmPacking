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

data class GenModel_532_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_532_ {
    data class Load(val id: Long) : GenEvent_532_()
    data class Update(val model: GenModel_532_) : GenEvent_532_()
    data class Delete(val id: Long) : GenEvent_532_()
    data object Refresh : GenEvent_532_()
    data class Search(val query: String) : GenEvent_532_()
    data class Filter(val predicate: String) : GenEvent_532_()
}

sealed class GenState_532_ {
    data object Idle : GenState_532_()
    data object Loading : GenState_532_()
    data class Success(val items: List<GenModel_532_>) : GenState_532_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_532_()
    data class Partial(val items: List<GenModel_532_>, val hasMore: Boolean) : GenState_532_()
}

interface GenRepository_532_ {
    suspend fun getAll(): List<GenModel_532_>
    suspend fun getById(id: Long): GenModel_532_?
    suspend fun save(model: GenModel_532_): GenModel_532_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_532_>
}

@Singleton
class GenRepositoryImpl_532_ @Inject constructor() : GenRepository_532_ {
    private val store = mutableMapOf<Long, GenModel_532_>()
    override suspend fun getAll(): List<GenModel_532_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_532_? = store[id]
    override suspend fun save(model: GenModel_532_): GenModel_532_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_532_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_532_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_532_ @Inject constructor(
    private val repository: GenRepositoryImpl_532_
) : GenUseCase_532_<Unit, List<GenModel_532_>> {
    override suspend fun invoke(params: Unit): List<GenModel_532_> = repository.getAll()
}

class GenSaveUseCase_532_ @Inject constructor(
    private val repository: GenRepositoryImpl_532_
) : GenUseCase_532_<GenModel_532_, GenModel_532_> {
    override suspend fun invoke(params: GenModel_532_): GenModel_532_ = repository.save(params)
}

class GenDeleteUseCase_532_ @Inject constructor(
    private val repository: GenRepositoryImpl_532_
) : GenUseCase_532_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_532_ @Inject constructor(
    private val repository: GenRepositoryImpl_532_
) : GenUseCase_532_<String, List<GenModel_532_>> {
    override suspend fun invoke(params: String): List<GenModel_532_> = repository.search(params)
}

abstract class GenMapper_532_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_532_ : GenMapper_532_<GenModel_532_, String>() {
    override fun map(input: GenModel_532_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_532_ : GenMapper_532_<String, GenModel_532_>() {
    override fun map(input: String): GenModel_532_ {
        val parts = input.split(":")
        return GenModel_532_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_532_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_532_,
    private val saveUseCase: GenSaveUseCase_532_,
    private val deleteUseCase: GenDeleteUseCase_532_,
    private val searchUseCase: GenSearchUseCase_532_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_532_>(GenState_532_.Idle)
    val state: StateFlow<GenState_532_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_532_) {
        when (event) {
            is GenEvent_532_.Load -> loadAll()
            is GenEvent_532_.Update -> save(event.model)
            is GenEvent_532_.Delete -> delete(event.id)
            is GenEvent_532_.Refresh -> loadAll()
            is GenEvent_532_.Search -> search(event.query)
            is GenEvent_532_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_532_.Loading; _state.value = GenState_532_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_532_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_532_.Success(searchUseCase(query)) } }
}
