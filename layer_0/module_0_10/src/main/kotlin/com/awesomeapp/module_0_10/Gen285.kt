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

data class GenModel_285_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_285_ {
    data class Load(val id: Long) : GenEvent_285_()
    data class Update(val model: GenModel_285_) : GenEvent_285_()
    data class Delete(val id: Long) : GenEvent_285_()
    data object Refresh : GenEvent_285_()
    data class Search(val query: String) : GenEvent_285_()
    data class Filter(val predicate: String) : GenEvent_285_()
}

sealed class GenState_285_ {
    data object Idle : GenState_285_()
    data object Loading : GenState_285_()
    data class Success(val items: List<GenModel_285_>) : GenState_285_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_285_()
    data class Partial(val items: List<GenModel_285_>, val hasMore: Boolean) : GenState_285_()
}

interface GenRepository_285_ {
    suspend fun getAll(): List<GenModel_285_>
    suspend fun getById(id: Long): GenModel_285_?
    suspend fun save(model: GenModel_285_): GenModel_285_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_285_>
}

@Singleton
class GenRepositoryImpl_285_ @Inject constructor() : GenRepository_285_ {
    private val store = mutableMapOf<Long, GenModel_285_>()
    override suspend fun getAll(): List<GenModel_285_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_285_? = store[id]
    override suspend fun save(model: GenModel_285_): GenModel_285_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_285_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_285_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_285_ @Inject constructor(
    private val repository: GenRepositoryImpl_285_
) : GenUseCase_285_<Unit, List<GenModel_285_>> {
    override suspend fun invoke(params: Unit): List<GenModel_285_> = repository.getAll()
}

class GenSaveUseCase_285_ @Inject constructor(
    private val repository: GenRepositoryImpl_285_
) : GenUseCase_285_<GenModel_285_, GenModel_285_> {
    override suspend fun invoke(params: GenModel_285_): GenModel_285_ = repository.save(params)
}

class GenDeleteUseCase_285_ @Inject constructor(
    private val repository: GenRepositoryImpl_285_
) : GenUseCase_285_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_285_ @Inject constructor(
    private val repository: GenRepositoryImpl_285_
) : GenUseCase_285_<String, List<GenModel_285_>> {
    override suspend fun invoke(params: String): List<GenModel_285_> = repository.search(params)
}

abstract class GenMapper_285_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_285_ : GenMapper_285_<GenModel_285_, String>() {
    override fun map(input: GenModel_285_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_285_ : GenMapper_285_<String, GenModel_285_>() {
    override fun map(input: String): GenModel_285_ {
        val parts = input.split(":")
        return GenModel_285_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_285_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_285_,
    private val saveUseCase: GenSaveUseCase_285_,
    private val deleteUseCase: GenDeleteUseCase_285_,
    private val searchUseCase: GenSearchUseCase_285_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_285_>(GenState_285_.Idle)
    val state: StateFlow<GenState_285_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_285_) {
        when (event) {
            is GenEvent_285_.Load -> loadAll()
            is GenEvent_285_.Update -> save(event.model)
            is GenEvent_285_.Delete -> delete(event.id)
            is GenEvent_285_.Refresh -> loadAll()
            is GenEvent_285_.Search -> search(event.query)
            is GenEvent_285_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_285_.Loading; _state.value = GenState_285_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_285_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_285_.Success(searchUseCase(query)) } }
}
