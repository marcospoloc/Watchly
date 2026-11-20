# 0002. Cancelación de búsquedas obsoletas con `flatMapLatest`

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

La búsqueda se lanza mientras el usuario escribe. Si una petición lenta ("lost") termina después de una más reciente ("house"), sus resultados no pueden llegar a la UI. Además, TVMaze limita a 20 peticiones cada 10 segundos.

## Decisión

`SearchViewModel` construye su estado a partir del texto de búsqueda con un único pipeline de `Flow`:

```kotlin
_query
    .map { it.trim() }
    .distinctUntilChanged()
    .combine(retryRequests) { query, _ -> query }
    .flatMapLatest(::searchStates)
    .stateIn(viewModelScope, SharingStarted.Lazily, SearchUiState.Idle)
```

`searchStates(query)` devuelve `Idle` al instante si la query está vacía; si no, espera `SEARCH_DEBOUNCE` (300 ms) con `delay`, emite `Loading` y después el resultado.

- `flatMapLatest` cancela la corrutina de la búsqueda anterior en cuanto cambia el texto, tanto si aún está en la espera del debounce como si ya está haciendo la petición. Como las funciones `suspend` de Retrofit son cancelables, también se cancela la petición HTTP en curso.
- El debounce va dentro de `flatMapLatest` y no como `debounce()` delante. La primera versión lo tenía delante y la búsqueda anterior seguía viva durante los 300 ms de debounce: si respondía en ese intervalo, sus resultados aparecían junto al texto nuevo, o con el campo vacío al borrarlo.
- `safeApiCall` (`data/remote/SafeApiCall.kt`) relanza `CancellationException` en vez de convertirla en error.
- El reintento usa un contador (`retryRequests`) en lugar de reenviar el texto, para que `distinctUntilChanged` no lo descarte.

## Consecuencias

- Es imposible mostrar resultados de una búsqueda obsoleta. Lo cubren tests de `SearchViewModelTest`, que fallan si se sustituye `flatMapLatest` por `flatMapConcat` o si se vuelve a poner el debounce delante.
- Borrar el campo vuelve a `Idle` al momento, sin esperar el debounce.
- El estado se comparte con `SharingStarted.Lazily`: mientras hay un detalle abierto la pantalla de búsqueda deja de recoger, y con `WhileSubscribed` el pipeline se reiniciaba al volver, repetía la búsqueda, mostraba otra vez el esqueleto y la lista volvía arriba. La contrapartida es que una petición en curso termina aunque la app pase a segundo plano.
- El reintento también espera los 300 ms del debounce, y volver a una query que se acababa de abandonar ("lost" → "lo" → "lost") lanza otra búsqueda; la caché en memoria responde sin red si la primera había terminado.
- El debounce y `distinctUntilChanged` reducen las peticiones y mantienen la app dentro del límite de TVMaze.
- Usa `flatMapLatest`, marcada como experimental en coroutines (`ExperimentalCoroutinesApi`), declarada explícitamente con `@OptIn`.
