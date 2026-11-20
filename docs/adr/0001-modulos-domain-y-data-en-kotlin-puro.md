# 0001. Módulos `:domain` y `:data` en Kotlin puro, ensamblados en `:app`

- **Estado:** aceptada; la presentación pasó a su propio módulo en el [ADR 0006](0006-modulo-presentation.md)
- **Fecha:** 2026-11

## Contexto

La app separa presentación, dominio y acceso a datos. Esa separación puede hacerse con paquetes dentro de un único módulo, pero entonces la regla de dependencia ("el dominio no conoce Retrofit ni Android") depende de la disciplina del equipo y no la comprueba nadie.

## Decisión

- Tres módulos Gradle: `:app` (Android), `:domain` y `:data` (plugin `kotlin.jvm`, sin Android).
- Dirección de dependencias `:app → :domain ← :data`. `:data` implementa `ShowRepository`, que define `:domain`.
- Ni `:domain` ni `:data` dependen de Hilt. El grafo se ensambla en `app/di/AppModule.kt`, que hace de *composition root*.
- `:data` expone una única función pública, `createShowRepository(...)` (`ShowRepositoryFactory.kt`). La API de Retrofit, los DTOs, los mappers y `ShowRepositoryImpl` son `internal`.

## Consecuencias

- El compilador hace cumplir la arquitectura: `:domain` no puede importar Retrofit porque no está en su classpath.
- Los tests de `:domain` y `:data` son JVM puros, rápidos y no necesitan Robolectric ni emulador.
- `:app` no puede depender por accidente de los DTOs ni de la implementación del repositorio.
- Coste: más ficheros de build y cableado manual en `AppModule` en lugar de anotaciones `@Inject` en `:data`. Con dos casos de uso, el coste es pequeño.
