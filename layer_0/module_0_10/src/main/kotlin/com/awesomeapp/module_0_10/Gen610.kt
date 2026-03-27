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

data class GenModel_610_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_610_ {
    data class Load(val id: Long) : GenEvent_610_()
    data class Update(val model: GenModel_610_) : GenEvent_610_()
    data class Delete(val id: Long) : GenEvent_610_()
    data object Refresh : GenEvent_610_()
    data class Search(val query: String) : GenEvent_610_()
    data class Filter(val predicate: String) : GenEvent_610_()
}

sealed class GenState_610_ {
    data object Idle : GenState_610_()
    data object Loading : GenState_610_()
    data class Success(val items: List<GenModel_610_>) : GenState_610_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_610_()
    data class Partial(val items: List<GenModel_610_>, val hasMore: Boolean) : GenState_610_()
}

interface GenRepository_610_ {
    suspend fun getAll(): List<GenModel_610_>
    suspend fun getById(id: Long): GenModel_610_?
    suspend fun save(model: GenModel_610_): GenModel_610_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_610_>
}

@Singleton
class GenRepositoryImpl_610_ @Inject constructor() : GenRepository_610_ {
    private val store = mutableMapOf<Long, GenModel_610_>()
    override suspend fun getAll(): List<GenModel_610_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_610_? = store[id]
    override suspend fun save(model: GenModel_610_): GenModel_610_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_610_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_610_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_610_ @Inject constructor(
    private val repository: GenRepositoryImpl_610_
) : GenUseCase_610_<Unit, List<GenModel_610_>> {
    override suspend fun invoke(params: Unit): List<GenModel_610_> = repository.getAll()
}

class GenSaveUseCase_610_ @Inject constructor(
    private val repository: GenRepositoryImpl_610_
) : GenUseCase_610_<GenModel_610_, GenModel_610_> {
    override suspend fun invoke(params: GenModel_610_): GenModel_610_ = repository.save(params)
}

class GenDeleteUseCase_610_ @Inject constructor(
    private val repository: GenRepositoryImpl_610_
) : GenUseCase_610_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_610_ @Inject constructor(
    private val repository: GenRepositoryImpl_610_
) : GenUseCase_610_<String, List<GenModel_610_>> {
    override suspend fun invoke(params: String): List<GenModel_610_> = repository.search(params)
}

abstract class GenMapper_610_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_610_ : GenMapper_610_<GenModel_610_, String>() {
    override fun map(input: GenModel_610_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_610_ : GenMapper_610_<String, GenModel_610_>() {
    override fun map(input: String): GenModel_610_ {
        val parts = input.split(":")
        return GenModel_610_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_610_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_610_,
    private val saveUseCase: GenSaveUseCase_610_,
    private val deleteUseCase: GenDeleteUseCase_610_,
    private val searchUseCase: GenSearchUseCase_610_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_610_>(GenState_610_.Idle)
    val state: StateFlow<GenState_610_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_610_) {
        when (event) {
            is GenEvent_610_.Load -> loadAll()
            is GenEvent_610_.Update -> save(event.model)
            is GenEvent_610_.Delete -> delete(event.id)
            is GenEvent_610_.Refresh -> loadAll()
            is GenEvent_610_.Search -> search(event.query)
            is GenEvent_610_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_610_.Loading; _state.value = GenState_610_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_610_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_610_.Success(searchUseCase(query)) } }
}
