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

data class GenModel_104_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_104_ {
    data class Load(val id: Long) : GenEvent_104_()
    data class Update(val model: GenModel_104_) : GenEvent_104_()
    data class Delete(val id: Long) : GenEvent_104_()
    data object Refresh : GenEvent_104_()
    data class Search(val query: String) : GenEvent_104_()
    data class Filter(val predicate: String) : GenEvent_104_()
}

sealed class GenState_104_ {
    data object Idle : GenState_104_()
    data object Loading : GenState_104_()
    data class Success(val items: List<GenModel_104_>) : GenState_104_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_104_()
    data class Partial(val items: List<GenModel_104_>, val hasMore: Boolean) : GenState_104_()
}

interface GenRepository_104_ {
    suspend fun getAll(): List<GenModel_104_>
    suspend fun getById(id: Long): GenModel_104_?
    suspend fun save(model: GenModel_104_): GenModel_104_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_104_>
}

@Singleton
class GenRepositoryImpl_104_ @Inject constructor() : GenRepository_104_ {
    private val store = mutableMapOf<Long, GenModel_104_>()
    override suspend fun getAll(): List<GenModel_104_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_104_? = store[id]
    override suspend fun save(model: GenModel_104_): GenModel_104_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_104_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_104_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_104_ @Inject constructor(
    private val repository: GenRepositoryImpl_104_
) : GenUseCase_104_<Unit, List<GenModel_104_>> {
    override suspend fun invoke(params: Unit): List<GenModel_104_> = repository.getAll()
}

class GenSaveUseCase_104_ @Inject constructor(
    private val repository: GenRepositoryImpl_104_
) : GenUseCase_104_<GenModel_104_, GenModel_104_> {
    override suspend fun invoke(params: GenModel_104_): GenModel_104_ = repository.save(params)
}

class GenDeleteUseCase_104_ @Inject constructor(
    private val repository: GenRepositoryImpl_104_
) : GenUseCase_104_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_104_ @Inject constructor(
    private val repository: GenRepositoryImpl_104_
) : GenUseCase_104_<String, List<GenModel_104_>> {
    override suspend fun invoke(params: String): List<GenModel_104_> = repository.search(params)
}

abstract class GenMapper_104_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_104_ : GenMapper_104_<GenModel_104_, String>() {
    override fun map(input: GenModel_104_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_104_ : GenMapper_104_<String, GenModel_104_>() {
    override fun map(input: String): GenModel_104_ {
        val parts = input.split(":")
        return GenModel_104_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_104_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_104_,
    private val saveUseCase: GenSaveUseCase_104_,
    private val deleteUseCase: GenDeleteUseCase_104_,
    private val searchUseCase: GenSearchUseCase_104_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_104_>(GenState_104_.Idle)
    val state: StateFlow<GenState_104_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_104_) {
        when (event) {
            is GenEvent_104_.Load -> loadAll()
            is GenEvent_104_.Update -> save(event.model)
            is GenEvent_104_.Delete -> delete(event.id)
            is GenEvent_104_.Refresh -> loadAll()
            is GenEvent_104_.Search -> search(event.query)
            is GenEvent_104_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_104_.Loading; _state.value = GenState_104_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_104_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_104_.Success(searchUseCase(query)) } }
}
