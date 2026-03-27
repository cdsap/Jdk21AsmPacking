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

data class GenModel_425_(
    val id: Long = 0L,
    val name: String = "",
    val value: Double = 0.0,
    val active: Boolean = false,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

sealed class GenEvent_425_ {
    data class Load(val id: Long) : GenEvent_425_()
    data class Update(val model: GenModel_425_) : GenEvent_425_()
    data class Delete(val id: Long) : GenEvent_425_()
    data object Refresh : GenEvent_425_()
    data class Search(val query: String) : GenEvent_425_()
    data class Filter(val predicate: String) : GenEvent_425_()
}

sealed class GenState_425_ {
    data object Idle : GenState_425_()
    data object Loading : GenState_425_()
    data class Success(val items: List<GenModel_425_>) : GenState_425_()
    data class Error(val message: String, val cause: Throwable? = null) : GenState_425_()
    data class Partial(val items: List<GenModel_425_>, val hasMore: Boolean) : GenState_425_()
}

interface GenRepository_425_ {
    suspend fun getAll(): List<GenModel_425_>
    suspend fun getById(id: Long): GenModel_425_?
    suspend fun save(model: GenModel_425_): GenModel_425_
    suspend fun delete(id: Long): Boolean
    suspend fun search(query: String): List<GenModel_425_>
}

@Singleton
class GenRepositoryImpl_425_ @Inject constructor() : GenRepository_425_ {
    private val store = mutableMapOf<Long, GenModel_425_>()
    override suspend fun getAll(): List<GenModel_425_> = store.values.toList()
    override suspend fun getById(id: Long): GenModel_425_? = store[id]
    override suspend fun save(model: GenModel_425_): GenModel_425_ { store[model.id] = model; return model }
    override suspend fun delete(id: Long): Boolean = store.remove(id) != null
    override suspend fun search(query: String): List<GenModel_425_> = store.values.filter { it.name.contains(query) }
}

interface GenUseCase_425_<in P, out R> {
    suspend operator fun invoke(params: P): R
}

class GenGetAllUseCase_425_ @Inject constructor(
    private val repository: GenRepositoryImpl_425_
) : GenUseCase_425_<Unit, List<GenModel_425_>> {
    override suspend fun invoke(params: Unit): List<GenModel_425_> = repository.getAll()
}

class GenSaveUseCase_425_ @Inject constructor(
    private val repository: GenRepositoryImpl_425_
) : GenUseCase_425_<GenModel_425_, GenModel_425_> {
    override suspend fun invoke(params: GenModel_425_): GenModel_425_ = repository.save(params)
}

class GenDeleteUseCase_425_ @Inject constructor(
    private val repository: GenRepositoryImpl_425_
) : GenUseCase_425_<Long, Boolean> {
    override suspend fun invoke(params: Long): Boolean = repository.delete(params)
}

class GenSearchUseCase_425_ @Inject constructor(
    private val repository: GenRepositoryImpl_425_
) : GenUseCase_425_<String, List<GenModel_425_>> {
    override suspend fun invoke(params: String): List<GenModel_425_> = repository.search(params)
}

abstract class GenMapper_425_<in I, out O> {
    abstract fun map(input: I): O
    fun mapList(input: List<I>): List<O> = input.map { map(it) }
}

class GenModelToStringMapper_425_ : GenMapper_425_<GenModel_425_, String>() {
    override fun map(input: GenModel_425_): String = "${input.id}:${input.name}"
}

class GenStringToModelMapper_425_ : GenMapper_425_<String, GenModel_425_>() {
    override fun map(input: String): GenModel_425_ {
        val parts = input.split(":")
        return GenModel_425_(id = parts[0].toLongOrNull() ?: 0L, name = parts.getOrElse(1) { "" })
    }
}

@HiltViewModel
class GenViewModel_425_ @Inject constructor(
    private val getAllUseCase: GenGetAllUseCase_425_,
    private val saveUseCase: GenSaveUseCase_425_,
    private val deleteUseCase: GenDeleteUseCase_425_,
    private val searchUseCase: GenSearchUseCase_425_
) : ViewModel() {
    private val _state = MutableStateFlow<GenState_425_>(GenState_425_.Idle)
    val state: StateFlow<GenState_425_> = _state.asStateFlow()

    fun onEvent(event: GenEvent_425_) {
        when (event) {
            is GenEvent_425_.Load -> loadAll()
            is GenEvent_425_.Update -> save(event.model)
            is GenEvent_425_.Delete -> delete(event.id)
            is GenEvent_425_.Refresh -> loadAll()
            is GenEvent_425_.Search -> search(event.query)
            is GenEvent_425_.Filter -> loadAll()
        }
    }

    private fun loadAll() { viewModelScope.launch { _state.value = GenState_425_.Loading; _state.value = GenState_425_.Success(getAllUseCase(Unit)) } }
    private fun save(model: GenModel_425_) { viewModelScope.launch { saveUseCase(model); loadAll() } }
    private fun delete(id: Long) { viewModelScope.launch { deleteUseCase(id); loadAll() } }
    private fun search(query: String) { viewModelScope.launch { _state.value = GenState_425_.Success(searchUseCase(query)) } }
}
