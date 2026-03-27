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

data class GenModel_951_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_951_ {
    data class Load(val id: Long) : GenEvent_951_()
    data class Update(val model: GenModel_951_) : GenEvent_951_()
    data class Delete(val id: Long) : GenEvent_951_()
    data object Refresh : GenEvent_951_()
    data class Search(val query: String) : GenEvent_951_()
    data class Filter(val predicate: String) : GenEvent_951_()
}

sealed class GenState_951_ {
    data object Idle : GenState_951_()
    data object Loading : GenState_951_()
    data class Success(val items: List<GenModel_951_>) : GenState_951_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_951_()
    data class Partial(val items: List<GenModel_951_>, val hasMore: Boolean) : GenState_951_()
}

interface GenRepository_951_ {
    suspend fun getAll(): List<GenModel_951_>
    suspend fun getById(id: Long): GenModel_951_?
    suspend fun save(model: GenModel_951_): GenModel_951_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_951_>
}

@Singleton
class GenRepositoryImpl_951_ @Inject constructor() : GenRepository_951_ {
    private val store = mutableMapOf<Long, GenModel_951_>()
    override suspend fun getAll(): List<GenModel_951_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_951_? = store[id]
    override suspend fun save(model: GenModel_951_): GenModel_951_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_951_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_951_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_951_ @Inject constructor(
    private val repository: GenRepositoryImpl_951_
) : GenUseCase_951_<Unit, List<GenModel_951_>> {
    override suspend fun invoke(params: Unit): List<GenModel_951_> = repository.getAll()
}

class GenSaveUseCase_951_ @Inject constructor(
    private val repository: GenRepositoryImpl_951_
) : GenUseCase_951_<GenModel_951_, GenModel_951_> {
    override suspend fun invoke(params: GenModel_951_): GenModel_951_ = repository.save(params)
}

class GenDeleteUseCase_951_ @Inject constructor(
    private val repository: GenRepositoryImpl_951_
) : GenUseCase_951_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_951_ @Inject constructor(
    private val repository: GenRepositoryImpl_951_
) : GenUseCase_951_<String, List<GenModel_951_>> {
    override suspend fun invoke(params: String): List<GenModel_951_> = repository.search(params)
}

abstract class GenMapper_951_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_951_ : GenMapper_951_<GenModel_951_, String>() {
    override fun map(input: GenModel_951_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_951_ : GenMapper_951_<String, GenModel_951_>() {
    override fun map(input: String): GenModel_951_ {
        val parts = input.split(":")
        return GenModel_951_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_951_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_951_,
    private val saveUseCase: GenSaveUseCase_951_,
    private val deleteUseCase: GenDeleteUseCase_951_,
    private val searchUseCase: GenSearchUseCase_951_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_951_>(GenState_951_.Idle)
    val state: StateFlow<GenState_951_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_951_) {
        when (event) {
            is GenEvent_951_.Load -> loadAll()
            is GenEvent_951_.Update -> save(event.model)
            is GenEvent_951_.Delete -> delete(event.id)
            is GenEvent_951_.Refresh -> loadAll()
            is GenEvent_951_.Search -> search(event.query)
            is GenEvent_951_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_951_.Loading; _state.value = GenState_951_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_951_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_951_.Success(searchUseCase(query)) } }
}
