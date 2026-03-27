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

data class GenModel_729_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_729_ {
    data class Load(val id: Long) : GenEvent_729_()
    data class Update(val model: GenModel_729_) : GenEvent_729_()
    data class Delete(val id: Long) : GenEvent_729_()
    data object Refresh : GenEvent_729_()
    data class Search(val query: String) : GenEvent_729_()
    data class Filter(val predicate: String) : GenEvent_729_()
}

sealed class GenState_729_ {
    data object Idle : GenState_729_()
    data object Loading : GenState_729_()
    data class Success(val items: List<GenModel_729_>) : GenState_729_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_729_()
    data class Partial(val items: List<GenModel_729_>, val hasMore: Boolean) : GenState_729_()
}

interface GenRepository_729_ {
    suspend fun getAll(): List<GenModel_729_>
    suspend fun getById(id: Long): GenModel_729_?
    suspend fun save(model: GenModel_729_): GenModel_729_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_729_>
}

@Singleton
class GenRepositoryImpl_729_ @Inject constructor() : GenRepository_729_ {
    private val store = mutableMapOf<Long, GenModel_729_>()
    override suspend fun getAll(): List<GenModel_729_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_729_? = store[id]
    override suspend fun save(model: GenModel_729_): GenModel_729_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_729_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_729_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_729_ @Inject constructor(
    private val repository: GenRepositoryImpl_729_
) : GenUseCase_729_<Unit, List<GenModel_729_>> {
    override suspend fun invoke(params: Unit): List<GenModel_729_> = repository.getAll()
}

class GenSaveUseCase_729_ @Inject constructor(
    private val repository: GenRepositoryImpl_729_
) : GenUseCase_729_<GenModel_729_, GenModel_729_> {
    override suspend fun invoke(params: GenModel_729_): GenModel_729_ = repository.save(params)
}

class GenDeleteUseCase_729_ @Inject constructor(
    private val repository: GenRepositoryImpl_729_
) : GenUseCase_729_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_729_ @Inject constructor(
    private val repository: GenRepositoryImpl_729_
) : GenUseCase_729_<String, List<GenModel_729_>> {
    override suspend fun invoke(params: String): List<GenModel_729_> = repository.search(params)
}

abstract class GenMapper_729_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_729_ : GenMapper_729_<GenModel_729_, String>() {
    override fun map(input: GenModel_729_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_729_ : GenMapper_729_<String, GenModel_729_>() {
    override fun map(input: String): GenModel_729_ {
        val parts = input.split(":")
        return GenModel_729_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_729_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_729_,
    private val saveUseCase: GenSaveUseCase_729_,
    private val deleteUseCase: GenDeleteUseCase_729_,
    private val searchUseCase: GenSearchUseCase_729_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_729_>(GenState_729_.Idle)
    val state: StateFlow<GenState_729_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_729_) {
        when (event) {
            is GenEvent_729_.Load -> loadAll()
            is GenEvent_729_.Update -> save(event.model)
            is GenEvent_729_.Delete -> delete(event.id)
            is GenEvent_729_.Refresh -> loadAll()
            is GenEvent_729_.Search -> search(event.query)
            is GenEvent_729_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_729_.Loading; _state.value = GenState_729_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_729_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_729_.Success(searchUseCase(query)) } }
}
