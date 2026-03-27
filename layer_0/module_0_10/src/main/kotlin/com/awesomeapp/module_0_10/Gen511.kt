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

data class GenModel_511_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_511_ {
    data class Load(val id: Long) : GenEvent_511_()
    data class Update(val model: GenModel_511_) : GenEvent_511_()
    data class Delete(val id: Long) : GenEvent_511_()
    data object Refresh : GenEvent_511_()
    data class Search(val query: String) : GenEvent_511_()
    data class Filter(val predicate: String) : GenEvent_511_()
}

sealed class GenState_511_ {
    data object Idle : GenState_511_()
    data object Loading : GenState_511_()
    data class Success(val items: List<GenModel_511_>) : GenState_511_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_511_()
    data class Partial(val items: List<GenModel_511_>, val hasMore: Boolean) : GenState_511_()
}

interface GenRepository_511_ {
    suspend fun getAll(): List<GenModel_511_>
    suspend fun getById(id: Long): GenModel_511_?
    suspend fun save(model: GenModel_511_): GenModel_511_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_511_>
}

@Singleton
class GenRepositoryImpl_511_ @Inject constructor() : GenRepository_511_ {
    private val store = mutableMapOf<Long, GenModel_511_>()
    override suspend fun getAll(): List<GenModel_511_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_511_? = store[id]
    override suspend fun save(model: GenModel_511_): GenModel_511_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_511_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_511_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_511_ @Inject constructor(
    private val repository: GenRepositoryImpl_511_
) : GenUseCase_511_<Unit, List<GenModel_511_>> {
    override suspend fun invoke(params: Unit): List<GenModel_511_> = repository.getAll()
}

class GenSaveUseCase_511_ @Inject constructor(
    private val repository: GenRepositoryImpl_511_
) : GenUseCase_511_<GenModel_511_, GenModel_511_> {
    override suspend fun invoke(params: GenModel_511_): GenModel_511_ = repository.save(params)
}

class GenDeleteUseCase_511_ @Inject constructor(
    private val repository: GenRepositoryImpl_511_
) : GenUseCase_511_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_511_ @Inject constructor(
    private val repository: GenRepositoryImpl_511_
) : GenUseCase_511_<String, List<GenModel_511_>> {
    override suspend fun invoke(params: String): List<GenModel_511_> = repository.search(params)
}

abstract class GenMapper_511_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_511_ : GenMapper_511_<GenModel_511_, String>() {
    override fun map(input: GenModel_511_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_511_ : GenMapper_511_<String, GenModel_511_>() {
    override fun map(input: String): GenModel_511_ {
        val parts = input.split(":")
        return GenModel_511_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_511_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_511_,
    private val saveUseCase: GenSaveUseCase_511_,
    private val deleteUseCase: GenDeleteUseCase_511_,
    private val searchUseCase: GenSearchUseCase_511_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_511_>(GenState_511_.Idle)
    val state: StateFlow<GenState_511_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_511_) {
        when (event) {
            is GenEvent_511_.Load -> loadAll()
            is GenEvent_511_.Update -> save(event.model)
            is GenEvent_511_.Delete -> delete(event.id)
            is GenEvent_511_.Refresh -> loadAll()
            is GenEvent_511_.Search -> search(event.query)
            is GenEvent_511_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_511_.Loading; _state.value = GenState_511_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_511_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_511_.Success(searchUseCase(query)) } }
}
