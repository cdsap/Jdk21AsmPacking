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

data class GenModel_74_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_74_ {
    data class Load(val id: Long) : GenEvent_74_()
    data class Update(val model: GenModel_74_) : GenEvent_74_()
    data class Delete(val id: Long) : GenEvent_74_()
    data object Refresh : GenEvent_74_()
    data class Search(val query: String) : GenEvent_74_()
    data class Filter(val predicate: String) : GenEvent_74_()
}

sealed class GenState_74_ {
    data object Idle : GenState_74_()
    data object Loading : GenState_74_()
    data class Success(val items: List<GenModel_74_>) : GenState_74_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_74_()
    data class Partial(val items: List<GenModel_74_>, val hasMore: Boolean) : GenState_74_()
}

interface GenRepository_74_ {
    suspend fun getAll(): List<GenModel_74_>
    suspend fun getById(id: Long): GenModel_74_?
    suspend fun save(model: GenModel_74_): GenModel_74_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_74_>
}

@Singleton
class GenRepositoryImpl_74_ @Inject constructor() : GenRepository_74_ {
    private val store = mutableMapOf<Long, GenModel_74_>()
    override suspend fun getAll(): List<GenModel_74_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_74_? = store[id]
    override suspend fun save(model: GenModel_74_): GenModel_74_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_74_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_74_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_74_ @Inject constructor(
    private val repository: GenRepositoryImpl_74_
) : GenUseCase_74_<Unit, List<GenModel_74_>> {
    override suspend fun invoke(params: Unit): List<GenModel_74_> = repository.getAll()
}

class GenSaveUseCase_74_ @Inject constructor(
    private val repository: GenRepositoryImpl_74_
) : GenUseCase_74_<GenModel_74_, GenModel_74_> {
    override suspend fun invoke(params: GenModel_74_): GenModel_74_ = repository.save(params)
}

class GenDeleteUseCase_74_ @Inject constructor(
    private val repository: GenRepositoryImpl_74_
) : GenUseCase_74_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_74_ @Inject constructor(
    private val repository: GenRepositoryImpl_74_
) : GenUseCase_74_<String, List<GenModel_74_>> {
    override suspend fun invoke(params: String): List<GenModel_74_> = repository.search(params)
}

abstract class GenMapper_74_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_74_ : GenMapper_74_<GenModel_74_, String>() {
    override fun map(input: GenModel_74_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_74_ : GenMapper_74_<String, GenModel_74_>() {
    override fun map(input: String): GenModel_74_ {
        val parts = input.split(":")
        return GenModel_74_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_74_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_74_,
    private val saveUseCase: GenSaveUseCase_74_,
    private val deleteUseCase: GenDeleteUseCase_74_,
    private val searchUseCase: GenSearchUseCase_74_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_74_>(GenState_74_.Idle)
    val state: StateFlow<GenState_74_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_74_) {
        when (event) {
            is GenEvent_74_.Load -> loadAll()
            is GenEvent_74_.Update -> save(event.model)
            is GenEvent_74_.Delete -> delete(event.id)
            is GenEvent_74_.Refresh -> loadAll()
            is GenEvent_74_.Search -> search(event.query)
            is GenEvent_74_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_74_.Loading; _state.value = GenState_74_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_74_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_74_.Success(searchUseCase(query)) } }
}
