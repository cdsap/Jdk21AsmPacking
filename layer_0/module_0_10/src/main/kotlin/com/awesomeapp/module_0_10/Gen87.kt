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

data class GenModel_87_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_87_ {
    data class Load(val id: Long) : GenEvent_87_()
    data class Update(val model: GenModel_87_) : GenEvent_87_()
    data class Delete(val id: Long) : GenEvent_87_()
    data object Refresh : GenEvent_87_()
    data class Search(val query: String) : GenEvent_87_()
    data class Filter(val predicate: String) : GenEvent_87_()
}

sealed class GenState_87_ {
    data object Idle : GenState_87_()
    data object Loading : GenState_87_()
    data class Success(val items: List<GenModel_87_>) : GenState_87_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_87_()
    data class Partial(val items: List<GenModel_87_>, val hasMore: Boolean) : GenState_87_()
}

interface GenRepository_87_ {
    suspend fun getAll(): List<GenModel_87_>
    suspend fun getById(id: Long): GenModel_87_?
    suspend fun save(model: GenModel_87_): GenModel_87_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_87_>
}

@Singleton
class GenRepositoryImpl_87_ @Inject constructor() : GenRepository_87_ {
    private val store = mutableMapOf<Long, GenModel_87_>()
    override suspend fun getAll(): List<GenModel_87_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_87_? = store[id]
    override suspend fun save(model: GenModel_87_): GenModel_87_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_87_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_87_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_87_ @Inject constructor(
    private val repository: GenRepositoryImpl_87_
) : GenUseCase_87_<Unit, List<GenModel_87_>> {
    override suspend fun invoke(params: Unit): List<GenModel_87_> = repository.getAll()
}

class GenSaveUseCase_87_ @Inject constructor(
    private val repository: GenRepositoryImpl_87_
) : GenUseCase_87_<GenModel_87_, GenModel_87_> {
    override suspend fun invoke(params: GenModel_87_): GenModel_87_ = repository.save(params)
}

class GenDeleteUseCase_87_ @Inject constructor(
    private val repository: GenRepositoryImpl_87_
) : GenUseCase_87_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_87_ @Inject constructor(
    private val repository: GenRepositoryImpl_87_
) : GenUseCase_87_<String, List<GenModel_87_>> {
    override suspend fun invoke(params: String): List<GenModel_87_> = repository.search(params)
}

abstract class GenMapper_87_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_87_ : GenMapper_87_<GenModel_87_, String>() {
    override fun map(input: GenModel_87_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_87_ : GenMapper_87_<String, GenModel_87_>() {
    override fun map(input: String): GenModel_87_ {
        val parts = input.split(":")
        return GenModel_87_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_87_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_87_,
    private val saveUseCase: GenSaveUseCase_87_,
    private val deleteUseCase: GenDeleteUseCase_87_,
    private val searchUseCase: GenSearchUseCase_87_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_87_>(GenState_87_.Idle)
    val state: StateFlow<GenState_87_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_87_) {
        when (event) {
            is GenEvent_87_.Load -> loadAll()
            is GenEvent_87_.Update -> save(event.model)
            is GenEvent_87_.Delete -> delete(event.id)
            is GenEvent_87_.Refresh -> loadAll()
            is GenEvent_87_.Search -> search(event.query)
            is GenEvent_87_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_87_.Loading; _state.value = GenState_87_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_87_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_87_.Success(searchUseCase(query)) } }
}
