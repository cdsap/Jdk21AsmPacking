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

data class GenModel_665_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_665_ {
    data class Load(val id: Long) : GenEvent_665_()
    data class Update(val model: GenModel_665_) : GenEvent_665_()
    data class Delete(val id: Long) : GenEvent_665_()
    data object Refresh : GenEvent_665_()
    data class Search(val query: String) : GenEvent_665_()
    data class Filter(val predicate: String) : GenEvent_665_()
}

sealed class GenState_665_ {
    data object Idle : GenState_665_()
    data object Loading : GenState_665_()
    data class Success(val items: List<GenModel_665_>) : GenState_665_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_665_()
    data class Partial(val items: List<GenModel_665_>, val hasMore: Boolean) : GenState_665_()
}

interface GenRepository_665_ {
    suspend fun getAll(): List<GenModel_665_>
    suspend fun getById(id: Long): GenModel_665_?
    suspend fun save(model: GenModel_665_): GenModel_665_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_665_>
}

@Singleton
class GenRepositoryImpl_665_ @Inject constructor() : GenRepository_665_ {
    private val store = mutableMapOf<Long, GenModel_665_>()
    override suspend fun getAll(): List<GenModel_665_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_665_? = store[id]
    override suspend fun save(model: GenModel_665_): GenModel_665_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_665_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_665_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_665_ @Inject constructor(
    private val repository: GenRepositoryImpl_665_
) : GenUseCase_665_<Unit, List<GenModel_665_>> {
    override suspend fun invoke(params: Unit): List<GenModel_665_> = repository.getAll()
}

class GenSaveUseCase_665_ @Inject constructor(
    private val repository: GenRepositoryImpl_665_
) : GenUseCase_665_<GenModel_665_, GenModel_665_> {
    override suspend fun invoke(params: GenModel_665_): GenModel_665_ = repository.save(params)
}

class GenDeleteUseCase_665_ @Inject constructor(
    private val repository: GenRepositoryImpl_665_
) : GenUseCase_665_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_665_ @Inject constructor(
    private val repository: GenRepositoryImpl_665_
) : GenUseCase_665_<String, List<GenModel_665_>> {
    override suspend fun invoke(params: String): List<GenModel_665_> = repository.search(params)
}

abstract class GenMapper_665_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_665_ : GenMapper_665_<GenModel_665_, String>() {
    override fun map(input: GenModel_665_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_665_ : GenMapper_665_<String, GenModel_665_>() {
    override fun map(input: String): GenModel_665_ {
        val parts = input.split(":")
        return GenModel_665_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_665_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_665_,
    private val saveUseCase: GenSaveUseCase_665_,
    private val deleteUseCase: GenDeleteUseCase_665_,
    private val searchUseCase: GenSearchUseCase_665_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_665_>(GenState_665_.Idle)
    val state: StateFlow<GenState_665_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_665_) {
        when (event) {
            is GenEvent_665_.Load -> loadAll()
            is GenEvent_665_.Update -> save(event.model)
            is GenEvent_665_.Delete -> delete(event.id)
            is GenEvent_665_.Refresh -> loadAll()
            is GenEvent_665_.Search -> search(event.query)
            is GenEvent_665_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_665_.Loading; _state.value = GenState_665_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_665_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_665_.Success(searchUseCase(query)) } }
}
