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

data class GenModel_883_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_883_ {
    data class Load(val id: Long) : GenEvent_883_()
    data class Update(val model: GenModel_883_) : GenEvent_883_()
    data class Delete(val id: Long) : GenEvent_883_()
    data object Refresh : GenEvent_883_()
    data class Search(val query: String) : GenEvent_883_()
    data class Filter(val predicate: String) : GenEvent_883_()
}

sealed class GenState_883_ {
    data object Idle : GenState_883_()
    data object Loading : GenState_883_()
    data class Success(val items: List<GenModel_883_>) : GenState_883_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_883_()
    data class Partial(val items: List<GenModel_883_>, val hasMore: Boolean) : GenState_883_()
}

interface GenRepository_883_ {
    suspend fun getAll(): List<GenModel_883_>
    suspend fun getById(id: Long): GenModel_883_?
    suspend fun save(model: GenModel_883_): GenModel_883_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_883_>
}

@Singleton
class GenRepositoryImpl_883_ @Inject constructor() : GenRepository_883_ {
    private val store = mutableMapOf<Long, GenModel_883_>()
    override suspend fun getAll(): List<GenModel_883_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_883_? = store[id]
    override suspend fun save(model: GenModel_883_): GenModel_883_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_883_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_883_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_883_ @Inject constructor(
    private val repository: GenRepositoryImpl_883_
) : GenUseCase_883_<Unit, List<GenModel_883_>> {
    override suspend fun invoke(params: Unit): List<GenModel_883_> = repository.getAll()
}

class GenSaveUseCase_883_ @Inject constructor(
    private val repository: GenRepositoryImpl_883_
) : GenUseCase_883_<GenModel_883_, GenModel_883_> {
    override suspend fun invoke(params: GenModel_883_): GenModel_883_ = repository.save(params)
}

class GenDeleteUseCase_883_ @Inject constructor(
    private val repository: GenRepositoryImpl_883_
) : GenUseCase_883_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_883_ @Inject constructor(
    private val repository: GenRepositoryImpl_883_
) : GenUseCase_883_<String, List<GenModel_883_>> {
    override suspend fun invoke(params: String): List<GenModel_883_> = repository.search(params)
}

abstract class GenMapper_883_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_883_ : GenMapper_883_<GenModel_883_, String>() {
    override fun map(input: GenModel_883_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_883_ : GenMapper_883_<String, GenModel_883_>() {
    override fun map(input: String): GenModel_883_ {
        val parts = input.split(":")
        return GenModel_883_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_883_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_883_,
    private val saveUseCase: GenSaveUseCase_883_,
    private val deleteUseCase: GenDeleteUseCase_883_,
    private val searchUseCase: GenSearchUseCase_883_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_883_>(GenState_883_.Idle)
    val state: StateFlow<GenState_883_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_883_) {
        when (event) {
            is GenEvent_883_.Load -> loadAll()
            is GenEvent_883_.Update -> save(event.model)
            is GenEvent_883_.Delete -> delete(event.id)
            is GenEvent_883_.Refresh -> loadAll()
            is GenEvent_883_.Search -> search(event.query)
            is GenEvent_883_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_883_.Loading; _state.value = GenState_883_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_883_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_883_.Success(searchUseCase(query)) } }
}
