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

data class GenModel_2_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2_ {
    data class Load(val id: Long) : GenEvent_2_()
    data class Update(val model: GenModel_2_) : GenEvent_2_()
    data class Delete(val id: Long) : GenEvent_2_()
    data object Refresh : GenEvent_2_()
    data class Search(val query: String) : GenEvent_2_()
    data class Filter(val predicate: String) : GenEvent_2_()
}

sealed class GenState_2_ {
    data object Idle : GenState_2_()
    data object Loading : GenState_2_()
    data class Success(val items: List<GenModel_2_>) : GenState_2_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2_()
    data class Partial(val items: List<GenModel_2_>, val hasMore: Boolean) : GenState_2_()
}

interface GenRepository_2_ {
    suspend fun getAll(): List<GenModel_2_>
    suspend fun getById(id: Long): GenModel_2_?
    suspend fun save(model: GenModel_2_): GenModel_2_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2_>
}

@Singleton
class GenRepositoryImpl_2_ @Inject constructor() : GenRepository_2_ {
    private val store = mutableMapOf<Long, GenModel_2_>()
    override suspend fun getAll(): List<GenModel_2_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2_? = store[id]
    override suspend fun save(model: GenModel_2_): GenModel_2_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2_ @Inject constructor(
    private val repository: GenRepositoryImpl_2_
) : GenUseCase_2_<Unit, List<GenModel_2_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2_> = repository.getAll()
}

class GenSaveUseCase_2_ @Inject constructor(
    private val repository: GenRepositoryImpl_2_
) : GenUseCase_2_<GenModel_2_, GenModel_2_> {
    override suspend fun invoke(params: GenModel_2_): GenModel_2_ = repository.save(params)
}

class GenDeleteUseCase_2_ @Inject constructor(
    private val repository: GenRepositoryImpl_2_
) : GenUseCase_2_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2_ @Inject constructor(
    private val repository: GenRepositoryImpl_2_
) : GenUseCase_2_<String, List<GenModel_2_>> {
    override suspend fun invoke(params: String): List<GenModel_2_> = repository.search(params)
}

abstract class GenMapper_2_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2_ : GenMapper_2_<GenModel_2_, String>() {
    override fun map(input: GenModel_2_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2_ : GenMapper_2_<String, GenModel_2_>() {
    override fun map(input: String): GenModel_2_ {
        val parts = input.split(":")
        return GenModel_2_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2_,
    private val saveUseCase: GenSaveUseCase_2_,
    private val deleteUseCase: GenDeleteUseCase_2_,
    private val searchUseCase: GenSearchUseCase_2_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2_>(GenState_2_.Idle)
    val state: StateFlow<GenState_2_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2_) {
        when (event) {
            is GenEvent_2_.Load -> loadAll()
            is GenEvent_2_.Update -> save(event.model)
            is GenEvent_2_.Delete -> delete(event.id)
            is GenEvent_2_.Refresh -> loadAll()
            is GenEvent_2_.Search -> search(event.query)
            is GenEvent_2_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2_.Loading; _state.value = GenState_2_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2_.Success(searchUseCase(query)) } }
}
