# 0002. Cancelación de búsquedas obsoletas con `flatMapLatest`

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

La búsqueda se lanza mientras el usuario escribe. Si una petición lenta ("lost") termina después de una más reciente ("house"), sus resultados no pueden llegar a la UI. Además, TVMaze limita a 20 peticiones cada 10 segundos.

## Decisión

`SearchViewModel` construye su estado a partir del texto de búsqueda con un único pipeline de `Flow`:

```kotlin
_query
    .debounce(SEARCH_DEBOUNCE)          // 300 ms
    .map { it.trim() }
    .distinctUntilChanged()
    .combine(retryRequests) { query, _ -> query }
    .flatMapLatest(::searchStates)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState.Idle)
```

- `flatMapLatest` cancela la corrutina de la búsqueda anterior cuando llega una nueva. Como las funciones `suspend` de Retrofit son cancelables, también se cancela la petición HTTP en curso.
- `safeApiCall` (`data/remote/SafeApiCall.kt`) relanza `CancellationException` en vez de convertirla en error.
- El reintento usa un contador (`retryRequests`) en lugar de reenviar el texto, para que `distinctUntilChanged` no lo descarte.

## Consecuencias

- Es imposible mostrar resultados de una búsqueda obsoleta, y lo cubre un test: `SearchViewModelTest` falla si se sustituye `flatMapLatest` por `flatMapConcat`.
- El debounce y `distinctUntilChanged` reducen las peticiones y mantienen la app dentro del límite de TVMaze.
- Usa APIs marcadas como experimentales en coroutines (`FlowPreview`, `ExperimentalCoroutinesApi`), declaradas explícitamente con `@OptIn`.
