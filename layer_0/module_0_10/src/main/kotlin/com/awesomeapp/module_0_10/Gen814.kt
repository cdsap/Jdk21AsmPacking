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

data class GenModel_814_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_814_ {
    data class Load(val id: Long) : GenEvent_814_()
    data class Update(val model: GenModel_814_) : GenEvent_814_()
    data class Delete(val id: Long) : GenEvent_814_()
    data object Refresh : GenEvent_814_()
    data class Search(val query: String) : GenEvent_814_()
    data class Filter(val predicate: String) : GenEvent_814_()
}

sealed class GenState_814_ {
    data object Idle : GenState_814_()
    data object Loading : GenState_814_()
    data class Success(val items: List<GenModel_814_>) : GenState_814_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_814_()
    data class Partial(val items: List<GenModel_814_>, val hasMore: Boolean) : GenState_814_()
}

interface GenRepository_814_ {
    suspend fun getAll(): List<GenModel_814_>
    suspend fun getById(id: Long): GenModel_814_?
    suspend fun save(model: GenModel_814_): GenModel_814_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_814_>
}

@Singleton
class GenRepositoryImpl_814_ @Inject constructor() : GenRepository_814_ {
    private val store = mutableMapOf<Long, GenModel_814_>()
    override suspend fun getAll(): List<GenModel_814_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_814_? = store[id]
    override suspend fun save(model: GenModel_814_): GenModel_814_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_814_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_814_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_814_ @Inject constructor(
    private val repository: GenRepositoryImpl_814_
) : GenUseCase_814_<Unit, List<GenModel_814_>> {
    override suspend fun invoke(params: Unit): List<GenModel_814_> = repository.getAll()
}

class GenSaveUseCase_814_ @Inject constructor(
    private val repository: GenRepositoryImpl_814_
) : GenUseCase_814_<GenModel_814_, GenModel_814_> {
    override suspend fun invoke(params: GenModel_814_): GenModel_814_ = repository.save(params)
}

class GenDeleteUseCase_814_ @Inject constructor(
    private val repository: GenRepositoryImpl_814_
) : GenUseCase_814_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_814_ @Inject constructor(
    private val repository: GenRepositoryImpl_814_
) : GenUseCase_814_<String, List<GenModel_814_>> {
    override suspend fun invoke(params: String): List<GenModel_814_> = repository.search(params)
}

abstract class GenMapper_814_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_814_ : GenMapper_814_<GenModel_814_, String>() {
    override fun map(input: GenModel_814_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_814_ : GenMapper_814_<String, GenModel_814_>() {
    override fun map(input: String): GenModel_814_ {
        val parts = input.split(":")
        return GenModel_814_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_814_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_814_,
    private val saveUseCase: GenSaveUseCase_814_,
    private val deleteUseCase: GenDeleteUseCase_814_,
    private val searchUseCase: GenSearchUseCase_814_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_814_>(GenState_814_.Idle)
    val state: StateFlow<GenState_814_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_814_) {
        when (event) {
            is GenEvent_814_.Load -> loadAll()
            is GenEvent_814_.Update -> save(event.model)
            is GenEvent_814_.Delete -> delete(event.id)
            is GenEvent_814_.Refresh -> loadAll()
            is GenEvent_814_.Search -> search(event.query)
            is GenEvent_814_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_814_.Loading; _state.value = GenState_814_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_814_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_814_.Success(searchUseCase(query)) } }
}
