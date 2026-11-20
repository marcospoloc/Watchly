# 0006. Módulo `:presentation` separado de `:app`

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

Con el [ADR 0001](0001-modulos-domain-y-data-en-kotlin-puro.md), `:app` contenía dos cosas: la composición de la app (`Application`, `MainActivity`, el módulo Hilt, el manifest y la firma) y toda la presentación (pantallas, ViewModels, navegación y textos). Como `:app` depende de `:data` para ensamblar el repositorio, nada impedía que una pantalla o un ViewModel importase `createShowRepository()` o cualquier otra API pública de `:data`. La regla "la UI solo habla con el dominio" dependía de la disciplina, igual que la que el ADR 0001 quiso evitar para `:domain`.

## Decisión

- Nuevo módulo Android library `:presentation` (namespace `com.marcospolo.watchly.presentation`) con pantallas Compose, ViewModels, `UiState`, componentes, tema, navegación, textos en inglés y español, y sus tests.
- `:presentation` depende solo de `:domain`. La dirección queda `:app → :presentation → :domain ← :data`, y `:app → :data` solo para el *composition root*.
- `:app` queda como raíz de composición: `WatchlyApplication`, `MainActivity`, `di/AppModule.kt`, manifest, icono, tema de la ventana, R8 y firma.
- Los ViewModels siguen usando `@HiltViewModel`; `:presentation` aplica el plugin de Hilt y KSP para generarlos, y el grafo se ensambla en `:app`.

## Consecuencias

- El compilador impide que la UI use `:data`: un import de `com.marcospolo.watchly.data` en `:presentation` no compila.
- Las capas Data, Domain y Presentation son módulos Gradle, no carpetas.
- Los tests de ViewModel y de accesibilidad viven en `:presentation`. Como una library no tiene `targetSdk`, se fija en `testOptions` para que Android no tape la actividad de test con el aviso de "app para una versión antigua".
- Coste: un módulo más que configurar, parte de las dependencias de Compose repetidas en `:app` y `:presentation`, y cada módulo tiene su propia clase `R`.
