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

data class GenModel_2417_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_2417_ {
    data class Load(val id: Long) : GenEvent_2417_()
    data class Update(val model: GenModel_2417_) : GenEvent_2417_()
    data class Delete(val id: Long) : GenEvent_2417_()
    data object Refresh : GenEvent_2417_()
    data class Search(val query: String) : GenEvent_2417_()
    data class Filter(val predicate: String) : GenEvent_2417_()
}

sealed class GenState_2417_ {
    data object Idle : GenState_2417_()
    data object Loading : GenState_2417_()
    data class Success(val items: List<GenModel_2417_>) : GenState_2417_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_2417_()
    data class Partial(val items: List<GenModel_2417_>, val hasMore: Boolean) : GenState_2417_()
}

interface GenRepository_2417_ {
    suspend fun getAll(): List<GenModel_2417_>
    suspend fun getById(id: Long): GenModel_2417_?
    suspend fun save(model: GenModel_2417_): GenModel_2417_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_2417_>
}

@Singleton
class GenRepositoryImpl_2417_ @Inject constructor() : GenRepository_2417_ {
    private val store = mutableMapOf<Long, GenModel_2417_>()
    override suspend fun getAll(): List<GenModel_2417_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_2417_? = store[id]
    override suspend fun save(model: GenModel_2417_): GenModel_2417_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_2417_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_2417_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_2417_ @Inject constructor(
    private val repository: GenRepositoryImpl_2417_
) : GenUseCase_2417_<Unit, List<GenModel_2417_>> {
    override suspend fun invoke(params: Unit): List<GenModel_2417_> = repository.getAll()
}

class GenSaveUseCase_2417_ @Inject constructor(
    private val repository: GenRepositoryImpl_2417_
) : GenUseCase_2417_<GenModel_2417_, GenModel_2417_> {
    override suspend fun invoke(params: GenModel_2417_): GenModel_2417_ = repository.save(params)
}

class GenDeleteUseCase_2417_ @Inject constructor(
    private val repository: GenRepositoryImpl_2417_
) : GenUseCase_2417_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_2417_ @Inject constructor(
    private val repository: GenRepositoryImpl_2417_
) : GenUseCase_2417_<String, List<GenModel_2417_>> {
    override suspend fun invoke(params: String): List<GenModel_2417_> = repository.search(params)
}

abstract class GenMapper_2417_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_2417_ : GenMapper_2417_<GenModel_2417_, String>() {
    override fun map(input: GenModel_2417_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_2417_ : GenMapper_2417_<String, GenModel_2417_>() {
    override fun map(input: String): GenModel_2417_ {
        val parts = input.split(":")
        return GenModel_2417_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_2417_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_2417_,
    private val saveUseCase: GenSaveUseCase_2417_,
    private val deleteUseCase: GenDeleteUseCase_2417_,
    private val searchUseCase: GenSearchUseCase_2417_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_2417_>(GenState_2417_.Idle)
    val state: StateFlow<GenState_2417_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_2417_) {
        when (event) {
            is GenEvent_2417_.Load -> loadAll()
            is GenEvent_2417_.Update -> save(event.model)
            is GenEvent_2417_.Delete -> delete(event.id)
            is GenEvent_2417_.Refresh -> loadAll()
            is GenEvent_2417_.Search -> search(event.query)
            is GenEvent_2417_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_2417_.Loading; _state.value = GenState_2417_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_2417_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_2417_.Success(searchUseCase(query)) } }
}
