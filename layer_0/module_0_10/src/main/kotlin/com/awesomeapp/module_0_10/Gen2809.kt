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

data class GenModel_2809_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2809_ {
    data class Load(val id: Long) : GenEvent_2809_()
    data class Update(val model: GenModel_2809_) : GenEvent_2809_()
    data class Delete(val id: Long) : GenEvent_2809_()
    data object Refresh : GenEvent_2809_()
    data class Search(val query: String) : GenEvent_2809_()
    data class Filter(val predicate: String) : GenEvent_2809_()
}

sealed class GenState_2809_ {
    data object Idle : GenState_2809_()
    data object Loading : GenState_2809_()
    data class Success(val items: List<GenModel_2809_>) : GenState_2809_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2809_()
    data class Partial(val items: List<GenModel_2809_>, val hasMore: Boolean) : GenState_2809_()
}

interface GenRepository_2809_ {
    suspend fun getAll(): List<GenModel_2809_>
    suspend fun getById(id: Long): GenModel_2809_?
    suspend fun save(model: GenModel_2809_): GenModel_2809_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2809_>
}

@Singleton
class GenRepositoryImpl_2809_ @Inject constructor() : GenRepository_2809_ {
    private val store = mutableMapOf<Long, GenModel_2809_>()
    override suspend fun getAll(): List<GenModel_2809_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2809_? = store[id]
    override suspend fun save(model: GenModel_2809_): GenModel_2809_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2809_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2809_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2809_ @Inject constructor(
    private val repository: GenRepositoryImpl_2809_
) : GenUseCase_2809_<Unit, List<GenModel_2809_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2809_> = repository.getAll()
}

class GenSaveUseCase_2809_ @Inject constructor(
    private val repository: GenRepositoryImpl_2809_
) : GenUseCase_2809_<GenModel_2809_, GenModel_2809_> {
    override suspend fun invoke(params: GenModel_2809_): GenModel_2809_ = repository.save(params)
}

class GenDeleteUseCase_2809_ @Inject constructor(
    private val repository: GenRepositoryImpl_2809_
) : GenUseCase_2809_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2809_ @Inject constructor(
    private val repository: GenRepositoryImpl_2809_
) : GenUseCase_2809_<String, List<GenModel_2809_>> {
    override suspend fun invoke(params: String): List<GenModel_2809_> = repository.search(params)
}

abstract class GenMapper_2809_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2809_ : GenMapper_2809_<GenModel_2809_, String>() {
    override fun map(input: GenModel_2809_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2809_ : GenMapper_2809_<String, GenModel_2809_>() {
    override fun map(input: String): GenModel_2809_ {
        val parts = input.split(":")
        return GenModel_2809_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2809_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2809_,
    private val saveUseCase: GenSaveUseCase_2809_,
    private val deleteUseCase: GenDeleteUseCase_2809_,
    private val searchUseCase: GenSearchUseCase_2809_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2809_>(GenState_2809_.Idle)
    val state: StateFlow<GenState_2809_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2809_) {
        when (event) {
            is GenEvent_2809_.Load -> loadAll()
            is GenEvent_2809_.Update -> save(event.model)
            is GenEvent_2809_.Delete -> delete(event.id)
            is GenEvent_2809_.Refresh -> loadAll()
            is GenEvent_2809_.Search -> search(event.query)
            is GenEvent_2809_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2809_.Loading; _state.value = GenState_2809_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2809_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2809_.Success(searchUseCase(query)) } }
}
