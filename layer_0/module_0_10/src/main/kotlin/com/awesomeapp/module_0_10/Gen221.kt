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

data class GenModel_221_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_221_ {
    data class Load(val id: Long) : GenEvent_221_()
    data class Update(val model: GenModel_221_) : GenEvent_221_()
    data class Delete(val id: Long) : GenEvent_221_()
    data object Refresh : GenEvent_221_()
    data class Search(val query: String) : GenEvent_221_()
    data class Filter(val predicate: String) : GenEvent_221_()
}

sealed class GenState_221_ {
    data object Idle : GenState_221_()
    data object Loading : GenState_221_()
    data class Success(val items: List<GenModel_221_>) : GenState_221_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_221_()
    data class Partial(val items: List<GenModel_221_>, val hasMore: Boolean) : GenState_221_()
}

interface GenRepository_221_ {
    suspend fun getAll(): List<GenModel_221_>
    suspend fun getById(id: Long): GenModel_221_?
    suspend fun save(model: GenModel_221_): GenModel_221_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_221_>
}

@Singleton
class GenRepositoryImpl_221_ @Inject constructor() : GenRepository_221_ {
    private val store = mutableMapOf<Long, GenModel_221_>()
    override suspend fun getAll(): List<GenModel_221_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_221_? = store[id]
    override suspend fun save(model: GenModel_221_): GenModel_221_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_221_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_221_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_221_ @Inject constructor(
    private val repository: GenRepositoryImpl_221_
) : GenUseCase_221_<Unit, List<GenModel_221_>> {
    override suspend fun invoke(params: Unit): List<GenModel_221_> = repository.getAll()
}

class GenSaveUseCase_221_ @Inject constructor(
    private val repository: GenRepositoryImpl_221_
) : GenUseCase_221_<GenModel_221_, GenModel_221_> {
    override suspend fun invoke(params: GenModel_221_): GenModel_221_ = repository.save(params)
}

class GenDeleteUseCase_221_ @Inject constructor(
    private val repository: GenRepositoryImpl_221_
) : GenUseCase_221_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_221_ @Inject constructor(
    private val repository: GenRepositoryImpl_221_
) : GenUseCase_221_<String, List<GenModel_221_>> {
    override suspend fun invoke(params: String): List<GenModel_221_> = repository.search(params)
}

abstract class GenMapper_221_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_221_ : GenMapper_221_<GenModel_221_, String>() {
    override fun map(input: GenModel_221_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_221_ : GenMapper_221_<String, GenModel_221_>() {
    override fun map(input: String): GenModel_221_ {
        val parts = input.split(":")
        return GenModel_221_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_221_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_221_,
    private val saveUseCase: GenSaveUseCase_221_,
    private val deleteUseCase: GenDeleteUseCase_221_,
    private val searchUseCase: GenSearchUseCase_221_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_221_>(GenState_221_.Idle)
    val state: StateFlow<GenState_221_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_221_) {
        when (event) {
            is GenEvent_221_.Load -> loadAll()
            is GenEvent_221_.Update -> save(event.model)
            is GenEvent_221_.Delete -> delete(event.id)
            is GenEvent_221_.Refresh -> loadAll()
            is GenEvent_221_.Search -> search(event.query)
            is GenEvent_221_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_221_.Loading; _state.value = GenState_221_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_221_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_221_.Success(searchUseCase(query)) } }
}
