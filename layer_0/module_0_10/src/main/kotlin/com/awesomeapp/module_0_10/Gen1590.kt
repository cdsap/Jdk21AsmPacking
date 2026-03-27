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

data class GenModel_1590_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1590_ {
    data class Load(val id: Long) : GenEvent_1590_()
    data class Update(val model: GenModel_1590_) : GenEvent_1590_()
    data class Delete(val id: Long) : GenEvent_1590_()
    data object Refresh : GenEvent_1590_()
    data class Search(val query: String) : GenEvent_1590_()
    data class Filter(val predicate: String) : GenEvent_1590_()
}

sealed class GenState_1590_ {
    data object Idle : GenState_1590_()
    data object Loading : GenState_1590_()
    data class Success(val items: List<GenModel_1590_>) : GenState_1590_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1590_()
    data class Partial(val items: List<GenModel_1590_>, val hasMore: Boolean) : GenState_1590_()
}

interface GenRepository_1590_ {
    suspend fun getAll(): List<GenModel_1590_>
    suspend fun getById(id: Long): GenModel_1590_?
    suspend fun save(model: GenModel_1590_): GenModel_1590_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1590_>
}

@Singleton
class GenRepositoryImpl_1590_ @Inject constructor() : GenRepository_1590_ {
    private val store = mutableMapOf<Long, GenModel_1590_>()
    override suspend fun getAll(): List<GenModel_1590_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1590_? = store[id]
    override suspend fun save(model: GenModel_1590_): GenModel_1590_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1590_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1590_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1590_ @Inject constructor(
    private val repository: GenRepositoryImpl_1590_
) : GenUseCase_1590_<Unit, List<GenModel_1590_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1590_> = repository.getAll()
}

class GenSaveUseCase_1590_ @Inject constructor(
    private val repository: GenRepositoryImpl_1590_
) : GenUseCase_1590_<GenModel_1590_, GenModel_1590_> {
    override suspend fun invoke(params: GenModel_1590_): GenModel_1590_ = repository.save(params)
}

class GenDeleteUseCase_1590_ @Inject constructor(
    private val repository: GenRepositoryImpl_1590_
) : GenUseCase_1590_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1590_ @Inject constructor(
    private val repository: GenRepositoryImpl_1590_
) : GenUseCase_1590_<String, List<GenModel_1590_>> {
    override suspend fun invoke(params: String): List<GenModel_1590_> = repository.search(params)
}

abstract class GenMapper_1590_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1590_ : GenMapper_1590_<GenModel_1590_, String>() {
    override fun map(input: GenModel_1590_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1590_ : GenMapper_1590_<String, GenModel_1590_>() {
    override fun map(input: String): GenModel_1590_ {
        val parts = input.split(":")
        return GenModel_1590_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1590_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1590_,
    private val saveUseCase: GenSaveUseCase_1590_,
    private val deleteUseCase: GenDeleteUseCase_1590_,
    private val searchUseCase: GenSearchUseCase_1590_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1590_>(GenState_1590_.Idle)
    val state: StateFlow<GenState_1590_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1590_) {
        when (event) {
            is GenEvent_1590_.Load -> loadAll()
            is GenEvent_1590_.Update -> save(event.model)
            is GenEvent_1590_.Delete -> delete(event.id)
            is GenEvent_1590_.Refresh -> loadAll()
            is GenEvent_1590_.Search -> search(event.query)
            is GenEvent_1590_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1590_.Loading; _state.value = GenState_1590_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1590_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1590_.Success(searchUseCase(query)) } }
}
