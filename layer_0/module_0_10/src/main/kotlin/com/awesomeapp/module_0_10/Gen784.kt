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

data class GenModel_784_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_784_ {
    data class Load(val id: Long) : GenEvent_784_()
    data class Update(val model: GenModel_784_) : GenEvent_784_()
    data class Delete(val id: Long) : GenEvent_784_()
    data object Refresh : GenEvent_784_()
    data class Search(val query: String) : GenEvent_784_()
    data class Filter(val predicate: String) : GenEvent_784_()
}

sealed class GenState_784_ {
    data object Idle : GenState_784_()
    data object Loading : GenState_784_()
    data class Success(val items: List<GenModel_784_>) : GenState_784_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_784_()
    data class Partial(val items: List<GenModel_784_>, val hasMore: Boolean) : GenState_784_()
}

interface GenRepository_784_ {
    suspend fun getAll(): List<GenModel_784_>
    suspend fun getById(id: Long): GenModel_784_?
    suspend fun save(model: GenModel_784_): GenModel_784_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_784_>
}

@Singleton
class GenRepositoryImpl_784_ @Inject constructor() : GenRepository_784_ {
    private val store = mutableMapOf<Long, GenModel_784_>()
    override suspend fun getAll(): List<GenModel_784_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_784_? = store[id]
    override suspend fun save(model: GenModel_784_): GenModel_784_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_784_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_784_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_784_ @Inject constructor(
    private val repository: GenRepositoryImpl_784_
) : GenUseCase_784_<Unit, List<GenModel_784_>> {
    override suspend fun invoke(params: Unit): List<GenModel_784_> = repository.getAll()
}

class GenSaveUseCase_784_ @Inject constructor(
    private val repository: GenRepositoryImpl_784_
) : GenUseCase_784_<GenModel_784_, GenModel_784_> {
    override suspend fun invoke(params: GenModel_784_): GenModel_784_ = repository.save(params)
}

class GenDeleteUseCase_784_ @Inject constructor(
    private val repository: GenRepositoryImpl_784_
) : GenUseCase_784_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_784_ @Inject constructor(
    private val repository: GenRepositoryImpl_784_
) : GenUseCase_784_<String, List<GenModel_784_>> {
    override suspend fun invoke(params: String): List<GenModel_784_> = repository.search(params)
}

abstract class GenMapper_784_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_784_ : GenMapper_784_<GenModel_784_, String>() {
    override fun map(input: GenModel_784_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_784_ : GenMapper_784_<String, GenModel_784_>() {
    override fun map(input: String): GenModel_784_ {
        val parts = input.split(":")
        return GenModel_784_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_784_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_784_,
    private val saveUseCase: GenSaveUseCase_784_,
    private val deleteUseCase: GenDeleteUseCase_784_,
    private val searchUseCase: GenSearchUseCase_784_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_784_>(GenState_784_.Idle)
    val state: StateFlow<GenState_784_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_784_) {
        when (event) {
            is GenEvent_784_.Load -> loadAll()
            is GenEvent_784_.Update -> save(event.model)
            is GenEvent_784_.Delete -> delete(event.id)
            is GenEvent_784_.Refresh -> loadAll()
            is GenEvent_784_.Search -> search(event.query)
            is GenEvent_784_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_784_.Loading; _state.value = GenState_784_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_784_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_784_.Success(searchUseCase(query)) } }
}
