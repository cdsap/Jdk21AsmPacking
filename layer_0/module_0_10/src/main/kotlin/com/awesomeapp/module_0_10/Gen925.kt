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

data class GenModel_925_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_925_ {
    data class Load(val id: Long) : GenEvent_925_()
    data class Update(val model: GenModel_925_) : GenEvent_925_()
    data class Delete(val id: Long) : GenEvent_925_()
    data object Refresh : GenEvent_925_()
    data class Search(val query: String) : GenEvent_925_()
    data class Filter(val predicate: String) : GenEvent_925_()
}

sealed class GenState_925_ {
    data object Idle : GenState_925_()
    data object Loading : GenState_925_()
    data class Success(val items: List<GenModel_925_>) : GenState_925_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_925_()
    data class Partial(val items: List<GenModel_925_>, val hasMore: Boolean) : GenState_925_()
}

interface GenRepository_925_ {
    suspend fun getAll(): List<GenModel_925_>
    suspend fun getById(id: Long): GenModel_925_?
    suspend fun save(model: GenModel_925_): GenModel_925_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_925_>
}

@Singleton
class GenRepositoryImpl_925_ @Inject constructor() : GenRepository_925_ {
    private val store = mutableMapOf<Long, GenModel_925_>()
    override suspend fun getAll(): List<GenModel_925_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_925_? = store[id]
    override suspend fun save(model: GenModel_925_): GenModel_925_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_925_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_925_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_925_ @Inject constructor(
    private val repository: GenRepositoryImpl_925_
) : GenUseCase_925_<Unit, List<GenModel_925_>> {
    override suspend fun invoke(params: Unit): List<GenModel_925_> = repository.getAll()
}

class GenSaveUseCase_925_ @Inject constructor(
    private val repository: GenRepositoryImpl_925_
) : GenUseCase_925_<GenModel_925_, GenModel_925_> {
    override suspend fun invoke(params: GenModel_925_): GenModel_925_ = repository.save(params)
}

class GenDeleteUseCase_925_ @Inject constructor(
    private val repository: GenRepositoryImpl_925_
) : GenUseCase_925_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_925_ @Inject constructor(
    private val repository: GenRepositoryImpl_925_
) : GenUseCase_925_<String, List<GenModel_925_>> {
    override suspend fun invoke(params: String): List<GenModel_925_> = repository.search(params)
}

abstract class GenMapper_925_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_925_ : GenMapper_925_<GenModel_925_, String>() {
    override fun map(input: GenModel_925_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_925_ : GenMapper_925_<String, GenModel_925_>() {
    override fun map(input: String): GenModel_925_ {
        val parts = input.split(":")
        return GenModel_925_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_925_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_925_,
    private val saveUseCase: GenSaveUseCase_925_,
    private val deleteUseCase: GenDeleteUseCase_925_,
    private val searchUseCase: GenSearchUseCase_925_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_925_>(GenState_925_.Idle)
    val state: StateFlow<GenState_925_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_925_) {
        when (event) {
            is GenEvent_925_.Load -> loadAll()
            is GenEvent_925_.Update -> save(event.model)
            is GenEvent_925_.Delete -> delete(event.id)
            is GenEvent_925_.Refresh -> loadAll()
            is GenEvent_925_.Search -> search(event.query)
            is GenEvent_925_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_925_.Loading; _state.value = GenState_925_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_925_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_925_.Success(searchUseCase(query)) } }
}
