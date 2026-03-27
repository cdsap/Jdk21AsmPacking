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

data class GenModel_571_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_571_ {
    data class Load(val id: Long) : GenEvent_571_()
    data class Update(val model: GenModel_571_) : GenEvent_571_()
    data class Delete(val id: Long) : GenEvent_571_()
    data object Refresh : GenEvent_571_()
    data class Search(val query: String) : GenEvent_571_()
    data class Filter(val predicate: String) : GenEvent_571_()
}

sealed class GenState_571_ {
    data object Idle : GenState_571_()
    data object Loading : GenState_571_()
    data class Success(val items: List<GenModel_571_>) : GenState_571_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_571_()
    data class Partial(val items: List<GenModel_571_>, val hasMore: Boolean) : GenState_571_()
}

interface GenRepository_571_ {
    suspend fun getAll(): List<GenModel_571_>
    suspend fun getById(id: Long): GenModel_571_?
    suspend fun save(model: GenModel_571_): GenModel_571_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_571_>
}

@Singleton
class GenRepositoryImpl_571_ @Inject constructor() : GenRepository_571_ {
    private val store = mutableMapOf<Long, GenModel_571_>()
    override suspend fun getAll(): List<GenModel_571_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_571_? = store[id]
    override suspend fun save(model: GenModel_571_): GenModel_571_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_571_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_571_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_571_ @Inject constructor(
    private val repository: GenRepositoryImpl_571_
) : GenUseCase_571_<Unit, List<GenModel_571_>> {
    override suspend fun invoke(params: Unit): List<GenModel_571_> = repository.getAll()
}

class GenSaveUseCase_571_ @Inject constructor(
    private val repository: GenRepositoryImpl_571_
) : GenUseCase_571_<GenModel_571_, GenModel_571_> {
    override suspend fun invoke(params: GenModel_571_): GenModel_571_ = repository.save(params)
}

class GenDeleteUseCase_571_ @Inject constructor(
    private val repository: GenRepositoryImpl_571_
) : GenUseCase_571_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_571_ @Inject constructor(
    private val repository: GenRepositoryImpl_571_
) : GenUseCase_571_<String, List<GenModel_571_>> {
    override suspend fun invoke(params: String): List<GenModel_571_> = repository.search(params)
}

abstract class GenMapper_571_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_571_ : GenMapper_571_<GenModel_571_, String>() {
    override fun map(input: GenModel_571_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_571_ : GenMapper_571_<String, GenModel_571_>() {
    override fun map(input: String): GenModel_571_ {
        val parts = input.split(":")
        return GenModel_571_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_571_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_571_,
    private val saveUseCase: GenSaveUseCase_571_,
    private val deleteUseCase: GenDeleteUseCase_571_,
    private val searchUseCase: GenSearchUseCase_571_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_571_>(GenState_571_.Idle)
    val state: StateFlow<GenState_571_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_571_) {
        when (event) {
            is GenEvent_571_.Load -> loadAll()
            is GenEvent_571_.Update -> save(event.model)
            is GenEvent_571_.Delete -> delete(event.id)
            is GenEvent_571_.Refresh -> loadAll()
            is GenEvent_571_.Search -> search(event.query)
            is GenEvent_571_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_571_.Loading; _state.value = GenState_571_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_571_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_571_.Success(searchUseCase(query)) } }
}
