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

data class GenModel_410_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_410_ {
    data class Load(val id: Long) : GenEvent_410_()
    data class Update(val model: GenModel_410_) : GenEvent_410_()
    data class Delete(val id: Long) : GenEvent_410_()
    data object Refresh : GenEvent_410_()
    data class Search(val query: String) : GenEvent_410_()
    data class Filter(val predicate: String) : GenEvent_410_()
}

sealed class GenState_410_ {
    data object Idle : GenState_410_()
    data object Loading : GenState_410_()
    data class Success(val items: List<GenModel_410_>) : GenState_410_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_410_()
    data class Partial(val items: List<GenModel_410_>, val hasMore: Boolean) : GenState_410_()
}

interface GenRepository_410_ {
    suspend fun getAll(): List<GenModel_410_>
    suspend fun getById(id: Long): GenModel_410_?
    suspend fun save(model: GenModel_410_): GenModel_410_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_410_>
}

@Singleton
class GenRepositoryImpl_410_ @Inject constructor() : GenRepository_410_ {
    private val store = mutableMapOf<Long, GenModel_410_>()
    override suspend fun getAll(): List<GenModel_410_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_410_? = store[id]
    override suspend fun save(model: GenModel_410_): GenModel_410_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_410_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_410_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_410_ @Inject constructor(
    private val repository: GenRepositoryImpl_410_
) : GenUseCase_410_<Unit, List<GenModel_410_>> {
    override suspend fun invoke(params: Unit): List<GenModel_410_> = repository.getAll()
}

class GenSaveUseCase_410_ @Inject constructor(
    private val repository: GenRepositoryImpl_410_
) : GenUseCase_410_<GenModel_410_, GenModel_410_> {
    override suspend fun invoke(params: GenModel_410_): GenModel_410_ = repository.save(params)
}

class GenDeleteUseCase_410_ @Inject constructor(
    private val repository: GenRepositoryImpl_410_
) : GenUseCase_410_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_410_ @Inject constructor(
    private val repository: GenRepositoryImpl_410_
) : GenUseCase_410_<String, List<GenModel_410_>> {
    override suspend fun invoke(params: String): List<GenModel_410_> = repository.search(params)
}

abstract class GenMapper_410_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_410_ : GenMapper_410_<GenModel_410_, String>() {
    override fun map(input: GenModel_410_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_410_ : GenMapper_410_<String, GenModel_410_>() {
    override fun map(input: String): GenModel_410_ {
        val parts = input.split(":")
        return GenModel_410_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_410_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_410_,
    private val saveUseCase: GenSaveUseCase_410_,
    private val deleteUseCase: GenDeleteUseCase_410_,
    private val searchUseCase: GenSearchUseCase_410_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_410_>(GenState_410_.Idle)
    val state: StateFlow<GenState_410_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_410_) {
        when (event) {
            is GenEvent_410_.Load -> loadAll()
            is GenEvent_410_.Update -> save(event.model)
            is GenEvent_410_.Delete -> delete(event.id)
            is GenEvent_410_.Refresh -> loadAll()
            is GenEvent_410_.Search -> search(event.query)
            is GenEvent_410_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_410_.Loading; _state.value = GenState_410_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_410_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_410_.Success(searchUseCase(query)) } }
}
