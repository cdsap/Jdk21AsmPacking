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

data class GenModel_590_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_590_ {
    data class Load(val id: Long) : GenEvent_590_()
    data class Update(val model: GenModel_590_) : GenEvent_590_()
    data class Delete(val id: Long) : GenEvent_590_()
    data object Refresh : GenEvent_590_()
    data class Search(val query: String) : GenEvent_590_()
    data class Filter(val predicate: String) : GenEvent_590_()
}

sealed class GenState_590_ {
    data object Idle : GenState_590_()
    data object Loading : GenState_590_()
    data class Success(val items: List<GenModel_590_>) : GenState_590_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_590_()
    data class Partial(val items: List<GenModel_590_>, val hasMore: Boolean) : GenState_590_()
}

interface GenRepository_590_ {
    suspend fun getAll(): List<GenModel_590_>
    suspend fun getById(id: Long): GenModel_590_?
    suspend fun save(model: GenModel_590_): GenModel_590_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_590_>
}

@Singleton
class GenRepositoryImpl_590_ @Inject constructor() : GenRepository_590_ {
    private val store = mutableMapOf<Long, GenModel_590_>()
    override suspend fun getAll(): List<GenModel_590_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_590_? = store[id]
    override suspend fun save(model: GenModel_590_): GenModel_590_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_590_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_590_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_590_ @Inject constructor(
    private val repository: GenRepositoryImpl_590_
) : GenUseCase_590_<Unit, List<GenModel_590_>> {
    override suspend fun invoke(params: Unit): List<GenModel_590_> = repository.getAll()
}

class GenSaveUseCase_590_ @Inject constructor(
    private val repository: GenRepositoryImpl_590_
) : GenUseCase_590_<GenModel_590_, GenModel_590_> {
    override suspend fun invoke(params: GenModel_590_): GenModel_590_ = repository.save(params)
}

class GenDeleteUseCase_590_ @Inject constructor(
    private val repository: GenRepositoryImpl_590_
) : GenUseCase_590_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_590_ @Inject constructor(
    private val repository: GenRepositoryImpl_590_
) : GenUseCase_590_<String, List<GenModel_590_>> {
    override suspend fun invoke(params: String): List<GenModel_590_> = repository.search(params)
}

abstract class GenMapper_590_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_590_ : GenMapper_590_<GenModel_590_, String>() {
    override fun map(input: GenModel_590_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_590_ : GenMapper_590_<String, GenModel_590_>() {
    override fun map(input: String): GenModel_590_ {
        val parts = input.split(":")
        return GenModel_590_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_590_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_590_,
    private val saveUseCase: GenSaveUseCase_590_,
    private val deleteUseCase: GenDeleteUseCase_590_,
    private val searchUseCase: GenSearchUseCase_590_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_590_>(GenState_590_.Idle)
    val state: StateFlow<GenState_590_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_590_) {
        when (event) {
            is GenEvent_590_.Load -> loadAll()
            is GenEvent_590_.Update -> save(event.model)
            is GenEvent_590_.Delete -> delete(event.id)
            is GenEvent_590_.Refresh -> loadAll()
            is GenEvent_590_.Search -> search(event.query)
            is GenEvent_590_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_590_.Loading; _state.value = GenState_590_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_590_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_590_.Success(searchUseCase(query)) } }
}
