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

data class GenModel_93_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_93_ {
    data class Load(val id: Long) : GenEvent_93_()
    data class Update(val model: GenModel_93_) : GenEvent_93_()
    data class Delete(val id: Long) : GenEvent_93_()
    data object Refresh : GenEvent_93_()
    data class Search(val query: String) : GenEvent_93_()
    data class Filter(val predicate: String) : GenEvent_93_()
}

sealed class GenState_93_ {
    data object Idle : GenState_93_()
    data object Loading : GenState_93_()
    data class Success(val items: List<GenModel_93_>) : GenState_93_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_93_()
    data class Partial(val items: List<GenModel_93_>, val hasMore: Boolean) : GenState_93_()
}

interface GenRepository_93_ {
    suspend fun getAll(): List<GenModel_93_>
    suspend fun getById(id: Long): GenModel_93_?
    suspend fun save(model: GenModel_93_): GenModel_93_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_93_>
}

@Singleton
class GenRepositoryImpl_93_ @Inject constructor() : GenRepository_93_ {
    private val store = mutableMapOf<Long, GenModel_93_>()
    override suspend fun getAll(): List<GenModel_93_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_93_? = store[id]
    override suspend fun save(model: GenModel_93_): GenModel_93_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_93_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_93_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_93_ @Inject constructor(
    private val repository: GenRepositoryImpl_93_
) : GenUseCase_93_<Unit, List<GenModel_93_>> {
    override suspend fun invoke(params: Unit): List<GenModel_93_> = repository.getAll()
}

class GenSaveUseCase_93_ @Inject constructor(
    private val repository: GenRepositoryImpl_93_
) : GenUseCase_93_<GenModel_93_, GenModel_93_> {
    override suspend fun invoke(params: GenModel_93_): GenModel_93_ = repository.save(params)
}

class GenDeleteUseCase_93_ @Inject constructor(
    private val repository: GenRepositoryImpl_93_
) : GenUseCase_93_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_93_ @Inject constructor(
    private val repository: GenRepositoryImpl_93_
) : GenUseCase_93_<String, List<GenModel_93_>> {
    override suspend fun invoke(params: String): List<GenModel_93_> = repository.search(params)
}

abstract class GenMapper_93_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_93_ : GenMapper_93_<GenModel_93_, String>() {
    override fun map(input: GenModel_93_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_93_ : GenMapper_93_<String, GenModel_93_>() {
    override fun map(input: String): GenModel_93_ {
        val parts = input.split(":")
        return GenModel_93_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_93_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_93_,
    private val saveUseCase: GenSaveUseCase_93_,
    private val deleteUseCase: GenDeleteUseCase_93_,
    private val searchUseCase: GenSearchUseCase_93_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_93_>(GenState_93_.Idle)
    val state: StateFlow<GenState_93_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_93_) {
        when (event) {
            is GenEvent_93_.Load -> loadAll()
            is GenEvent_93_.Update -> save(event.model)
            is GenEvent_93_.Delete -> delete(event.id)
            is GenEvent_93_.Refresh -> loadAll()
            is GenEvent_93_.Search -> search(event.query)
            is GenEvent_93_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_93_.Loading; _state.value = GenState_93_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_93_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_93_.Success(searchUseCase(query)) } }
}
