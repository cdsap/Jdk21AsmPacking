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

data class GenModel_538_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_538_ {
    data class Load(val id: Long) : GenEvent_538_()
    data class Update(val model: GenModel_538_) : GenEvent_538_()
    data class Delete(val id: Long) : GenEvent_538_()
    data object Refresh : GenEvent_538_()
    data class Search(val query: String) : GenEvent_538_()
    data class Filter(val predicate: String) : GenEvent_538_()
}

sealed class GenState_538_ {
    data object Idle : GenState_538_()
    data object Loading : GenState_538_()
    data class Success(val items: List<GenModel_538_>) : GenState_538_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_538_()
    data class Partial(val items: List<GenModel_538_>, val hasMore: Boolean) : GenState_538_()
}

interface GenRepository_538_ {
    suspend fun getAll(): List<GenModel_538_>
    suspend fun getById(id: Long): GenModel_538_?
    suspend fun save(model: GenModel_538_): GenModel_538_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_538_>
}

@Singleton
class GenRepositoryImpl_538_ @Inject constructor() : GenRepository_538_ {
    private val store = mutableMapOf<Long, GenModel_538_>()
    override suspend fun getAll(): List<GenModel_538_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_538_? = store[id]
    override suspend fun save(model: GenModel_538_): GenModel_538_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_538_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_538_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_538_ @Inject constructor(
    private val repository: GenRepositoryImpl_538_
) : GenUseCase_538_<Unit, List<GenModel_538_>> {
    override suspend fun invoke(params: Unit): List<GenModel_538_> = repository.getAll()
}

class GenSaveUseCase_538_ @Inject constructor(
    private val repository: GenRepositoryImpl_538_
) : GenUseCase_538_<GenModel_538_, GenModel_538_> {
    override suspend fun invoke(params: GenModel_538_): GenModel_538_ = repository.save(params)
}

class GenDeleteUseCase_538_ @Inject constructor(
    private val repository: GenRepositoryImpl_538_
) : GenUseCase_538_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_538_ @Inject constructor(
    private val repository: GenRepositoryImpl_538_
) : GenUseCase_538_<String, List<GenModel_538_>> {
    override suspend fun invoke(params: String): List<GenModel_538_> = repository.search(params)
}

abstract class GenMapper_538_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_538_ : GenMapper_538_<GenModel_538_, String>() {
    override fun map(input: GenModel_538_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_538_ : GenMapper_538_<String, GenModel_538_>() {
    override fun map(input: String): GenModel_538_ {
        val parts = input.split(":")
        return GenModel_538_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_538_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_538_,
    private val saveUseCase: GenSaveUseCase_538_,
    private val deleteUseCase: GenDeleteUseCase_538_,
    private val searchUseCase: GenSearchUseCase_538_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_538_>(GenState_538_.Idle)
    val state: StateFlow<GenState_538_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_538_) {
        when (event) {
            is GenEvent_538_.Load -> loadAll()
            is GenEvent_538_.Update -> save(event.model)
            is GenEvent_538_.Delete -> delete(event.id)
            is GenEvent_538_.Refresh -> loadAll()
            is GenEvent_538_.Search -> search(event.query)
            is GenEvent_538_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_538_.Loading; _state.value = GenState_538_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_538_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_538_.Success(searchUseCase(query)) } }
}
