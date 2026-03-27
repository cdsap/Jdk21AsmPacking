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

data class GenModel_13_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_13_ {
    data class Load(val id: Long) : GenEvent_13_()
    data class Update(val model: GenModel_13_) : GenEvent_13_()
    data class Delete(val id: Long) : GenEvent_13_()
    data object Refresh : GenEvent_13_()
    data class Search(val query: String) : GenEvent_13_()
    data class Filter(val predicate: String) : GenEvent_13_()
}

sealed class GenState_13_ {
    data object Idle : GenState_13_()
    data object Loading : GenState_13_()
    data class Success(val items: List<GenModel_13_>) : GenState_13_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_13_()
    data class Partial(val items: List<GenModel_13_>, val hasMore: Boolean) : GenState_13_()
}

interface GenRepository_13_ {
    suspend fun getAll(): List<GenModel_13_>
    suspend fun getById(id: Long): GenModel_13_?
    suspend fun save(model: GenModel_13_): GenModel_13_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_13_>
}

@Singleton
class GenRepositoryImpl_13_ @Inject constructor() : GenRepository_13_ {
    private val store = mutableMapOf<Long, GenModel_13_>()
    override suspend fun getAll(): List<GenModel_13_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_13_? = store[id]
    override suspend fun save(model: GenModel_13_): GenModel_13_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_13_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_13_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_13_ @Inject constructor(
    private val repository: GenRepositoryImpl_13_
) : GenUseCase_13_<Unit, List<GenModel_13_>> {
    override suspend fun invoke(params: Unit): List<GenModel_13_> = repository.getAll()
}

class GenSaveUseCase_13_ @Inject constructor(
    private val repository: GenRepositoryImpl_13_
) : GenUseCase_13_<GenModel_13_, GenModel_13_> {
    override suspend fun invoke(params: GenModel_13_): GenModel_13_ = repository.save(params)
}

class GenDeleteUseCase_13_ @Inject constructor(
    private val repository: GenRepositoryImpl_13_
) : GenUseCase_13_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_13_ @Inject constructor(
    private val repository: GenRepositoryImpl_13_
) : GenUseCase_13_<String, List<GenModel_13_>> {
    override suspend fun invoke(params: String): List<GenModel_13_> = repository.search(params)
}

abstract class GenMapper_13_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_13_ : GenMapper_13_<GenModel_13_, String>() {
    override fun map(input: GenModel_13_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_13_ : GenMapper_13_<String, GenModel_13_>() {
    override fun map(input: String): GenModel_13_ {
        val parts = input.split(":")
        return GenModel_13_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_13_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_13_,
    private val saveUseCase: GenSaveUseCase_13_,
    private val deleteUseCase: GenDeleteUseCase_13_,
    private val searchUseCase: GenSearchUseCase_13_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_13_>(GenState_13_.Idle)
    val state: StateFlow<GenState_13_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_13_) {
        when (event) {
            is GenEvent_13_.Load -> loadAll()
            is GenEvent_13_.Update -> save(event.model)
            is GenEvent_13_.Delete -> delete(event.id)
            is GenEvent_13_.Refresh -> loadAll()
            is GenEvent_13_.Search -> search(event.query)
            is GenEvent_13_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_13_.Loading; _state.value = GenState_13_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_13_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_13_.Success(searchUseCase(query)) } }
}
