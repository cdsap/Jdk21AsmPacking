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

data class GenModel_506_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_506_ {
    data class Load(val id: Long) : GenEvent_506_()
    data class Update(val model: GenModel_506_) : GenEvent_506_()
    data class Delete(val id: Long) : GenEvent_506_()
    data object Refresh : GenEvent_506_()
    data class Search(val query: String) : GenEvent_506_()
    data class Filter(val predicate: String) : GenEvent_506_()
}

sealed class GenState_506_ {
    data object Idle : GenState_506_()
    data object Loading : GenState_506_()
    data class Success(val items: List<GenModel_506_>) : GenState_506_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_506_()
    data class Partial(val items: List<GenModel_506_>, val hasMore: Boolean) : GenState_506_()
}

interface GenRepository_506_ {
    suspend fun getAll(): List<GenModel_506_>
    suspend fun getById(id: Long): GenModel_506_?
    suspend fun save(model: GenModel_506_): GenModel_506_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_506_>
}

@Singleton
class GenRepositoryImpl_506_ @Inject constructor() : GenRepository_506_ {
    private val store = mutableMapOf<Long, GenModel_506_>()
    override suspend fun getAll(): List<GenModel_506_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_506_? = store[id]
    override suspend fun save(model: GenModel_506_): GenModel_506_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_506_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_506_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_506_ @Inject constructor(
    private val repository: GenRepositoryImpl_506_
) : GenUseCase_506_<Unit, List<GenModel_506_>> {
    override suspend fun invoke(params: Unit): List<GenModel_506_> = repository.getAll()
}

class GenSaveUseCase_506_ @Inject constructor(
    private val repository: GenRepositoryImpl_506_
) : GenUseCase_506_<GenModel_506_, GenModel_506_> {
    override suspend fun invoke(params: GenModel_506_): GenModel_506_ = repository.save(params)
}

class GenDeleteUseCase_506_ @Inject constructor(
    private val repository: GenRepositoryImpl_506_
) : GenUseCase_506_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_506_ @Inject constructor(
    private val repository: GenRepositoryImpl_506_
) : GenUseCase_506_<String, List<GenModel_506_>> {
    override suspend fun invoke(params: String): List<GenModel_506_> = repository.search(params)
}

abstract class GenMapper_506_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_506_ : GenMapper_506_<GenModel_506_, String>() {
    override fun map(input: GenModel_506_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_506_ : GenMapper_506_<String, GenModel_506_>() {
    override fun map(input: String): GenModel_506_ {
        val parts = input.split(":")
        return GenModel_506_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_506_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_506_,
    private val saveUseCase: GenSaveUseCase_506_,
    private val deleteUseCase: GenDeleteUseCase_506_,
    private val searchUseCase: GenSearchUseCase_506_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_506_>(GenState_506_.Idle)
    val state: StateFlow<GenState_506_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_506_) {
        when (event) {
            is GenEvent_506_.Load -> loadAll()
            is GenEvent_506_.Update -> save(event.model)
            is GenEvent_506_.Delete -> delete(event.id)
            is GenEvent_506_.Refresh -> loadAll()
            is GenEvent_506_.Search -> search(event.query)
            is GenEvent_506_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_506_.Loading; _state.value = GenState_506_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_506_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_506_.Success(searchUseCase(query)) } }
}
