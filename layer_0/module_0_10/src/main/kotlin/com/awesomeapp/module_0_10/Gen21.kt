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

data class GenModel_21_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_21_ {
    data class Load(val id: Long) : GenEvent_21_()
    data class Update(val model: GenModel_21_) : GenEvent_21_()
    data class Delete(val id: Long) : GenEvent_21_()
    data object Refresh : GenEvent_21_()
    data class Search(val query: String) : GenEvent_21_()
    data class Filter(val predicate: String) : GenEvent_21_()
}

sealed class GenState_21_ {
    data object Idle : GenState_21_()
    data object Loading : GenState_21_()
    data class Success(val items: List<GenModel_21_>) : GenState_21_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_21_()
    data class Partial(val items: List<GenModel_21_>, val hasMore: Boolean) : GenState_21_()
}

interface GenRepository_21_ {
    suspend fun getAll(): List<GenModel_21_>
    suspend fun getById(id: Long): GenModel_21_?
    suspend fun save(model: GenModel_21_): GenModel_21_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_21_>
}

@Singleton
class GenRepositoryImpl_21_ @Inject constructor() : GenRepository_21_ {
    private val store = mutableMapOf<Long, GenModel_21_>()
    override suspend fun getAll(): List<GenModel_21_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_21_? = store[id]
    override suspend fun save(model: GenModel_21_): GenModel_21_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_21_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_21_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_21_ @Inject constructor(
    private val repository: GenRepositoryImpl_21_
) : GenUseCase_21_<Unit, List<GenModel_21_>> {
    override suspend fun invoke(params: Unit): List<GenModel_21_> = repository.getAll()
}

class GenSaveUseCase_21_ @Inject constructor(
    private val repository: GenRepositoryImpl_21_
) : GenUseCase_21_<GenModel_21_, GenModel_21_> {
    override suspend fun invoke(params: GenModel_21_): GenModel_21_ = repository.save(params)
}

class GenDeleteUseCase_21_ @Inject constructor(
    private val repository: GenRepositoryImpl_21_
) : GenUseCase_21_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_21_ @Inject constructor(
    private val repository: GenRepositoryImpl_21_
) : GenUseCase_21_<String, List<GenModel_21_>> {
    override suspend fun invoke(params: String): List<GenModel_21_> = repository.search(params)
}

abstract class GenMapper_21_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_21_ : GenMapper_21_<GenModel_21_, String>() {
    override fun map(input: GenModel_21_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_21_ : GenMapper_21_<String, GenModel_21_>() {
    override fun map(input: String): GenModel_21_ {
        val parts = input.split(":")
        return GenModel_21_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_21_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_21_,
    private val saveUseCase: GenSaveUseCase_21_,
    private val deleteUseCase: GenDeleteUseCase_21_,
    private val searchUseCase: GenSearchUseCase_21_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_21_>(GenState_21_.Idle)
    val state: StateFlow<GenState_21_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_21_) {
        when (event) {
            is GenEvent_21_.Load -> loadAll()
            is GenEvent_21_.Update -> save(event.model)
            is GenEvent_21_.Delete -> delete(event.id)
            is GenEvent_21_.Refresh -> loadAll()
            is GenEvent_21_.Search -> search(event.query)
            is GenEvent_21_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_21_.Loading; _state.value = GenState_21_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_21_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_21_.Success(searchUseCase(query)) } }
}
