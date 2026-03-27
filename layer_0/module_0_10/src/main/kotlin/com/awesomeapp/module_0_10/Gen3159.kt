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

data class GenModel_3159_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3159_ {
    data class Load(val id: Long) : GenEvent_3159_()
    data class Update(val model: GenModel_3159_) : GenEvent_3159_()
    data class Delete(val id: Long) : GenEvent_3159_()
    data object Refresh : GenEvent_3159_()
    data class Search(val query: String) : GenEvent_3159_()
    data class Filter(val predicate: String) : GenEvent_3159_()
}

sealed class GenState_3159_ {
    data object Idle : GenState_3159_()
    data object Loading : GenState_3159_()
    data class Success(val items: List<GenModel_3159_>) : GenState_3159_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3159_()
    data class Partial(val items: List<GenModel_3159_>, val hasMore: Boolean) : GenState_3159_()
}

interface GenRepository_3159_ {
    suspend fun getAll(): List<GenModel_3159_>
    suspend fun getById(id: Long): GenModel_3159_?
    suspend fun save(model: GenModel_3159_): GenModel_3159_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3159_>
}

@Singleton
class GenRepositoryImpl_3159_ @Inject constructor() : GenRepository_3159_ {
    private val store = mutableMapOf<Long, GenModel_3159_>()
    override suspend fun getAll(): List<GenModel_3159_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3159_? = store[id]
    override suspend fun save(model: GenModel_3159_): GenModel_3159_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3159_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3159_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3159_ @Inject constructor(
    private val repository: GenRepositoryImpl_3159_
) : GenUseCase_3159_<Unit, List<GenModel_3159_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3159_> = repository.getAll()
}

class GenSaveUseCase_3159_ @Inject constructor(
    private val repository: GenRepositoryImpl_3159_
) : GenUseCase_3159_<GenModel_3159_, GenModel_3159_> {
    override suspend fun invoke(params: GenModel_3159_): GenModel_3159_ = repository.save(params)
}

class GenDeleteUseCase_3159_ @Inject constructor(
    private val repository: GenRepositoryImpl_3159_
) : GenUseCase_3159_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3159_ @Inject constructor(
    private val repository: GenRepositoryImpl_3159_
) : GenUseCase_3159_<String, List<GenModel_3159_>> {
    override suspend fun invoke(params: String): List<GenModel_3159_> = repository.search(params)
}

abstract class GenMapper_3159_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3159_ : GenMapper_3159_<GenModel_3159_, String>() {
    override fun map(input: GenModel_3159_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3159_ : GenMapper_3159_<String, GenModel_3159_>() {
    override fun map(input: String): GenModel_3159_ {
        val parts = input.split(":")
        return GenModel_3159_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3159_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3159_,
    private val saveUseCase: GenSaveUseCase_3159_,
    private val deleteUseCase: GenDeleteUseCase_3159_,
    private val searchUseCase: GenSearchUseCase_3159_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3159_>(GenState_3159_.Idle)
    val state: StateFlow<GenState_3159_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3159_) {
        when (event) {
            is GenEvent_3159_.Load -> loadAll()
            is GenEvent_3159_.Update -> save(event.model)
            is GenEvent_3159_.Delete -> delete(event.id)
            is GenEvent_3159_.Refresh -> loadAll()
            is GenEvent_3159_.Search -> search(event.query)
            is GenEvent_3159_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3159_.Loading; _state.value = GenState_3159_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3159_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3159_.Success(searchUseCase(query)) } }
}
