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

data class GenModel_3438_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_3438_ {
    data class Load(val id: Long) : GenEvent_3438_()
    data class Update(val model: GenModel_3438_) : GenEvent_3438_()
    data class Delete(val id: Long) : GenEvent_3438_()
    data object Refresh : GenEvent_3438_()
    data class Search(val query: String) : GenEvent_3438_()
    data class Filter(val predicate: String) : GenEvent_3438_()
}

sealed class GenState_3438_ {
    data object Idle : GenState_3438_()
    data object Loading : GenState_3438_()
    data class Success(val items: List<GenModel_3438_>) : GenState_3438_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_3438_()
    data class Partial(val items: List<GenModel_3438_>, val hasMore: Boolean) : GenState_3438_()
}

interface GenRepository_3438_ {
    suspend fun getAll(): List<GenModel_3438_>
    suspend fun getById(id: Long): GenModel_3438_?
    suspend fun save(model: GenModel_3438_): GenModel_3438_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_3438_>
}

@Singleton
class GenRepositoryImpl_3438_ @Inject constructor() : GenRepository_3438_ {
    private val store = mutableMapOf<Long, GenModel_3438_>()
    override suspend fun getAll(): List<GenModel_3438_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_3438_? = store[id]
    override suspend fun save(model: GenModel_3438_): GenModel_3438_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_3438_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_3438_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_3438_ @Inject constructor(
    private val repository: GenRepositoryImpl_3438_
) : GenUseCase_3438_<Unit, List<GenModel_3438_>> {
    override suspend fun invoke(params: Unit): List<GenModel_3438_> = repository.getAll()
}

class GenSaveUseCase_3438_ @Inject constructor(
    private val repository: GenRepositoryImpl_3438_
) : GenUseCase_3438_<GenModel_3438_, GenModel_3438_> {
    override suspend fun invoke(params: GenModel_3438_): GenModel_3438_ = repository.save(params)
}

class GenDeleteUseCase_3438_ @Inject constructor(
    private val repository: GenRepositoryImpl_3438_
) : GenUseCase_3438_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_3438_ @Inject constructor(
    private val repository: GenRepositoryImpl_3438_
) : GenUseCase_3438_<String, List<GenModel_3438_>> {
    override suspend fun invoke(params: String): List<GenModel_3438_> = repository.search(params)
}

abstract class GenMapper_3438_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_3438_ : GenMapper_3438_<GenModel_3438_, String>() {
    override fun map(input: GenModel_3438_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_3438_ : GenMapper_3438_<String, GenModel_3438_>() {
    override fun map(input: String): GenModel_3438_ {
        val parts = input.split(":")
        return GenModel_3438_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_3438_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_3438_,
    private val saveUseCase: GenSaveUseCase_3438_,
    private val deleteUseCase: GenDeleteUseCase_3438_,
    private val searchUseCase: GenSearchUseCase_3438_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_3438_>(GenState_3438_.Idle)
    val state: StateFlow<GenState_3438_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_3438_) {
        when (event) {
            is GenEvent_3438_.Load -> loadAll()
            is GenEvent_3438_.Update -> save(event.model)
            is GenEvent_3438_.Delete -> delete(event.id)
            is GenEvent_3438_.Refresh -> loadAll()
            is GenEvent_3438_.Search -> search(event.query)
            is GenEvent_3438_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_3438_.Loading; _state.value = GenState_3438_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_3438_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_3438_.Success(searchUseCase(query)) } }
}
