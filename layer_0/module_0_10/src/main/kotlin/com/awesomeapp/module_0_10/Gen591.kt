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

data class GenModel_591_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_591_ {
    data class Load(val id: Long) : GenEvent_591_()
    data class Update(val model: GenModel_591_) : GenEvent_591_()
    data class Delete(val id: Long) : GenEvent_591_()
    data object Refresh : GenEvent_591_()
    data class Search(val query: String) : GenEvent_591_()
    data class Filter(val predicate: String) : GenEvent_591_()
}

sealed class GenState_591_ {
    data object Idle : GenState_591_()
    data object Loading : GenState_591_()
    data class Success(val items: List<GenModel_591_>) : GenState_591_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_591_()
    data class Partial(val items: List<GenModel_591_>, val hasMore: Boolean) : GenState_591_()
}

interface GenRepository_591_ {
    suspend fun getAll(): List<GenModel_591_>
    suspend fun getById(id: Long): GenModel_591_?
    suspend fun save(model: GenModel_591_): GenModel_591_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_591_>
}

@Singleton
class GenRepositoryImpl_591_ @Inject constructor() : GenRepository_591_ {
    private val store = mutableMapOf<Long, GenModel_591_>()
    override suspend fun getAll(): List<GenModel_591_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_591_? = store[id]
    override suspend fun save(model: GenModel_591_): GenModel_591_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_591_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_591_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_591_ @Inject constructor(
    private val repository: GenRepositoryImpl_591_
) : GenUseCase_591_<Unit, List<GenModel_591_>> {
    override suspend fun invoke(params: Unit): List<GenModel_591_> = repository.getAll()
}

class GenSaveUseCase_591_ @Inject constructor(
    private val repository: GenRepositoryImpl_591_
) : GenUseCase_591_<GenModel_591_, GenModel_591_> {
    override suspend fun invoke(params: GenModel_591_): GenModel_591_ = repository.save(params)
}

class GenDeleteUseCase_591_ @Inject constructor(
    private val repository: GenRepositoryImpl_591_
) : GenUseCase_591_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_591_ @Inject constructor(
    private val repository: GenRepositoryImpl_591_
) : GenUseCase_591_<String, List<GenModel_591_>> {
    override suspend fun invoke(params: String): List<GenModel_591_> = repository.search(params)
}

abstract class GenMapper_591_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_591_ : GenMapper_591_<GenModel_591_, String>() {
    override fun map(input: GenModel_591_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_591_ : GenMapper_591_<String, GenModel_591_>() {
    override fun map(input: String): GenModel_591_ {
        val parts = input.split(":")
        return GenModel_591_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_591_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_591_,
    private val saveUseCase: GenSaveUseCase_591_,
    private val deleteUseCase: GenDeleteUseCase_591_,
    private val searchUseCase: GenSearchUseCase_591_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_591_>(GenState_591_.Idle)
    val state: StateFlow<GenState_591_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_591_) {
        when (event) {
            is GenEvent_591_.Load -> loadAll()
            is GenEvent_591_.Update -> save(event.model)
            is GenEvent_591_.Delete -> delete(event.id)
            is GenEvent_591_.Refresh -> loadAll()
            is GenEvent_591_.Search -> search(event.query)
            is GenEvent_591_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_591_.Loading; _state.value = GenState_591_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_591_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_591_.Success(searchUseCase(query)) } }
}
