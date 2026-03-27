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

data class GenModel_5_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_5_ {
    data class Load(val id: Long) : GenEvent_5_()
    data class Update(val model: GenModel_5_) : GenEvent_5_()
    data class Delete(val id: Long) : GenEvent_5_()
    data object Refresh : GenEvent_5_()
    data class Search(val query: String) : GenEvent_5_()
    data class Filter(val predicate: String) : GenEvent_5_()
}

sealed class GenState_5_ {
    data object Idle : GenState_5_()
    data object Loading : GenState_5_()
    data class Success(val items: List<GenModel_5_>) : GenState_5_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_5_()
    data class Partial(val items: List<GenModel_5_>, val hasMore: Boolean) : GenState_5_()
}

interface GenRepository_5_ {
    suspend fun getAll(): List<GenModel_5_>
    suspend fun getById(id: Long): GenModel_5_?
    suspend fun save(model: GenModel_5_): GenModel_5_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_5_>
}

@Singleton
class GenRepositoryImpl_5_ @Inject constructor() : GenRepository_5_ {
    private val store = mutableMapOf<Long, GenModel_5_>()
    override suspend fun getAll(): List<GenModel_5_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_5_? = store[id]
    override suspend fun save(model: GenModel_5_): GenModel_5_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_5_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_5_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_5_ @Inject constructor(
    private val repository: GenRepositoryImpl_5_
) : GenUseCase_5_<Unit, List<GenModel_5_>> {
    override suspend fun invoke(params: Unit): List<GenModel_5_> = repository.getAll()
}

class GenSaveUseCase_5_ @Inject constructor(
    private val repository: GenRepositoryImpl_5_
) : GenUseCase_5_<GenModel_5_, GenModel_5_> {
    override suspend fun invoke(params: GenModel_5_): GenModel_5_ = repository.save(params)
}

class GenDeleteUseCase_5_ @Inject constructor(
    private val repository: GenRepositoryImpl_5_
) : GenUseCase_5_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_5_ @Inject constructor(
    private val repository: GenRepositoryImpl_5_
) : GenUseCase_5_<String, List<GenModel_5_>> {
    override suspend fun invoke(params: String): List<GenModel_5_> = repository.search(params)
}

abstract class GenMapper_5_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_5_ : GenMapper_5_<GenModel_5_, String>() {
    override fun map(input: GenModel_5_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_5_ : GenMapper_5_<String, GenModel_5_>() {
    override fun map(input: String): GenModel_5_ {
        val parts = input.split(":")
        return GenModel_5_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_5_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_5_,
    private val saveUseCase: GenSaveUseCase_5_,
    private val deleteUseCase: GenDeleteUseCase_5_,
    private val searchUseCase: GenSearchUseCase_5_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_5_>(GenState_5_.Idle)
    val state: StateFlow<GenState_5_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_5_) {
        when (event) {
            is GenEvent_5_.Load -> loadAll()
            is GenEvent_5_.Update -> save(event.model)
            is GenEvent_5_.Delete -> delete(event.id)
            is GenEvent_5_.Refresh -> loadAll()
            is GenEvent_5_.Search -> search(event.query)
            is GenEvent_5_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_5_.Loading; _state.value = GenState_5_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_5_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_5_.Success(searchUseCase(query)) } }
}
