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

data class GenModel_43_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_43_ {
    data class Load(val id: Long) : GenEvent_43_()
    data class Update(val model: GenModel_43_) : GenEvent_43_()
    data class Delete(val id: Long) : GenEvent_43_()
    data object Refresh : GenEvent_43_()
    data class Search(val query: String) : GenEvent_43_()
    data class Filter(val predicate: String) : GenEvent_43_()
}

sealed class GenState_43_ {
    data object Idle : GenState_43_()
    data object Loading : GenState_43_()
    data class Success(val items: List<GenModel_43_>) : GenState_43_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_43_()
    data class Partial(val items: List<GenModel_43_>, val hasMore: Boolean) : GenState_43_()
}

interface GenRepository_43_ {
    suspend fun getAll(): List<GenModel_43_>
    suspend fun getById(id: Long): GenModel_43_?
    suspend fun save(model: GenModel_43_): GenModel_43_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_43_>
}

@Singleton
class GenRepositoryImpl_43_ @Inject constructor() : GenRepository_43_ {
    private val store = mutableMapOf<Long, GenModel_43_>()
    override suspend fun getAll(): List<GenModel_43_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_43_? = store[id]
    override suspend fun save(model: GenModel_43_): GenModel_43_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_43_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_43_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_43_ @Inject constructor(
    private val repository: GenRepositoryImpl_43_
) : GenUseCase_43_<Unit, List<GenModel_43_>> {
    override suspend fun invoke(params: Unit): List<GenModel_43_> = repository.getAll()
}

class GenSaveUseCase_43_ @Inject constructor(
    private val repository: GenRepositoryImpl_43_
) : GenUseCase_43_<GenModel_43_, GenModel_43_> {
    override suspend fun invoke(params: GenModel_43_): GenModel_43_ = repository.save(params)
}

class GenDeleteUseCase_43_ @Inject constructor(
    private val repository: GenRepositoryImpl_43_
) : GenUseCase_43_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_43_ @Inject constructor(
    private val repository: GenRepositoryImpl_43_
) : GenUseCase_43_<String, List<GenModel_43_>> {
    override suspend fun invoke(params: String): List<GenModel_43_> = repository.search(params)
}

abstract class GenMapper_43_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_43_ : GenMapper_43_<GenModel_43_, String>() {
    override fun map(input: GenModel_43_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_43_ : GenMapper_43_<String, GenModel_43_>() {
    override fun map(input: String): GenModel_43_ {
        val parts = input.split(":")
        return GenModel_43_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_43_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_43_,
    private val saveUseCase: GenSaveUseCase_43_,
    private val deleteUseCase: GenDeleteUseCase_43_,
    private val searchUseCase: GenSearchUseCase_43_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_43_>(GenState_43_.Idle)
    val state: StateFlow<GenState_43_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_43_) {
        when (event) {
            is GenEvent_43_.Load -> loadAll()
            is GenEvent_43_.Update -> save(event.model)
            is GenEvent_43_.Delete -> delete(event.id)
            is GenEvent_43_.Refresh -> loadAll()
            is GenEvent_43_.Search -> search(event.query)
            is GenEvent_43_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_43_.Loading; _state.value = GenState_43_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_43_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_43_.Success(searchUseCase(query)) } }
}
