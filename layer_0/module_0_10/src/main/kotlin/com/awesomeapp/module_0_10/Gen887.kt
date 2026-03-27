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

data class GenModel_887_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_887_ {
    data class Load(val id: Long) : GenEvent_887_()
    data class Update(val model: GenModel_887_) : GenEvent_887_()
    data class Delete(val id: Long) : GenEvent_887_()
    data object Refresh : GenEvent_887_()
    data class Search(val query: String) : GenEvent_887_()
    data class Filter(val predicate: String) : GenEvent_887_()
}

sealed class GenState_887_ {
    data object Idle : GenState_887_()
    data object Loading : GenState_887_()
    data class Success(val items: List<GenModel_887_>) : GenState_887_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_887_()
    data class Partial(val items: List<GenModel_887_>, val hasMore: Boolean) : GenState_887_()
}

interface GenRepository_887_ {
    suspend fun getAll(): List<GenModel_887_>
    suspend fun getById(id: Long): GenModel_887_?
    suspend fun save(model: GenModel_887_): GenModel_887_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_887_>
}

@Singleton
class GenRepositoryImpl_887_ @Inject constructor() : GenRepository_887_ {
    private val store = mutableMapOf<Long, GenModel_887_>()
    override suspend fun getAll(): List<GenModel_887_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_887_? = store[id]
    override suspend fun save(model: GenModel_887_): GenModel_887_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_887_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_887_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_887_ @Inject constructor(
    private val repository: GenRepositoryImpl_887_
) : GenUseCase_887_<Unit, List<GenModel_887_>> {
    override suspend fun invoke(params: Unit): List<GenModel_887_> = repository.getAll()
}

class GenSaveUseCase_887_ @Inject constructor(
    private val repository: GenRepositoryImpl_887_
) : GenUseCase_887_<GenModel_887_, GenModel_887_> {
    override suspend fun invoke(params: GenModel_887_): GenModel_887_ = repository.save(params)
}

class GenDeleteUseCase_887_ @Inject constructor(
    private val repository: GenRepositoryImpl_887_
) : GenUseCase_887_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_887_ @Inject constructor(
    private val repository: GenRepositoryImpl_887_
) : GenUseCase_887_<String, List<GenModel_887_>> {
    override suspend fun invoke(params: String): List<GenModel_887_> = repository.search(params)
}

abstract class GenMapper_887_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_887_ : GenMapper_887_<GenModel_887_, String>() {
    override fun map(input: GenModel_887_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_887_ : GenMapper_887_<String, GenModel_887_>() {
    override fun map(input: String): GenModel_887_ {
        val parts = input.split(":")
        return GenModel_887_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_887_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_887_,
    private val saveUseCase: GenSaveUseCase_887_,
    private val deleteUseCase: GenDeleteUseCase_887_,
    private val searchUseCase: GenSearchUseCase_887_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_887_>(GenState_887_.Idle)
    val state: StateFlow<GenState_887_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_887_) {
        when (event) {
            is GenEvent_887_.Load -> loadAll()
            is GenEvent_887_.Update -> save(event.model)
            is GenEvent_887_.Delete -> delete(event.id)
            is GenEvent_887_.Refresh -> loadAll()
            is GenEvent_887_.Search -> search(event.query)
            is GenEvent_887_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_887_.Loading; _state.value = GenState_887_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_887_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_887_.Success(searchUseCase(query)) } }
}
