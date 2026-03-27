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

data class GenModel_220_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_220_ {
    data class Load(val id: Long) : GenEvent_220_()
    data class Update(val model: GenModel_220_) : GenEvent_220_()
    data class Delete(val id: Long) : GenEvent_220_()
    data object Refresh : GenEvent_220_()
    data class Search(val query: String) : GenEvent_220_()
    data class Filter(val predicate: String) : GenEvent_220_()
}

sealed class GenState_220_ {
    data object Idle : GenState_220_()
    data object Loading : GenState_220_()
    data class Success(val items: List<GenModel_220_>) : GenState_220_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_220_()
    data class Partial(val items: List<GenModel_220_>, val hasMore: Boolean) : GenState_220_()
}

interface GenRepository_220_ {
    suspend fun getAll(): List<GenModel_220_>
    suspend fun getById(id: Long): GenModel_220_?
    suspend fun save(model: GenModel_220_): GenModel_220_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_220_>
}

@Singleton
class GenRepositoryImpl_220_ @Inject constructor() : GenRepository_220_ {
    private val store = mutableMapOf<Long, GenModel_220_>()
    override suspend fun getAll(): List<GenModel_220_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_220_? = store[id]
    override suspend fun save(model: GenModel_220_): GenModel_220_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_220_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_220_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_220_ @Inject constructor(
    private val repository: GenRepositoryImpl_220_
) : GenUseCase_220_<Unit, List<GenModel_220_>> {
    override suspend fun invoke(params: Unit): List<GenModel_220_> = repository.getAll()
}

class GenSaveUseCase_220_ @Inject constructor(
    private val repository: GenRepositoryImpl_220_
) : GenUseCase_220_<GenModel_220_, GenModel_220_> {
    override suspend fun invoke(params: GenModel_220_): GenModel_220_ = repository.save(params)
}

class GenDeleteUseCase_220_ @Inject constructor(
    private val repository: GenRepositoryImpl_220_
) : GenUseCase_220_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_220_ @Inject constructor(
    private val repository: GenRepositoryImpl_220_
) : GenUseCase_220_<String, List<GenModel_220_>> {
    override suspend fun invoke(params: String): List<GenModel_220_> = repository.search(params)
}

abstract class GenMapper_220_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_220_ : GenMapper_220_<GenModel_220_, String>() {
    override fun map(input: GenModel_220_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_220_ : GenMapper_220_<String, GenModel_220_>() {
    override fun map(input: String): GenModel_220_ {
        val parts = input.split(":")
        return GenModel_220_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_220_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_220_,
    private val saveUseCase: GenSaveUseCase_220_,
    private val deleteUseCase: GenDeleteUseCase_220_,
    private val searchUseCase: GenSearchUseCase_220_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_220_>(GenState_220_.Idle)
    val state: StateFlow<GenState_220_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_220_) {
        when (event) {
            is GenEvent_220_.Load -> loadAll()
            is GenEvent_220_.Update -> save(event.model)
            is GenEvent_220_.Delete -> delete(event.id)
            is GenEvent_220_.Refresh -> loadAll()
            is GenEvent_220_.Search -> search(event.query)
            is GenEvent_220_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_220_.Loading; _state.value = GenState_220_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_220_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_220_.Success(searchUseCase(query)) } }
}
