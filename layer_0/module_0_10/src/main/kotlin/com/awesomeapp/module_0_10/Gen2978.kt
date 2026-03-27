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

data class GenModel_2978_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2978_ {
    data class Load(val id: Long) : GenEvent_2978_()
    data class Update(val model: GenModel_2978_) : GenEvent_2978_()
    data class Delete(val id: Long) : GenEvent_2978_()
    data object Refresh : GenEvent_2978_()
    data class Search(val query: String) : GenEvent_2978_()
    data class Filter(val predicate: String) : GenEvent_2978_()
}

sealed class GenState_2978_ {
    data object Idle : GenState_2978_()
    data object Loading : GenState_2978_()
    data class Success(val items: List<GenModel_2978_>) : GenState_2978_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2978_()
    data class Partial(val items: List<GenModel_2978_>, val hasMore: Boolean) : GenState_2978_()
}

interface GenRepository_2978_ {
    suspend fun getAll(): List<GenModel_2978_>
    suspend fun getById(id: Long): GenModel_2978_?
    suspend fun save(model: GenModel_2978_): GenModel_2978_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2978_>
}

@Singleton
class GenRepositoryImpl_2978_ @Inject constructor() : GenRepository_2978_ {
    private val store = mutableMapOf<Long, GenModel_2978_>()
    override suspend fun getAll(): List<GenModel_2978_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2978_? = store[id]
    override suspend fun save(model: GenModel_2978_): GenModel_2978_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2978_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2978_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2978_ @Inject constructor(
    private val repository: GenRepositoryImpl_2978_
) : GenUseCase_2978_<Unit, List<GenModel_2978_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2978_> = repository.getAll()
}

class GenSaveUseCase_2978_ @Inject constructor(
    private val repository: GenRepositoryImpl_2978_
) : GenUseCase_2978_<GenModel_2978_, GenModel_2978_> {
    override suspend fun invoke(params: GenModel_2978_): GenModel_2978_ = repository.save(params)
}

class GenDeleteUseCase_2978_ @Inject constructor(
    private val repository: GenRepositoryImpl_2978_
) : GenUseCase_2978_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2978_ @Inject constructor(
    private val repository: GenRepositoryImpl_2978_
) : GenUseCase_2978_<String, List<GenModel_2978_>> {
    override suspend fun invoke(params: String): List<GenModel_2978_> = repository.search(params)
}

abstract class GenMapper_2978_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2978_ : GenMapper_2978_<GenModel_2978_, String>() {
    override fun map(input: GenModel_2978_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2978_ : GenMapper_2978_<String, GenModel_2978_>() {
    override fun map(input: String): GenModel_2978_ {
        val parts = input.split(":")
        return GenModel_2978_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2978_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2978_,
    private val saveUseCase: GenSaveUseCase_2978_,
    private val deleteUseCase: GenDeleteUseCase_2978_,
    private val searchUseCase: GenSearchUseCase_2978_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2978_>(GenState_2978_.Idle)
    val state: StateFlow<GenState_2978_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2978_) {
        when (event) {
            is GenEvent_2978_.Load -> loadAll()
            is GenEvent_2978_.Update -> save(event.model)
            is GenEvent_2978_.Delete -> delete(event.id)
            is GenEvent_2978_.Refresh -> loadAll()
            is GenEvent_2978_.Search -> search(event.query)
            is GenEvent_2978_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2978_.Loading; _state.value = GenState_2978_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2978_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2978_.Success(searchUseCase(query)) } }
}
