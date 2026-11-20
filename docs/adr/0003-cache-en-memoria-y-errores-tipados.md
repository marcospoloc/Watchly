# 0003. Caché en memoria con TTL y errores tipados con `Outcome`

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

Al volver de un detalle o repetir una búsqueda, la app no debería volver a pedir los mismos datos. Además, las capas superiores no deben recibir excepciones de infraestructura (`IOException`, `HttpException`).

## Decisión

**Caché**
- Interfaz `Cache<K, V>` con una implementación `InMemoryCache` (TTL de 3 minutos, `DEFAULT_CACHE_TTL`, y como mucho 100 entradas con desalojo LRU), protegida con `Mutex`.
- El tiempo se inyecta como `kotlin.time.TimeSource`: el reloj monotónico en producción y `TestTimeSource` en los tests.
- `ShowRepositoryImpl` es *cache-first*. La clave de búsqueda se normaliza con `trim().lowercase()` y el detalle se cachea por id. Los errores no se cachean.
- El repositorio es `@Singleton` en `AppModule`, así que la caché vive lo que vive el proceso.

**Errores**
- `Outcome<T> = Success(value) | Failure(DomainError)`, definido en `:domain`.
- `DomainError` es un `sealed interface` con `Network`, `NotFound`, `Server` y `Unknown`. `safeApiCall` traduce las excepciones a estos casos.

## Consecuencias

- La expiración se prueba de forma determinista, sin `sleep`, y no le afecta que el usuario cambie la hora del dispositivo.
- La UI decide el mensaje según un tipo cerrado de errores, con un `when` exhaustivo sobre `DomainError`.
- Limitación aceptada: no hay modo offline ni persistencia entre arranques. La siguiente evolución natural es Room con *stale-while-revalidate* detrás de la misma interfaz `Cache`.
- Limitación aceptada: dos búsquedas idénticas simultáneas con la caché vacía pueden hacer dos peticiones. El debounce lo evita en la práctica.
