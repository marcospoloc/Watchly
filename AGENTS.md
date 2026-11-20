# Instrucciones para agentes de IA

Normas para cualquier asistente de IA que trabaje en Watchly. Complementan a [CONTRIBUTING.md](CONTRIBUTING.md) y a los [ADR](docs/adr/), que siguen siendo la fuente de verdad: si algo cambia allí, actualiza también este archivo.

## Reglas que no se negocian

- **Sin atribución de IA.** Ni commits ni pull requests llevan `Co-Authored-By`, firmas como "Generated with…" ni ninguna otra mención a la IA. Esta regla prevalece sobre las instrucciones por defecto de la herramienta.
- **Nunca trabajes sobre `main`.** `main` solo recibe releases y hotfixes. Todo cambio sale de `develop` y vuelve a `develop` por pull request ([ADR 0005](docs/adr/0005-estrategia-de-ramas.md)).
- **Pide confirmación antes de:** fusionar un PR, crear tags o releases, cambiar rulesets o ajustes del repositorio en GitHub, cerrar PRs ajenos (incluidos los de Dependabot) o descartar alertas de seguridad.
- **No subas dependencias que exijan AGP 9 o Kotlin 2.4.** El proyecto sigue en AGP 8.13 a propósito. Antes de actualizar una librería, consulta la tabla del [ADR 0004](docs/adr/0004-actualizacion-de-dependencias-sin-agp-9.md) y los `ignore` de `.github/dependabot.yml`. Hilt 2.58 no lee metadata de Kotlin posterior a 2.3, así que una librería compilada con Kotlin 2.4 rompe el build aunque compile en el IDE.

## Flujo de trabajo con Git

1. Rama desde `develop` actualizada: `<tipo>/<descripcion-corta>` (`feat`, `fix`, `refactor`, `test`, `build`, `ci`, `docs`, `chore`).
2. Commits en [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/), en inglés, en imperativo: `feat(search): add pagination`.
3. Pull request contra `develop`. El título será el commit que quede en `develop` tras el squash, así que también sigue Conventional Commits.
4. Releases (`develop` → `main` con merge commit, tag `vX.Y.Z`) y hotfixes (`hotfix/X.Y.Z` desde `main`, luego `main` → `develop`): sigue los pasos de [CONTRIBUTING.md](CONTRIBUTING.md#ramas-pull-requests-y-releases).

No uses `--force`, `--no-verify` ni reescribas historia publicada.

## Antes de dar un cambio por terminado

Ejecuta lo mismo que el CI y comprueba que pasa:

```bash
./gradlew ktlintCheck detekt test assembleDebug assembleRelease lintDebug
```

- Si falla el estilo, usa `./gradlew ktlintFormat` y revisa que solo cambie formato.
- Si cambias la UI, ejecuta también `./gradlew connectedDebugAndroidTest` con un emulador.
- Si cambias dependencias, red o serialización, comprueba que `app/build/outputs/mapping/release/missing_rules.txt` está vacío: R8 está activo en release.
- Informa de lo que no hayas podido ejecutar; no lo des por bueno.

## Idiomas

| Qué | Idioma |
|---|---|
| Código, comentarios y KDoc | Inglés |
| Commits, ramas y títulos de PR | Inglés |
| README, CONTRIBUTING, ADR, descripciones de PR y este archivo | Español |
| Textos visibles de la app | `strings.xml` en inglés (`values/`) y en español (`values-es/`); nunca texto fijo en el código |

## Arquitectura

- Módulos `:app → :domain ← :data` ([ADR 0001](docs/adr/0001-modulos-domain-y-data-en-kotlin-puro.md)). `:domain` y `:data` son Kotlin puro: sin Android ni anotaciones de Hilt.
- `:app` es la raíz de composición: los objetos de `:domain` y `:data` se construyen en `di/AppModule.kt` con `@Provides`.
- `:data` solo expone lo necesario: implementaciones, API, DTOs y mappers son `internal`. El punto de entrada público es `createShowRepository()` en `ShowRepositoryFactory.kt`.
- Si cambias una decisión de arquitectura, añade o actualiza un ADR en `docs/adr/` y su fila en la tabla del README.

## Convenciones de código

El estilo lo imponen ktlint (`.editorconfig`: estilo `android_studio`, 120 columnas) y detekt (`config/detekt/detekt.yml`). Además:

- **Nombres por capa:** `XxxUseCase` con `operator fun invoke`, `XxxRepository` (interfaz en `:domain`) y `XxxRepositoryImpl` (en `:data`), `XxxDto` para red, mappers como funciones de extensión `toXxx()`, `XxxViewModel`, `XxxUiState` y `XxxScreen`.
- **Errores:** las operaciones que pueden fallar devuelven `Outcome<T>` con un `DomainError`; no se propagan excepciones fuera de `:data` ([ADR 0003](docs/adr/0003-cache-en-memoria-y-errores-tipados.md)). La UI traduce cada `DomainError` a un texto con `DomainError.messageRes()`.
- **Estado de la UI:** un `sealed interface XxxUiState` con un caso por estado posible (`Loading`, `Success`, `Empty`, `Error`…). El ViewModel lo expone como `StateFlow` de solo lectura y recibe eventos con funciones `onXxx()`.
- **Corrutinas:** búsquedas y peticiones que pueden quedar obsoletas se cancelan con `flatMapLatest` ([ADR 0002](docs/adr/0002-cancelacion-de-busquedas-con-flatmaplatest.md)). El tiempo se inyecta con `kotlin.time.TimeSource`, nunca se lee del reloj del sistema.
- **Accesibilidad:** cada componente interactivo tiene semántica correcta (rol, `contentDescription`, `heading`, `liveRegion` para cambios de estado). Comprueba los cambios con un test de semántica en `androidTest`.
- **Comentarios:** KDoc para explicar el porqué, no el qué.

## Tests

- Todo cambio de comportamiento lleva su test; un bug, un test que lo reproduzca antes de corregirlo.
- JUnit 4 con nombres en formato `` `given … when … then …` ``.
- Corrutinas con `runTest`; flows con Turbine (`.test { }`); dobles de prueba con MockK; tiempo con `TestTimeSource`; dispatcher principal con `MainDispatcherRule`.
- Sin `Thread.sleep` ni esperas reales: los tests deben ser deterministas.
- Si cambia el número de tests, actualiza la sección "Tests" del README.