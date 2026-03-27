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

data class GenModel_135_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_135_ {
    data class Load(val id: Long) : GenEvent_135_()
    data class Update(val model: GenModel_135_) : GenEvent_135_()
    data class Delete(val id: Long) : GenEvent_135_()
    data object Refresh : GenEvent_135_()
    data class Search(val query: String) : GenEvent_135_()
    data class Filter(val predicate: String) : GenEvent_135_()
}

sealed class GenState_135_ {
    data object Idle : GenState_135_()
    data object Loading : GenState_135_()
    data class Success(val items: List<GenModel_135_>) : GenState_135_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_135_()
    data class Partial(val items: List<GenModel_135_>, val hasMore: Boolean) : GenState_135_()
}

interface GenRepository_135_ {
    suspend fun getAll(): List<GenModel_135_>
    suspend fun getById(id: Long): GenModel_135_?
    suspend fun save(model: GenModel_135_): GenModel_135_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_135_>
}

@Singleton
class GenRepositoryImpl_135_ @Inject constructor() : GenRepository_135_ {
    private val store = mutableMapOf<Long, GenModel_135_>()
    override suspend fun getAll(): List<GenModel_135_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_135_? = store[id]
    override suspend fun save(model: GenModel_135_): GenModel_135_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_135_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_135_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_135_ @Inject constructor(
    private val repository: GenRepositoryImpl_135_
) : GenUseCase_135_<Unit, List<GenModel_135_>> {
    override suspend fun invoke(params: Unit): List<GenModel_135_> = repository.getAll()
}

class GenSaveUseCase_135_ @Inject constructor(
    private val repository: GenRepositoryImpl_135_
) : GenUseCase_135_<GenModel_135_, GenModel_135_> {
    override suspend fun invoke(params: GenModel_135_): GenModel_135_ = repository.save(params)
}

class GenDeleteUseCase_135_ @Inject constructor(
    private val repository: GenRepositoryImpl_135_
) : GenUseCase_135_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_135_ @Inject constructor(
    private val repository: GenRepositoryImpl_135_
) : GenUseCase_135_<String, List<GenModel_135_>> {
    override suspend fun invoke(params: String): List<GenModel_135_> = repository.search(params)
}

abstract class GenMapper_135_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_135_ : GenMapper_135_<GenModel_135_, String>() {
    override fun map(input: GenModel_135_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_135_ : GenMapper_135_<String, GenModel_135_>() {
    override fun map(input: String): GenModel_135_ {
        val parts = input.split(":")
        return GenModel_135_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_135_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_135_,
    private val saveUseCase: GenSaveUseCase_135_,
    private val deleteUseCase: GenDeleteUseCase_135_,
    private val searchUseCase: GenSearchUseCase_135_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_135_>(GenState_135_.Idle)
    val state: StateFlow<GenState_135_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_135_) {
        when (event) {
            is GenEvent_135_.Load -> loadAll()
            is GenEvent_135_.Update -> save(event.model)
            is GenEvent_135_.Delete -> delete(event.id)
            is GenEvent_135_.Refresh -> loadAll()
            is GenEvent_135_.Search -> search(event.query)
            is GenEvent_135_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_135_.Loading; _state.value = GenState_135_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_135_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_135_.Success(searchUseCase(query)) } }
}
