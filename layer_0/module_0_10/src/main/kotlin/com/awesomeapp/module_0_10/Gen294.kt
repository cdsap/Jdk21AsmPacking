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

data class GenModel_294_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_294_ {
    data class Load(val id: Long) : GenEvent_294_()
    data class Update(val model: GenModel_294_) : GenEvent_294_()
    data class Delete(val id: Long) : GenEvent_294_()
    data object Refresh : GenEvent_294_()
    data class Search(val query: String) : GenEvent_294_()
    data class Filter(val predicate: String) : GenEvent_294_()
}

sealed class GenState_294_ {
    data object Idle : GenState_294_()
    data object Loading : GenState_294_()
    data class Success(val items: List<GenModel_294_>) : GenState_294_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_294_()
    data class Partial(val items: List<GenModel_294_>, val hasMore: Boolean) : GenState_294_()
}

interface GenRepository_294_ {
    suspend fun getAll(): List<GenModel_294_>
    suspend fun getById(id: Long): GenModel_294_?
    suspend fun save(model: GenModel_294_): GenModel_294_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_294_>
}

@Singleton
class GenRepositoryImpl_294_ @Inject constructor() : GenRepository_294_ {
    private val store = mutableMapOf<Long, GenModel_294_>()
    override suspend fun getAll(): List<GenModel_294_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_294_? = store[id]
    override suspend fun save(model: GenModel_294_): GenModel_294_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_294_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_294_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_294_ @Inject constructor(
    private val repository: GenRepositoryImpl_294_
) : GenUseCase_294_<Unit, List<GenModel_294_>> {
    override suspend fun invoke(params: Unit): List<GenModel_294_> = repository.getAll()
}

class GenSaveUseCase_294_ @Inject constructor(
    private val repository: GenRepositoryImpl_294_
) : GenUseCase_294_<GenModel_294_, GenModel_294_> {
    override suspend fun invoke(params: GenModel_294_): GenModel_294_ = repository.save(params)
}

class GenDeleteUseCase_294_ @Inject constructor(
    private val repository: GenRepositoryImpl_294_
) : GenUseCase_294_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_294_ @Inject constructor(
    private val repository: GenRepositoryImpl_294_
) : GenUseCase_294_<String, List<GenModel_294_>> {
    override suspend fun invoke(params: String): List<GenModel_294_> = repository.search(params)
}

abstract class GenMapper_294_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_294_ : GenMapper_294_<GenModel_294_, String>() {
    override fun map(input: GenModel_294_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_294_ : GenMapper_294_<String, GenModel_294_>() {
    override fun map(input: String): GenModel_294_ {
        val parts = input.split(":")
        return GenModel_294_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_294_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_294_,
    private val saveUseCase: GenSaveUseCase_294_,
    private val deleteUseCase: GenDeleteUseCase_294_,
    private val searchUseCase: GenSearchUseCase_294_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_294_>(GenState_294_.Idle)
    val state: StateFlow<GenState_294_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_294_) {
        when (event) {
            is GenEvent_294_.Load -> loadAll()
            is GenEvent_294_.Update -> save(event.model)
            is GenEvent_294_.Delete -> delete(event.id)
            is GenEvent_294_.Refresh -> loadAll()
            is GenEvent_294_.Search -> search(event.query)
            is GenEvent_294_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_294_.Loading; _state.value = GenState_294_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_294_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_294_.Success(searchUseCase(query)) } }
}
