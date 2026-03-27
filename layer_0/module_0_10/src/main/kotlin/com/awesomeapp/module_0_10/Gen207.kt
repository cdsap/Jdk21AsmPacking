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

data class GenModel_207_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_207_ {
    data class Load(val id: Long) : GenEvent_207_()
    data class Update(val model: GenModel_207_) : GenEvent_207_()
    data class Delete(val id: Long) : GenEvent_207_()
    data object Refresh : GenEvent_207_()
    data class Search(val query: String) : GenEvent_207_()
    data class Filter(val predicate: String) : GenEvent_207_()
}

sealed class GenState_207_ {
    data object Idle : GenState_207_()
    data object Loading : GenState_207_()
    data class Success(val items: List<GenModel_207_>) : GenState_207_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_207_()
    data class Partial(val items: List<GenModel_207_>, val hasMore: Boolean) : GenState_207_()
}

interface GenRepository_207_ {
    suspend fun getAll(): List<GenModel_207_>
    suspend fun getById(id: Long): GenModel_207_?
    suspend fun save(model: GenModel_207_): GenModel_207_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_207_>
}

@Singleton
class GenRepositoryImpl_207_ @Inject constructor() : GenRepository_207_ {
    private val store = mutableMapOf<Long, GenModel_207_>()
    override suspend fun getAll(): List<GenModel_207_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_207_? = store[id]
    override suspend fun save(model: GenModel_207_): GenModel_207_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_207_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_207_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_207_ @Inject constructor(
    private val repository: GenRepositoryImpl_207_
) : GenUseCase_207_<Unit, List<GenModel_207_>> {
    override suspend fun invoke(params: Unit): List<GenModel_207_> = repository.getAll()
}

class GenSaveUseCase_207_ @Inject constructor(
    private val repository: GenRepositoryImpl_207_
) : GenUseCase_207_<GenModel_207_, GenModel_207_> {
    override suspend fun invoke(params: GenModel_207_): GenModel_207_ = repository.save(params)
}

class GenDeleteUseCase_207_ @Inject constructor(
    private val repository: GenRepositoryImpl_207_
) : GenUseCase_207_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_207_ @Inject constructor(
    private val repository: GenRepositoryImpl_207_
) : GenUseCase_207_<String, List<GenModel_207_>> {
    override suspend fun invoke(params: String): List<GenModel_207_> = repository.search(params)
}

abstract class GenMapper_207_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_207_ : GenMapper_207_<GenModel_207_, String>() {
    override fun map(input: GenModel_207_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_207_ : GenMapper_207_<String, GenModel_207_>() {
    override fun map(input: String): GenModel_207_ {
        val parts = input.split(":")
        return GenModel_207_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_207_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_207_,
    private val saveUseCase: GenSaveUseCase_207_,
    private val deleteUseCase: GenDeleteUseCase_207_,
    private val searchUseCase: GenSearchUseCase_207_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_207_>(GenState_207_.Idle)
    val state: StateFlow<GenState_207_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_207_) {
        when (event) {
            is GenEvent_207_.Load -> loadAll()
            is GenEvent_207_.Update -> save(event.model)
            is GenEvent_207_.Delete -> delete(event.id)
            is GenEvent_207_.Refresh -> loadAll()
            is GenEvent_207_.Search -> search(event.query)
            is GenEvent_207_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_207_.Loading; _state.value = GenState_207_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_207_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_207_.Success(searchUseCase(query)) } }
}
