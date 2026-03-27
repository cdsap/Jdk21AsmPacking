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

data class GenModel_1935_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1935_ {
    data class Load(val id: Long) : GenEvent_1935_()
    data class Update(val model: GenModel_1935_) : GenEvent_1935_()
    data class Delete(val id: Long) : GenEvent_1935_()
    data object Refresh : GenEvent_1935_()
    data class Search(val query: String) : GenEvent_1935_()
    data class Filter(val predicate: String) : GenEvent_1935_()
}

sealed class GenState_1935_ {
    data object Idle : GenState_1935_()
    data object Loading : GenState_1935_()
    data class Success(val items: List<GenModel_1935_>) : GenState_1935_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1935_()
    data class Partial(val items: List<GenModel_1935_>, val hasMore: Boolean) : GenState_1935_()
}

interface GenRepository_1935_ {
    suspend fun getAll(): List<GenModel_1935_>
    suspend fun getById(id: Long): GenModel_1935_?
    suspend fun save(model: GenModel_1935_): GenModel_1935_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1935_>
}

@Singleton
class GenRepositoryImpl_1935_ @Inject constructor() : GenRepository_1935_ {
    private val store = mutableMapOf<Long, GenModel_1935_>()
    override suspend fun getAll(): List<GenModel_1935_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1935_? = store[id]
    override suspend fun save(model: GenModel_1935_): GenModel_1935_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1935_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1935_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1935_ @Inject constructor(
    private val repository: GenRepositoryImpl_1935_
) : GenUseCase_1935_<Unit, List<GenModel_1935_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1935_> = repository.getAll()
}

class GenSaveUseCase_1935_ @Inject constructor(
    private val repository: GenRepositoryImpl_1935_
) : GenUseCase_1935_<GenModel_1935_, GenModel_1935_> {
    override suspend fun invoke(params: GenModel_1935_): GenModel_1935_ = repository.save(params)
}

class GenDeleteUseCase_1935_ @Inject constructor(
    private val repository: GenRepositoryImpl_1935_
) : GenUseCase_1935_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1935_ @Inject constructor(
    private val repository: GenRepositoryImpl_1935_
) : GenUseCase_1935_<String, List<GenModel_1935_>> {
    override suspend fun invoke(params: String): List<GenModel_1935_> = repository.search(params)
}

abstract class GenMapper_1935_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1935_ : GenMapper_1935_<GenModel_1935_, String>() {
    override fun map(input: GenModel_1935_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1935_ : GenMapper_1935_<String, GenModel_1935_>() {
    override fun map(input: String): GenModel_1935_ {
        val parts = input.split(":")
        return GenModel_1935_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1935_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1935_,
    private val saveUseCase: GenSaveUseCase_1935_,
    private val deleteUseCase: GenDeleteUseCase_1935_,
    private val searchUseCase: GenSearchUseCase_1935_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1935_>(GenState_1935_.Idle)
    val state: StateFlow<GenState_1935_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1935_) {
        when (event) {
            is GenEvent_1935_.Load -> loadAll()
            is GenEvent_1935_.Update -> save(event.model)
            is GenEvent_1935_.Delete -> delete(event.id)
            is GenEvent_1935_.Refresh -> loadAll()
            is GenEvent_1935_.Search -> search(event.query)
            is GenEvent_1935_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1935_.Loading; _state.value = GenState_1935_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1935_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1935_.Success(searchUseCase(query)) } }
}
