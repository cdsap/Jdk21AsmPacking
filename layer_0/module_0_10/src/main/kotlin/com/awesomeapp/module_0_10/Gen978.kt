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

data class GenModel_978_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_978_ {
    data class Load(val id: Long) : GenEvent_978_()
    data class Update(val model: GenModel_978_) : GenEvent_978_()
    data class Delete(val id: Long) : GenEvent_978_()
    data object Refresh : GenEvent_978_()
    data class Search(val query: String) : GenEvent_978_()
    data class Filter(val predicate: String) : GenEvent_978_()
}

sealed class GenState_978_ {
    data object Idle : GenState_978_()
    data object Loading : GenState_978_()
    data class Success(val items: List<GenModel_978_>) : GenState_978_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_978_()
    data class Partial(val items: List<GenModel_978_>, val hasMore: Boolean) : GenState_978_()
}

interface GenRepository_978_ {
    suspend fun getAll(): List<GenModel_978_>
    suspend fun getById(id: Long): GenModel_978_?
    suspend fun save(model: GenModel_978_): GenModel_978_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_978_>
}

@Singleton
class GenRepositoryImpl_978_ @Inject constructor() : GenRepository_978_ {
    private val store = mutableMapOf<Long, GenModel_978_>()
    override suspend fun getAll(): List<GenModel_978_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_978_? = store[id]
    override suspend fun save(model: GenModel_978_): GenModel_978_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_978_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_978_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_978_ @Inject constructor(
    private val repository: GenRepositoryImpl_978_
) : GenUseCase_978_<Unit, List<GenModel_978_>> {
    override suspend fun invoke(params: Unit): List<GenModel_978_> = repository.getAll()
}

class GenSaveUseCase_978_ @Inject constructor(
    private val repository: GenRepositoryImpl_978_
) : GenUseCase_978_<GenModel_978_, GenModel_978_> {
    override suspend fun invoke(params: GenModel_978_): GenModel_978_ = repository.save(params)
}

class GenDeleteUseCase_978_ @Inject constructor(
    private val repository: GenRepositoryImpl_978_
) : GenUseCase_978_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_978_ @Inject constructor(
    private val repository: GenRepositoryImpl_978_
) : GenUseCase_978_<String, List<GenModel_978_>> {
    override suspend fun invoke(params: String): List<GenModel_978_> = repository.search(params)
}

abstract class GenMapper_978_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_978_ : GenMapper_978_<GenModel_978_, String>() {
    override fun map(input: GenModel_978_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_978_ : GenMapper_978_<String, GenModel_978_>() {
    override fun map(input: String): GenModel_978_ {
        val parts = input.split(":")
        return GenModel_978_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_978_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_978_,
    private val saveUseCase: GenSaveUseCase_978_,
    private val deleteUseCase: GenDeleteUseCase_978_,
    private val searchUseCase: GenSearchUseCase_978_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_978_>(GenState_978_.Idle)
    val state: StateFlow<GenState_978_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_978_) {
        when (event) {
            is GenEvent_978_.Load -> loadAll()
            is GenEvent_978_.Update -> save(event.model)
            is GenEvent_978_.Delete -> delete(event.id)
            is GenEvent_978_.Refresh -> loadAll()
            is GenEvent_978_.Search -> search(event.query)
            is GenEvent_978_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_978_.Loading; _state.value = GenState_978_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_978_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_978_.Success(searchUseCase(query)) } }
}
