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

data class GenModel_747_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_747_ {
    data class Load(val id: Long) : GenEvent_747_()
    data class Update(val model: GenModel_747_) : GenEvent_747_()
    data class Delete(val id: Long) : GenEvent_747_()
    data object Refresh : GenEvent_747_()
    data class Search(val query: String) : GenEvent_747_()
    data class Filter(val predicate: String) : GenEvent_747_()
}

sealed class GenState_747_ {
    data object Idle : GenState_747_()
    data object Loading : GenState_747_()
    data class Success(val items: List<GenModel_747_>) : GenState_747_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_747_()
    data class Partial(val items: List<GenModel_747_>, val hasMore: Boolean) : GenState_747_()
}

interface GenRepository_747_ {
    suspend fun getAll(): List<GenModel_747_>
    suspend fun getById(id: Long): GenModel_747_?
    suspend fun save(model: GenModel_747_): GenModel_747_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_747_>
}

@Singleton
class GenRepositoryImpl_747_ @Inject constructor() : GenRepository_747_ {
    private val store = mutableMapOf<Long, GenModel_747_>()
    override suspend fun getAll(): List<GenModel_747_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_747_? = store[id]
    override suspend fun save(model: GenModel_747_): GenModel_747_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_747_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_747_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_747_ @Inject constructor(
    private val repository: GenRepositoryImpl_747_
) : GenUseCase_747_<Unit, List<GenModel_747_>> {
    override suspend fun invoke(params: Unit): List<GenModel_747_> = repository.getAll()
}

class GenSaveUseCase_747_ @Inject constructor(
    private val repository: GenRepositoryImpl_747_
) : GenUseCase_747_<GenModel_747_, GenModel_747_> {
    override suspend fun invoke(params: GenModel_747_): GenModel_747_ = repository.save(params)
}

class GenDeleteUseCase_747_ @Inject constructor(
    private val repository: GenRepositoryImpl_747_
) : GenUseCase_747_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_747_ @Inject constructor(
    private val repository: GenRepositoryImpl_747_
) : GenUseCase_747_<String, List<GenModel_747_>> {
    override suspend fun invoke(params: String): List<GenModel_747_> = repository.search(params)
}

abstract class GenMapper_747_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_747_ : GenMapper_747_<GenModel_747_, String>() {
    override fun map(input: GenModel_747_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_747_ : GenMapper_747_<String, GenModel_747_>() {
    override fun map(input: String): GenModel_747_ {
        val parts = input.split(":")
        return GenModel_747_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_747_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_747_,
    private val saveUseCase: GenSaveUseCase_747_,
    private val deleteUseCase: GenDeleteUseCase_747_,
    private val searchUseCase: GenSearchUseCase_747_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_747_>(GenState_747_.Idle)
    val state: StateFlow<GenState_747_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_747_) {
        when (event) {
            is GenEvent_747_.Load -> loadAll()
            is GenEvent_747_.Update -> save(event.model)
            is GenEvent_747_.Delete -> delete(event.id)
            is GenEvent_747_.Refresh -> loadAll()
            is GenEvent_747_.Search -> search(event.query)
            is GenEvent_747_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_747_.Loading; _state.value = GenState_747_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_747_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_747_.Success(searchUseCase(query)) } }
}
