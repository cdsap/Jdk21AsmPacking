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

data class GenModel_859_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_859_ {
    data class Load(val id: Long) : GenEvent_859_()
    data class Update(val model: GenModel_859_) : GenEvent_859_()
    data class Delete(val id: Long) : GenEvent_859_()
    data object Refresh : GenEvent_859_()
    data class Search(val query: String) : GenEvent_859_()
    data class Filter(val predicate: String) : GenEvent_859_()
}

sealed class GenState_859_ {
    data object Idle : GenState_859_()
    data object Loading : GenState_859_()
    data class Success(val items: List<GenModel_859_>) : GenState_859_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_859_()
    data class Partial(val items: List<GenModel_859_>, val hasMore: Boolean) : GenState_859_()
}

interface GenRepository_859_ {
    suspend fun getAll(): List<GenModel_859_>
    suspend fun getById(id: Long): GenModel_859_?
    suspend fun save(model: GenModel_859_): GenModel_859_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_859_>
}

@Singleton
class GenRepositoryImpl_859_ @Inject constructor() : GenRepository_859_ {
    private val store = mutableMapOf<Long, GenModel_859_>()
    override suspend fun getAll(): List<GenModel_859_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_859_? = store[id]
    override suspend fun save(model: GenModel_859_): GenModel_859_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_859_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_859_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_859_ @Inject constructor(
    private val repository: GenRepositoryImpl_859_
) : GenUseCase_859_<Unit, List<GenModel_859_>> {
    override suspend fun invoke(params: Unit): List<GenModel_859_> = repository.getAll()
}

class GenSaveUseCase_859_ @Inject constructor(
    private val repository: GenRepositoryImpl_859_
) : GenUseCase_859_<GenModel_859_, GenModel_859_> {
    override suspend fun invoke(params: GenModel_859_): GenModel_859_ = repository.save(params)
}

class GenDeleteUseCase_859_ @Inject constructor(
    private val repository: GenRepositoryImpl_859_
) : GenUseCase_859_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_859_ @Inject constructor(
    private val repository: GenRepositoryImpl_859_
) : GenUseCase_859_<String, List<GenModel_859_>> {
    override suspend fun invoke(params: String): List<GenModel_859_> = repository.search(params)
}

abstract class GenMapper_859_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_859_ : GenMapper_859_<GenModel_859_, String>() {
    override fun map(input: GenModel_859_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_859_ : GenMapper_859_<String, GenModel_859_>() {
    override fun map(input: String): GenModel_859_ {
        val parts = input.split(":")
        return GenModel_859_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_859_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_859_,
    private val saveUseCase: GenSaveUseCase_859_,
    private val deleteUseCase: GenDeleteUseCase_859_,
    private val searchUseCase: GenSearchUseCase_859_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_859_>(GenState_859_.Idle)
    val state: StateFlow<GenState_859_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_859_) {
        when (event) {
            is GenEvent_859_.Load -> loadAll()
            is GenEvent_859_.Update -> save(event.model)
            is GenEvent_859_.Delete -> delete(event.id)
            is GenEvent_859_.Refresh -> loadAll()
            is GenEvent_859_.Search -> search(event.query)
            is GenEvent_859_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_859_.Loading; _state.value = GenState_859_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_859_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_859_.Success(searchUseCase(query)) } }
}
