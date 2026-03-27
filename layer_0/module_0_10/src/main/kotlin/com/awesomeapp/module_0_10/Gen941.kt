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

data class GenModel_941_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_941_ {
    data class Load(val id: Long) : GenEvent_941_()
    data class Update(val model: GenModel_941_) : GenEvent_941_()
    data class Delete(val id: Long) : GenEvent_941_()
    data object Refresh : GenEvent_941_()
    data class Search(val query: String) : GenEvent_941_()
    data class Filter(val predicate: String) : GenEvent_941_()
}

sealed class GenState_941_ {
    data object Idle : GenState_941_()
    data object Loading : GenState_941_()
    data class Success(val items: List<GenModel_941_>) : GenState_941_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_941_()
    data class Partial(val items: List<GenModel_941_>, val hasMore: Boolean) : GenState_941_()
}

interface GenRepository_941_ {
    suspend fun getAll(): List<GenModel_941_>
    suspend fun getById(id: Long): GenModel_941_?
    suspend fun save(model: GenModel_941_): GenModel_941_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_941_>
}

@Singleton
class GenRepositoryImpl_941_ @Inject constructor() : GenRepository_941_ {
    private val store = mutableMapOf<Long, GenModel_941_>()
    override suspend fun getAll(): List<GenModel_941_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_941_? = store[id]
    override suspend fun save(model: GenModel_941_): GenModel_941_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_941_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_941_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_941_ @Inject constructor(
    private val repository: GenRepositoryImpl_941_
) : GenUseCase_941_<Unit, List<GenModel_941_>> {
    override suspend fun invoke(params: Unit): List<GenModel_941_> = repository.getAll()
}

class GenSaveUseCase_941_ @Inject constructor(
    private val repository: GenRepositoryImpl_941_
) : GenUseCase_941_<GenModel_941_, GenModel_941_> {
    override suspend fun invoke(params: GenModel_941_): GenModel_941_ = repository.save(params)
}

class GenDeleteUseCase_941_ @Inject constructor(
    private val repository: GenRepositoryImpl_941_
) : GenUseCase_941_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_941_ @Inject constructor(
    private val repository: GenRepositoryImpl_941_
) : GenUseCase_941_<String, List<GenModel_941_>> {
    override suspend fun invoke(params: String): List<GenModel_941_> = repository.search(params)
}

abstract class GenMapper_941_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_941_ : GenMapper_941_<GenModel_941_, String>() {
    override fun map(input: GenModel_941_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_941_ : GenMapper_941_<String, GenModel_941_>() {
    override fun map(input: String): GenModel_941_ {
        val parts = input.split(":")
        return GenModel_941_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_941_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_941_,
    private val saveUseCase: GenSaveUseCase_941_,
    private val deleteUseCase: GenDeleteUseCase_941_,
    private val searchUseCase: GenSearchUseCase_941_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_941_>(GenState_941_.Idle)
    val state: StateFlow<GenState_941_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_941_) {
        when (event) {
            is GenEvent_941_.Load -> loadAll()
            is GenEvent_941_.Update -> save(event.model)
            is GenEvent_941_.Delete -> delete(event.id)
            is GenEvent_941_.Refresh -> loadAll()
            is GenEvent_941_.Search -> search(event.query)
            is GenEvent_941_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_941_.Loading; _state.value = GenState_941_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_941_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_941_.Success(searchUseCase(query)) } }
}
