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

data class GenModel_1095_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_1095_ {
    data class Load(val id: Long) : GenEvent_1095_()
    data class Update(val model: GenModel_1095_) : GenEvent_1095_()
    data class Delete(val id: Long) : GenEvent_1095_()
    data object Refresh : GenEvent_1095_()
    data class Search(val query: String) : GenEvent_1095_()
    data class Filter(val predicate: String) : GenEvent_1095_()
}

sealed class GenState_1095_ {
    data object Idle : GenState_1095_()
    data object Loading : GenState_1095_()
    data class Success(val items: List<GenModel_1095_>) : GenState_1095_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_1095_()
    data class Partial(val items: List<GenModel_1095_>, val hasMore: Boolean) : GenState_1095_()
}

interface GenRepository_1095_ {
    suspend fun getAll(): List<GenModel_1095_>
    suspend fun getById(id: Long): GenModel_1095_?
    suspend fun save(model: GenModel_1095_): GenModel_1095_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_1095_>
}

@Singleton
class GenRepositoryImpl_1095_ @Inject constructor() : GenRepository_1095_ {
    private val store = mutableMapOf<Long, GenModel_1095_>()
    override suspend fun getAll(): List<GenModel_1095_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_1095_? = store[id]
    override suspend fun save(model: GenModel_1095_): GenModel_1095_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_1095_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_1095_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_1095_ @Inject constructor(
    private val repository: GenRepositoryImpl_1095_
) : GenUseCase_1095_<Unit, List<GenModel_1095_>> {
    override suspend fun invoke(params: Unit): List<GenModel_1095_> = repository.getAll()
}

class GenSaveUseCase_1095_ @Inject constructor(
    private val repository: GenRepositoryImpl_1095_
) : GenUseCase_1095_<GenModel_1095_, GenModel_1095_> {
    override suspend fun invoke(params: GenModel_1095_): GenModel_1095_ = repository.save(params)
}

class GenDeleteUseCase_1095_ @Inject constructor(
    private val repository: GenRepositoryImpl_1095_
) : GenUseCase_1095_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_1095_ @Inject constructor(
    private val repository: GenRepositoryImpl_1095_
) : GenUseCase_1095_<String, List<GenModel_1095_>> {
    override suspend fun invoke(params: String): List<GenModel_1095_> = repository.search(params)
}

abstract class GenMapper_1095_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_1095_ : GenMapper_1095_<GenModel_1095_, String>() {
    override fun map(input: GenModel_1095_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_1095_ : GenMapper_1095_<String, GenModel_1095_>() {
    override fun map(input: String): GenModel_1095_ {
        val parts = input.split(":")
        return GenModel_1095_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_1095_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_1095_,
    private val saveUseCase: GenSaveUseCase_1095_,
    private val deleteUseCase: GenDeleteUseCase_1095_,
    private val searchUseCase: GenSearchUseCase_1095_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_1095_>(GenState_1095_.Idle)
    val state: StateFlow<GenState_1095_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_1095_) {
        when (event) {
            is GenEvent_1095_.Load -> loadAll()
            is GenEvent_1095_.Update -> save(event.model)
            is GenEvent_1095_.Delete -> delete(event.id)
            is GenEvent_1095_.Refresh -> loadAll()
            is GenEvent_1095_.Search -> search(event.query)
            is GenEvent_1095_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_1095_.Loading; _state.value = GenState_1095_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_1095_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_1095_.Success(searchUseCase(query)) } }
}
