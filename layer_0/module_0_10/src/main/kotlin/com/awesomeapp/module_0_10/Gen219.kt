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

data class GenModel_219_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_219_ {
    data class Load(val id: Long) : GenEvent_219_()
    data class Update(val model: GenModel_219_) : GenEvent_219_()
    data class Delete(val id: Long) : GenEvent_219_()
    data object Refresh : GenEvent_219_()
    data class Search(val query: String) : GenEvent_219_()
    data class Filter(val predicate: String) : GenEvent_219_()
}

sealed class GenState_219_ {
    data object Idle : GenState_219_()
    data object Loading : GenState_219_()
    data class Success(val items: List<GenModel_219_>) : GenState_219_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_219_()
    data class Partial(val items: List<GenModel_219_>, val hasMore: Boolean) : GenState_219_()
}

interface GenRepository_219_ {
    suspend fun getAll(): List<GenModel_219_>
    suspend fun getById(id: Long): GenModel_219_?
    suspend fun save(model: GenModel_219_): GenModel_219_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_219_>
}

@Singleton
class GenRepositoryImpl_219_ @Inject constructor() : GenRepository_219_ {
    private val store = mutableMapOf<Long, GenModel_219_>()
    override suspend fun getAll(): List<GenModel_219_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_219_? = store[id]
    override suspend fun save(model: GenModel_219_): GenModel_219_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_219_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_219_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_219_ @Inject constructor(
    private val repository: GenRepositoryImpl_219_
) : GenUseCase_219_<Unit, List<GenModel_219_>> {
    override suspend fun invoke(params: Unit): List<GenModel_219_> = repository.getAll()
}

class GenSaveUseCase_219_ @Inject constructor(
    private val repository: GenRepositoryImpl_219_
) : GenUseCase_219_<GenModel_219_, GenModel_219_> {
    override suspend fun invoke(params: GenModel_219_): GenModel_219_ = repository.save(params)
}

class GenDeleteUseCase_219_ @Inject constructor(
    private val repository: GenRepositoryImpl_219_
) : GenUseCase_219_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_219_ @Inject constructor(
    private val repository: GenRepositoryImpl_219_
) : GenUseCase_219_<String, List<GenModel_219_>> {
    override suspend fun invoke(params: String): List<GenModel_219_> = repository.search(params)
}

abstract class GenMapper_219_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_219_ : GenMapper_219_<GenModel_219_, String>() {
    override fun map(input: GenModel_219_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_219_ : GenMapper_219_<String, GenModel_219_>() {
    override fun map(input: String): GenModel_219_ {
        val parts = input.split(":")
        return GenModel_219_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_219_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_219_,
    private val saveUseCase: GenSaveUseCase_219_,
    private val deleteUseCase: GenDeleteUseCase_219_,
    private val searchUseCase: GenSearchUseCase_219_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_219_>(GenState_219_.Idle)
    val state: StateFlow<GenState_219_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_219_) {
        when (event) {
            is GenEvent_219_.Load -> loadAll()
            is GenEvent_219_.Update -> save(event.model)
            is GenEvent_219_.Delete -> delete(event.id)
            is GenEvent_219_.Refresh -> loadAll()
            is GenEvent_219_.Search -> search(event.query)
            is GenEvent_219_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_219_.Loading; _state.value = GenState_219_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_219_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_219_.Success(searchUseCase(query)) } }
}
