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

data class GenModel_1339_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1339_ {
    data class Load(val id: Long) : GenEvent_1339_()
    data class Update(val model: GenModel_1339_) : GenEvent_1339_()
    data class Delete(val id: Long) : GenEvent_1339_()
    data object Refresh : GenEvent_1339_()
    data class Search(val query: String) : GenEvent_1339_()
    data class Filter(val predicate: String) : GenEvent_1339_()
}

sealed class GenState_1339_ {
    data object Idle : GenState_1339_()
    data object Loading : GenState_1339_()
    data class Success(val items: List<GenModel_1339_>) : GenState_1339_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1339_()
    data class Partial(val items: List<GenModel_1339_>, val hasMore: Boolean) : GenState_1339_()
}

interface GenRepository_1339_ {
    suspend fun getAll(): List<GenModel_1339_>
    suspend fun getById(id: Long): GenModel_1339_?
    suspend fun save(model: GenModel_1339_): GenModel_1339_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1339_>
}

@Singleton
class GenRepositoryImpl_1339_ @Inject constructor() : GenRepository_1339_ {
    private val store = mutableMapOf<Long, GenModel_1339_>()
    override suspend fun getAll(): List<GenModel_1339_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1339_? = store[id]
    override suspend fun save(model: GenModel_1339_): GenModel_1339_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1339_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1339_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1339_ @Inject constructor(
    private val repository: GenRepositoryImpl_1339_
) : GenUseCase_1339_<Unit, List<GenModel_1339_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1339_> = repository.getAll()
}

class GenSaveUseCase_1339_ @Inject constructor(
    private val repository: GenRepositoryImpl_1339_
) : GenUseCase_1339_<GenModel_1339_, GenModel_1339_> {
    override suspend fun invoke(params: GenModel_1339_): GenModel_1339_ = repository.save(params)
}

class GenDeleteUseCase_1339_ @Inject constructor(
    private val repository: GenRepositoryImpl_1339_
) : GenUseCase_1339_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1339_ @Inject constructor(
    private val repository: GenRepositoryImpl_1339_
) : GenUseCase_1339_<String, List<GenModel_1339_>> {
    override suspend fun invoke(params: String): List<GenModel_1339_> = repository.search(params)
}

abstract class GenMapper_1339_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1339_ : GenMapper_1339_<GenModel_1339_, String>() {
    override fun map(input: GenModel_1339_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1339_ : GenMapper_1339_<String, GenModel_1339_>() {
    override fun map(input: String): GenModel_1339_ {
        val parts = input.split(":")
        return GenModel_1339_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1339_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1339_,
    private val saveUseCase: GenSaveUseCase_1339_,
    private val deleteUseCase: GenDeleteUseCase_1339_,
    private val searchUseCase: GenSearchUseCase_1339_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1339_>(GenState_1339_.Idle)
    val state: StateFlow<GenState_1339_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1339_) {
        when (event) {
            is GenEvent_1339_.Load -> loadAll()
            is GenEvent_1339_.Update -> save(event.model)
            is GenEvent_1339_.Delete -> delete(event.id)
            is GenEvent_1339_.Refresh -> loadAll()
            is GenEvent_1339_.Search -> search(event.query)
            is GenEvent_1339_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1339_.Loading; _state.value = GenState_1339_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1339_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1339_.Success(searchUseCase(query)) } }
}
