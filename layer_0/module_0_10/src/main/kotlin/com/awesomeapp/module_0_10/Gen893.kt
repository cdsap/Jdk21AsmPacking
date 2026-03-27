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

data class GenModel_893_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_893_ {
    data class Load(val id: Long) : GenEvent_893_()
    data class Update(val model: GenModel_893_) : GenEvent_893_()
    data class Delete(val id: Long) : GenEvent_893_()
    data object Refresh : GenEvent_893_()
    data class Search(val query: String) : GenEvent_893_()
    data class Filter(val predicate: String) : GenEvent_893_()
}

sealed class GenState_893_ {
    data object Idle : GenState_893_()
    data object Loading : GenState_893_()
    data class Success(val items: List<GenModel_893_>) : GenState_893_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_893_()
    data class Partial(val items: List<GenModel_893_>, val hasMore: Boolean) : GenState_893_()
}

interface GenRepository_893_ {
    suspend fun getAll(): List<GenModel_893_>
    suspend fun getById(id: Long): GenModel_893_?
    suspend fun save(model: GenModel_893_): GenModel_893_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_893_>
}

@Singleton
class GenRepositoryImpl_893_ @Inject constructor() : GenRepository_893_ {
    private val store = mutableMapOf<Long, GenModel_893_>()
    override suspend fun getAll(): List<GenModel_893_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_893_? = store[id]
    override suspend fun save(model: GenModel_893_): GenModel_893_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_893_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_893_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_893_ @Inject constructor(
    private val repository: GenRepositoryImpl_893_
) : GenUseCase_893_<Unit, List<GenModel_893_>> {
    override suspend fun invoke(params: Unit): List<GenModel_893_> = repository.getAll()
}

class GenSaveUseCase_893_ @Inject constructor(
    private val repository: GenRepositoryImpl_893_
) : GenUseCase_893_<GenModel_893_, GenModel_893_> {
    override suspend fun invoke(params: GenModel_893_): GenModel_893_ = repository.save(params)
}

class GenDeleteUseCase_893_ @Inject constructor(
    private val repository: GenRepositoryImpl_893_
) : GenUseCase_893_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_893_ @Inject constructor(
    private val repository: GenRepositoryImpl_893_
) : GenUseCase_893_<String, List<GenModel_893_>> {
    override suspend fun invoke(params: String): List<GenModel_893_> = repository.search(params)
}

abstract class GenMapper_893_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_893_ : GenMapper_893_<GenModel_893_, String>() {
    override fun map(input: GenModel_893_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_893_ : GenMapper_893_<String, GenModel_893_>() {
    override fun map(input: String): GenModel_893_ {
        val parts = input.split(":")
        return GenModel_893_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_893_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_893_,
    private val saveUseCase: GenSaveUseCase_893_,
    private val deleteUseCase: GenDeleteUseCase_893_,
    private val searchUseCase: GenSearchUseCase_893_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_893_>(GenState_893_.Idle)
    val state: StateFlow<GenState_893_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_893_) {
        when (event) {
            is GenEvent_893_.Load -> loadAll()
            is GenEvent_893_.Update -> save(event.model)
            is GenEvent_893_.Delete -> delete(event.id)
            is GenEvent_893_.Refresh -> loadAll()
            is GenEvent_893_.Search -> search(event.query)
            is GenEvent_893_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_893_.Loading; _state.value = GenState_893_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_893_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_893_.Success(searchUseCase(query)) } }
}
