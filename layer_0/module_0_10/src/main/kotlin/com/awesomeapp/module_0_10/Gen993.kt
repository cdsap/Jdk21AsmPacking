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

data class GenModel_993_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_993_ {
    data class Load(val id: Long) : GenEvent_993_()
    data class Update(val model: GenModel_993_) : GenEvent_993_()
    data class Delete(val id: Long) : GenEvent_993_()
    data object Refresh : GenEvent_993_()
    data class Search(val query: String) : GenEvent_993_()
    data class Filter(val predicate: String) : GenEvent_993_()
}

sealed class GenState_993_ {
    data object Idle : GenState_993_()
    data object Loading : GenState_993_()
    data class Success(val items: List<GenModel_993_>) : GenState_993_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_993_()
    data class Partial(val items: List<GenModel_993_>, val hasMore: Boolean) : GenState_993_()
}

interface GenRepository_993_ {
    suspend fun getAll(): List<GenModel_993_>
    suspend fun getById(id: Long): GenModel_993_?
    suspend fun save(model: GenModel_993_): GenModel_993_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_993_>
}

@Singleton
class GenRepositoryImpl_993_ @Inject constructor() : GenRepository_993_ {
    private val store = mutableMapOf<Long, GenModel_993_>()
    override suspend fun getAll(): List<GenModel_993_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_993_? = store[id]
    override suspend fun save(model: GenModel_993_): GenModel_993_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_993_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_993_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_993_ @Inject constructor(
    private val repository: GenRepositoryImpl_993_
) : GenUseCase_993_<Unit, List<GenModel_993_>> {
    override suspend fun invoke(params: Unit): List<GenModel_993_> = repository.getAll()
}

class GenSaveUseCase_993_ @Inject constructor(
    private val repository: GenRepositoryImpl_993_
) : GenUseCase_993_<GenModel_993_, GenModel_993_> {
    override suspend fun invoke(params: GenModel_993_): GenModel_993_ = repository.save(params)
}

class GenDeleteUseCase_993_ @Inject constructor(
    private val repository: GenRepositoryImpl_993_
) : GenUseCase_993_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_993_ @Inject constructor(
    private val repository: GenRepositoryImpl_993_
) : GenUseCase_993_<String, List<GenModel_993_>> {
    override suspend fun invoke(params: String): List<GenModel_993_> = repository.search(params)
}

abstract class GenMapper_993_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_993_ : GenMapper_993_<GenModel_993_, String>() {
    override fun map(input: GenModel_993_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_993_ : GenMapper_993_<String, GenModel_993_>() {
    override fun map(input: String): GenModel_993_ {
        val parts = input.split(":")
        return GenModel_993_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_993_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_993_,
    private val saveUseCase: GenSaveUseCase_993_,
    private val deleteUseCase: GenDeleteUseCase_993_,
    private val searchUseCase: GenSearchUseCase_993_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_993_>(GenState_993_.Idle)
    val state: StateFlow<GenState_993_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_993_) {
        when (event) {
            is GenEvent_993_.Load -> loadAll()
            is GenEvent_993_.Update -> save(event.model)
            is GenEvent_993_.Delete -> delete(event.id)
            is GenEvent_993_.Refresh -> loadAll()
            is GenEvent_993_.Search -> search(event.query)
            is GenEvent_993_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_993_.Loading; _state.value = GenState_993_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_993_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_993_.Success(searchUseCase(query)) } }
}
