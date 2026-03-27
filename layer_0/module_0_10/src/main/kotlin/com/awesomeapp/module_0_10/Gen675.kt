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

data class GenModel_675_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_675_ {
    data class Load(val id: Long) : GenEvent_675_()
    data class Update(val model: GenModel_675_) : GenEvent_675_()
    data class Delete(val id: Long) : GenEvent_675_()
    data object Refresh : GenEvent_675_()
    data class Search(val query: String) : GenEvent_675_()
    data class Filter(val predicate: String) : GenEvent_675_()
}

sealed class GenState_675_ {
    data object Idle : GenState_675_()
    data object Loading : GenState_675_()
    data class Success(val items: List<GenModel_675_>) : GenState_675_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_675_()
    data class Partial(val items: List<GenModel_675_>, val hasMore: Boolean) : GenState_675_()
}

interface GenRepository_675_ {
    suspend fun getAll(): List<GenModel_675_>
    suspend fun getById(id: Long): GenModel_675_?
    suspend fun save(model: GenModel_675_): GenModel_675_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_675_>
}

@Singleton
class GenRepositoryImpl_675_ @Inject constructor() : GenRepository_675_ {
    private val store = mutableMapOf<Long, GenModel_675_>()
    override suspend fun getAll(): List<GenModel_675_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_675_? = store[id]
    override suspend fun save(model: GenModel_675_): GenModel_675_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_675_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_675_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_675_ @Inject constructor(
    private val repository: GenRepositoryImpl_675_
) : GenUseCase_675_<Unit, List<GenModel_675_>> {
    override suspend fun invoke(params: Unit): List<GenModel_675_> = repository.getAll()
}

class GenSaveUseCase_675_ @Inject constructor(
    private val repository: GenRepositoryImpl_675_
) : GenUseCase_675_<GenModel_675_, GenModel_675_> {
    override suspend fun invoke(params: GenModel_675_): GenModel_675_ = repository.save(params)
}

class GenDeleteUseCase_675_ @Inject constructor(
    private val repository: GenRepositoryImpl_675_
) : GenUseCase_675_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_675_ @Inject constructor(
    private val repository: GenRepositoryImpl_675_
) : GenUseCase_675_<String, List<GenModel_675_>> {
    override suspend fun invoke(params: String): List<GenModel_675_> = repository.search(params)
}

abstract class GenMapper_675_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_675_ : GenMapper_675_<GenModel_675_, String>() {
    override fun map(input: GenModel_675_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_675_ : GenMapper_675_<String, GenModel_675_>() {
    override fun map(input: String): GenModel_675_ {
        val parts = input.split(":")
        return GenModel_675_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_675_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_675_,
    private val saveUseCase: GenSaveUseCase_675_,
    private val deleteUseCase: GenDeleteUseCase_675_,
    private val searchUseCase: GenSearchUseCase_675_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_675_>(GenState_675_.Idle)
    val state: StateFlow<GenState_675_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_675_) {
        when (event) {
            is GenEvent_675_.Load -> loadAll()
            is GenEvent_675_.Update -> save(event.model)
            is GenEvent_675_.Delete -> delete(event.id)
            is GenEvent_675_.Refresh -> loadAll()
            is GenEvent_675_.Search -> search(event.query)
            is GenEvent_675_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_675_.Loading; _state.value = GenState_675_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_675_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_675_.Success(searchUseCase(query)) } }
}
