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

data class GenModel_527_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_527_ {
    data class Load(val id: Long) : GenEvent_527_()
    data class Update(val model: GenModel_527_) : GenEvent_527_()
    data class Delete(val id: Long) : GenEvent_527_()
    data object Refresh : GenEvent_527_()
    data class Search(val query: String) : GenEvent_527_()
    data class Filter(val predicate: String) : GenEvent_527_()
}

sealed class GenState_527_ {
    data object Idle : GenState_527_()
    data object Loading : GenState_527_()
    data class Success(val items: List<GenModel_527_>) : GenState_527_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_527_()
    data class Partial(val items: List<GenModel_527_>, val hasMore: Boolean) : GenState_527_()
}

interface GenRepository_527_ {
    suspend fun getAll(): List<GenModel_527_>
    suspend fun getById(id: Long): GenModel_527_?
    suspend fun save(model: GenModel_527_): GenModel_527_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_527_>
}

@Singleton
class GenRepositoryImpl_527_ @Inject constructor() : GenRepository_527_ {
    private val store = mutableMapOf<Long, GenModel_527_>()
    override suspend fun getAll(): List<GenModel_527_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_527_? = store[id]
    override suspend fun save(model: GenModel_527_): GenModel_527_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_527_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_527_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_527_ @Inject constructor(
    private val repository: GenRepositoryImpl_527_
) : GenUseCase_527_<Unit, List<GenModel_527_>> {
    override suspend fun invoke(params: Unit): List<GenModel_527_> = repository.getAll()
}

class GenSaveUseCase_527_ @Inject constructor(
    private val repository: GenRepositoryImpl_527_
) : GenUseCase_527_<GenModel_527_, GenModel_527_> {
    override suspend fun invoke(params: GenModel_527_): GenModel_527_ = repository.save(params)
}

class GenDeleteUseCase_527_ @Inject constructor(
    private val repository: GenRepositoryImpl_527_
) : GenUseCase_527_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_527_ @Inject constructor(
    private val repository: GenRepositoryImpl_527_
) : GenUseCase_527_<String, List<GenModel_527_>> {
    override suspend fun invoke(params: String): List<GenModel_527_> = repository.search(params)
}

abstract class GenMapper_527_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_527_ : GenMapper_527_<GenModel_527_, String>() {
    override fun map(input: GenModel_527_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_527_ : GenMapper_527_<String, GenModel_527_>() {
    override fun map(input: String): GenModel_527_ {
        val parts = input.split(":")
        return GenModel_527_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_527_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_527_,
    private val saveUseCase: GenSaveUseCase_527_,
    private val deleteUseCase: GenDeleteUseCase_527_,
    private val searchUseCase: GenSearchUseCase_527_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_527_>(GenState_527_.Idle)
    val state: StateFlow<GenState_527_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_527_) {
        when (event) {
            is GenEvent_527_.Load -> loadAll()
            is GenEvent_527_.Update -> save(event.model)
            is GenEvent_527_.Delete -> delete(event.id)
            is GenEvent_527_.Refresh -> loadAll()
            is GenEvent_527_.Search -> search(event.query)
            is GenEvent_527_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_527_.Loading; _state.value = GenState_527_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_527_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_527_.Success(searchUseCase(query)) } }
}
