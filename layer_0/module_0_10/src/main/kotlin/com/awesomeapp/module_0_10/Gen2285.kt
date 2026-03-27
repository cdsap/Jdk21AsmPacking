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

data class GenModel_2285_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2285_ {
    data class Load(val id: Long) : GenEvent_2285_()
    data class Update(val model: GenModel_2285_) : GenEvent_2285_()
    data class Delete(val id: Long) : GenEvent_2285_()
    data object Refresh : GenEvent_2285_()
    data class Search(val query: String) : GenEvent_2285_()
    data class Filter(val predicate: String) : GenEvent_2285_()
}

sealed class GenState_2285_ {
    data object Idle : GenState_2285_()
    data object Loading : GenState_2285_()
    data class Success(val items: List<GenModel_2285_>) : GenState_2285_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2285_()
    data class Partial(val items: List<GenModel_2285_>, val hasMore: Boolean) : GenState_2285_()
}

interface GenRepository_2285_ {
    suspend fun getAll(): List<GenModel_2285_>
    suspend fun getById(id: Long): GenModel_2285_?
    suspend fun save(model: GenModel_2285_): GenModel_2285_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2285_>
}

@Singleton
class GenRepositoryImpl_2285_ @Inject constructor() : GenRepository_2285_ {
    private val store = mutableMapOf<Long, GenModel_2285_>()
    override suspend fun getAll(): List<GenModel_2285_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2285_? = store[id]
    override suspend fun save(model: GenModel_2285_): GenModel_2285_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2285_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2285_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2285_ @Inject constructor(
    private val repository: GenRepositoryImpl_2285_
) : GenUseCase_2285_<Unit, List<GenModel_2285_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2285_> = repository.getAll()
}

class GenSaveUseCase_2285_ @Inject constructor(
    private val repository: GenRepositoryImpl_2285_
) : GenUseCase_2285_<GenModel_2285_, GenModel_2285_> {
    override suspend fun invoke(params: GenModel_2285_): GenModel_2285_ = repository.save(params)
}

class GenDeleteUseCase_2285_ @Inject constructor(
    private val repository: GenRepositoryImpl_2285_
) : GenUseCase_2285_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2285_ @Inject constructor(
    private val repository: GenRepositoryImpl_2285_
) : GenUseCase_2285_<String, List<GenModel_2285_>> {
    override suspend fun invoke(params: String): List<GenModel_2285_> = repository.search(params)
}

abstract class GenMapper_2285_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2285_ : GenMapper_2285_<GenModel_2285_, String>() {
    override fun map(input: GenModel_2285_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2285_ : GenMapper_2285_<String, GenModel_2285_>() {
    override fun map(input: String): GenModel_2285_ {
        val parts = input.split(":")
        return GenModel_2285_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2285_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2285_,
    private val saveUseCase: GenSaveUseCase_2285_,
    private val deleteUseCase: GenDeleteUseCase_2285_,
    private val searchUseCase: GenSearchUseCase_2285_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2285_>(GenState_2285_.Idle)
    val state: StateFlow<GenState_2285_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2285_) {
        when (event) {
            is GenEvent_2285_.Load -> loadAll()
            is GenEvent_2285_.Update -> save(event.model)
            is GenEvent_2285_.Delete -> delete(event.id)
            is GenEvent_2285_.Refresh -> loadAll()
            is GenEvent_2285_.Search -> search(event.query)
            is GenEvent_2285_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2285_.Loading; _state.value = GenState_2285_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2285_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2285_.Success(searchUseCase(query)) } }
}
