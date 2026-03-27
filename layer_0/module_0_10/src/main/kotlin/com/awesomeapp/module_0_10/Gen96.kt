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

data class GenModel_96_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_96_ {
    data class Load(val id: Long) : GenEvent_96_()
    data class Update(val model: GenModel_96_) : GenEvent_96_()
    data class Delete(val id: Long) : GenEvent_96_()
    data object Refresh : GenEvent_96_()
    data class Search(val query: String) : GenEvent_96_()
    data class Filter(val predicate: String) : GenEvent_96_()
}

sealed class GenState_96_ {
    data object Idle : GenState_96_()
    data object Loading : GenState_96_()
    data class Success(val items: List<GenModel_96_>) : GenState_96_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_96_()
    data class Partial(val items: List<GenModel_96_>, val hasMore: Boolean) : GenState_96_()
}

interface GenRepository_96_ {
    suspend fun getAll(): List<GenModel_96_>
    suspend fun getById(id: Long): GenModel_96_?
    suspend fun save(model: GenModel_96_): GenModel_96_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_96_>
}

@Singleton
class GenRepositoryImpl_96_ @Inject constructor() : GenRepository_96_ {
    private val store = mutableMapOf<Long, GenModel_96_>()
    override suspend fun getAll(): List<GenModel_96_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_96_? = store[id]
    override suspend fun save(model: GenModel_96_): GenModel_96_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_96_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_96_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_96_ @Inject constructor(
    private val repository: GenRepositoryImpl_96_
) : GenUseCase_96_<Unit, List<GenModel_96_>> {
    override suspend fun invoke(params: Unit): List<GenModel_96_> = repository.getAll()
}

class GenSaveUseCase_96_ @Inject constructor(
    private val repository: GenRepositoryImpl_96_
) : GenUseCase_96_<GenModel_96_, GenModel_96_> {
    override suspend fun invoke(params: GenModel_96_): GenModel_96_ = repository.save(params)
}

class GenDeleteUseCase_96_ @Inject constructor(
    private val repository: GenRepositoryImpl_96_
) : GenUseCase_96_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_96_ @Inject constructor(
    private val repository: GenRepositoryImpl_96_
) : GenUseCase_96_<String, List<GenModel_96_>> {
    override suspend fun invoke(params: String): List<GenModel_96_> = repository.search(params)
}

abstract class GenMapper_96_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_96_ : GenMapper_96_<GenModel_96_, String>() {
    override fun map(input: GenModel_96_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_96_ : GenMapper_96_<String, GenModel_96_>() {
    override fun map(input: String): GenModel_96_ {
        val parts = input.split(":")
        return GenModel_96_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_96_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_96_,
    private val saveUseCase: GenSaveUseCase_96_,
    private val deleteUseCase: GenDeleteUseCase_96_,
    private val searchUseCase: GenSearchUseCase_96_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_96_>(GenState_96_.Idle)
    val state: StateFlow<GenState_96_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_96_) {
        when (event) {
            is GenEvent_96_.Load -> loadAll()
            is GenEvent_96_.Update -> save(event.model)
            is GenEvent_96_.Delete -> delete(event.id)
            is GenEvent_96_.Refresh -> loadAll()
            is GenEvent_96_.Search -> search(event.query)
            is GenEvent_96_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_96_.Loading; _state.value = GenState_96_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_96_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_96_.Success(searchUseCase(query)) } }
}
