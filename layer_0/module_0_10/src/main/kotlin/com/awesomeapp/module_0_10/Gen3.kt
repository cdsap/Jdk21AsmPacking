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

data class GenModel_3_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3_ {
    data class Load(val id: Long) : GenEvent_3_()
    data class Update(val model: GenModel_3_) : GenEvent_3_()
    data class Delete(val id: Long) : GenEvent_3_()
    data object Refresh : GenEvent_3_()
    data class Search(val query: String) : GenEvent_3_()
    data class Filter(val predicate: String) : GenEvent_3_()
}

sealed class GenState_3_ {
    data object Idle : GenState_3_()
    data object Loading : GenState_3_()
    data class Success(val items: List<GenModel_3_>) : GenState_3_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3_()
    data class Partial(val items: List<GenModel_3_>, val hasMore: Boolean) : GenState_3_()
}

interface GenRepository_3_ {
    suspend fun getAll(): List<GenModel_3_>
    suspend fun getById(id: Long): GenModel_3_?
    suspend fun save(model: GenModel_3_): GenModel_3_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3_>
}

@Singleton
class GenRepositoryImpl_3_ @Inject constructor() : GenRepository_3_ {
    private val store = mutableMapOf<Long, GenModel_3_>()
    override suspend fun getAll(): List<GenModel_3_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3_? = store[id]
    override suspend fun save(model: GenModel_3_): GenModel_3_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3_ @Inject constructor(
    private val repository: GenRepositoryImpl_3_
) : GenUseCase_3_<Unit, List<GenModel_3_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3_> = repository.getAll()
}

class GenSaveUseCase_3_ @Inject constructor(
    private val repository: GenRepositoryImpl_3_
) : GenUseCase_3_<GenModel_3_, GenModel_3_> {
    override suspend fun invoke(params: GenModel_3_): GenModel_3_ = repository.save(params)
}

class GenDeleteUseCase_3_ @Inject constructor(
    private val repository: GenRepositoryImpl_3_
) : GenUseCase_3_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3_ @Inject constructor(
    private val repository: GenRepositoryImpl_3_
) : GenUseCase_3_<String, List<GenModel_3_>> {
    override suspend fun invoke(params: String): List<GenModel_3_> = repository.search(params)
}

abstract class GenMapper_3_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3_ : GenMapper_3_<GenModel_3_, String>() {
    override fun map(input: GenModel_3_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3_ : GenMapper_3_<String, GenModel_3_>() {
    override fun map(input: String): GenModel_3_ {
        val parts = input.split(":")
        return GenModel_3_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3_,
    private val saveUseCase: GenSaveUseCase_3_,
    private val deleteUseCase: GenDeleteUseCase_3_,
    private val searchUseCase: GenSearchUseCase_3_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3_>(GenState_3_.Idle)
    val state: StateFlow<GenState_3_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3_) {
        when (event) {
            is GenEvent_3_.Load -> loadAll()
            is GenEvent_3_.Update -> save(event.model)
            is GenEvent_3_.Delete -> delete(event.id)
            is GenEvent_3_.Refresh -> loadAll()
            is GenEvent_3_.Search -> search(event.query)
            is GenEvent_3_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3_.Loading; _state.value = GenState_3_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3_.Success(searchUseCase(query)) } }
}
