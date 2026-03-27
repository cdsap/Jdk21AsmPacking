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

data class GenModel_33_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_33_ {
    data class Load(val id: Long) : GenEvent_33_()
    data class Update(val model: GenModel_33_) : GenEvent_33_()
    data class Delete(val id: Long) : GenEvent_33_()
    data object Refresh : GenEvent_33_()
    data class Search(val query: String) : GenEvent_33_()
    data class Filter(val predicate: String) : GenEvent_33_()
}

sealed class GenState_33_ {
    data object Idle : GenState_33_()
    data object Loading : GenState_33_()
    data class Success(val items: List<GenModel_33_>) : GenState_33_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_33_()
    data class Partial(val items: List<GenModel_33_>, val hasMore: Boolean) : GenState_33_()
}

interface GenRepository_33_ {
    suspend fun getAll(): List<GenModel_33_>
    suspend fun getById(id: Long): GenModel_33_?
    suspend fun save(model: GenModel_33_): GenModel_33_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_33_>
}

@Singleton
class GenRepositoryImpl_33_ @Inject constructor() : GenRepository_33_ {
    private val store = mutableMapOf<Long, GenModel_33_>()
    override suspend fun getAll(): List<GenModel_33_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_33_? = store[id]
    override suspend fun save(model: GenModel_33_): GenModel_33_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_33_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_33_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_33_ @Inject constructor(
    private val repository: GenRepositoryImpl_33_
) : GenUseCase_33_<Unit, List<GenModel_33_>> {
    override suspend fun invoke(params: Unit): List<GenModel_33_> = repository.getAll()
}

class GenSaveUseCase_33_ @Inject constructor(
    private val repository: GenRepositoryImpl_33_
) : GenUseCase_33_<GenModel_33_, GenModel_33_> {
    override suspend fun invoke(params: GenModel_33_): GenModel_33_ = repository.save(params)
}

class GenDeleteUseCase_33_ @Inject constructor(
    private val repository: GenRepositoryImpl_33_
) : GenUseCase_33_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_33_ @Inject constructor(
    private val repository: GenRepositoryImpl_33_
) : GenUseCase_33_<String, List<GenModel_33_>> {
    override suspend fun invoke(params: String): List<GenModel_33_> = repository.search(params)
}

abstract class GenMapper_33_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_33_ : GenMapper_33_<GenModel_33_, String>() {
    override fun map(input: GenModel_33_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_33_ : GenMapper_33_<String, GenModel_33_>() {
    override fun map(input: String): GenModel_33_ {
        val parts = input.split(":")
        return GenModel_33_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_33_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_33_,
    private val saveUseCase: GenSaveUseCase_33_,
    private val deleteUseCase: GenDeleteUseCase_33_,
    private val searchUseCase: GenSearchUseCase_33_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_33_>(GenState_33_.Idle)
    val state: StateFlow<GenState_33_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_33_) {
        when (event) {
            is GenEvent_33_.Load -> loadAll()
            is GenEvent_33_.Update -> save(event.model)
            is GenEvent_33_.Delete -> delete(event.id)
            is GenEvent_33_.Refresh -> loadAll()
            is GenEvent_33_.Search -> search(event.query)
            is GenEvent_33_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_33_.Loading; _state.value = GenState_33_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_33_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_33_.Success(searchUseCase(query)) } }
}
