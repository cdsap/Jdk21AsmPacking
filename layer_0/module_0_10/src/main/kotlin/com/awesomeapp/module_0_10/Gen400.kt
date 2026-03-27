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

data class GenModel_400_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_400_ {
    data class Load(val id: Long) : GenEvent_400_()
    data class Update(val model: GenModel_400_) : GenEvent_400_()
    data class Delete(val id: Long) : GenEvent_400_()
    data object Refresh : GenEvent_400_()
    data class Search(val query: String) : GenEvent_400_()
    data class Filter(val predicate: String) : GenEvent_400_()
}

sealed class GenState_400_ {
    data object Idle : GenState_400_()
    data object Loading : GenState_400_()
    data class Success(val items: List<GenModel_400_>) : GenState_400_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_400_()
    data class Partial(val items: List<GenModel_400_>, val hasMore: Boolean) : GenState_400_()
}

interface GenRepository_400_ {
    suspend fun getAll(): List<GenModel_400_>
    suspend fun getById(id: Long): GenModel_400_?
    suspend fun save(model: GenModel_400_): GenModel_400_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_400_>
}

@Singleton
class GenRepositoryImpl_400_ @Inject constructor() : GenRepository_400_ {
    private val store = mutableMapOf<Long, GenModel_400_>()
    override suspend fun getAll(): List<GenModel_400_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_400_? = store[id]
    override suspend fun save(model: GenModel_400_): GenModel_400_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_400_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_400_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_400_ @Inject constructor(
    private val repository: GenRepositoryImpl_400_
) : GenUseCase_400_<Unit, List<GenModel_400_>> {
    override suspend fun invoke(params: Unit): List<GenModel_400_> = repository.getAll()
}

class GenSaveUseCase_400_ @Inject constructor(
    private val repository: GenRepositoryImpl_400_
) : GenUseCase_400_<GenModel_400_, GenModel_400_> {
    override suspend fun invoke(params: GenModel_400_): GenModel_400_ = repository.save(params)
}

class GenDeleteUseCase_400_ @Inject constructor(
    private val repository: GenRepositoryImpl_400_
) : GenUseCase_400_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_400_ @Inject constructor(
    private val repository: GenRepositoryImpl_400_
) : GenUseCase_400_<String, List<GenModel_400_>> {
    override suspend fun invoke(params: String): List<GenModel_400_> = repository.search(params)
}

abstract class GenMapper_400_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_400_ : GenMapper_400_<GenModel_400_, String>() {
    override fun map(input: GenModel_400_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_400_ : GenMapper_400_<String, GenModel_400_>() {
    override fun map(input: String): GenModel_400_ {
        val parts = input.split(":")
        return GenModel_400_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_400_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_400_,
    private val saveUseCase: GenSaveUseCase_400_,
    private val deleteUseCase: GenDeleteUseCase_400_,
    private val searchUseCase: GenSearchUseCase_400_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_400_>(GenState_400_.Idle)
    val state: StateFlow<GenState_400_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_400_) {
        when (event) {
            is GenEvent_400_.Load -> loadAll()
            is GenEvent_400_.Update -> save(event.model)
            is GenEvent_400_.Delete -> delete(event.id)
            is GenEvent_400_.Refresh -> loadAll()
            is GenEvent_400_.Search -> search(event.query)
            is GenEvent_400_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_400_.Loading; _state.value = GenState_400_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_400_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_400_.Success(searchUseCase(query)) } }
}
