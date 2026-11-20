# Cómo contribuir

## Preparar el entorno

- JDK 17 y Android SDK con la plataforma 36.
- Si no usas Android Studio, crea `local.properties` con `sdk.dir=/ruta/al/Android/sdk`.

## Antes de abrir un pull request

```bash
./gradlew ktlintFormat                      # corrige el estilo automáticamente
./gradlew ktlintCheck detekt test lintDebug # lo mismo que valida el CI
./gradlew assembleRelease                   # comprueba que R8 no rompe el build
```

Si cambias la UI, ejecuta también `./gradlew connectedDebugAndroidTest` con un emulador.

## Ramas, pull requests y releases

`main` solo contiene versiones publicadas y el trabajo diario se hace en `develop`. GitHub aplica estas reglas: no se puede hacer push directo a ninguna de las dos ramas y el CI es obligatorio. El motivo está en el [ADR 0005](docs/adr/0005-estrategia-de-ramas.md).

**Cambio normal**

1. Crea la rama desde `develop` actualizada: `<tipo>/<descripcion-corta>`, con `feat`, `fix`, `refactor`, `test`, `build`, `ci`, `docs` o `chore`. Por ejemplo, `feat/search-pagination`.
2. Abre el PR contra `develop`, con el título en Conventional Commits: será el commit que quede en `develop`.
3. Cuando el CI pase, fusiona con **Squash and merge**. La rama se borra sola.

**Release**

1. PR a `develop` `chore(release): bump version to X.Y.Z`, que sube `versionName` y `versionCode` en `app/build.gradle.kts`.
2. PR `develop` → `main` titulado `release: vX.Y.Z`, fusionado con **Create a merge commit**.
3. Tag y release sobre `main`:

   ```bash
   git switch main && git pull
   git tag -a vX.Y.Z -m "Watchly X.Y.Z"
   git push origin vX.Y.Z
   gh release create vX.Y.Z --title "Watchly X.Y.Z" --generate-notes
   ```

**Hotfix**

1. Crea `hotfix/X.Y.Z` desde `main`, corrige el error y sube `versionName` y `versionCode` en `app/build.gradle.kts`.
2. Abre el PR contra `main` y fusiona con merge commit.
3. Crea el tag y la release como en una release normal.
4. Abre un PR `main` → `develop` y fusiónalo con merge commit, para que la corrección no se pierda en la siguiente release.

## Convenciones

### Commits y decisiones

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/) en inglés y en imperativo, por ejemplo `feat(search): add pagination` o `fix(data): handle empty genres`. Cada commit compila y lleva sus tests.
- **Decisiones:** si cambias una decisión de arquitectura, añade o actualiza un ADR en `docs/adr/` y su fila en la tabla del README.

### Idiomas

| Qué | Idioma |
|---|---|
| Código, comentarios y KDoc | Inglés |
| Commits, nombres de rama y títulos de PR | Inglés |
| README, CONTRIBUTING, ADR, `AGENTS.md` y descripciones de PR | Español |
| Textos visibles de la app | `strings.xml` de `:presentation` en inglés (`values/`, por defecto) y en español (`values-es/`); nunca texto fijo en el código |

### Arquitectura

- Respeta la dirección `:app → :presentation → :domain ← :data`. `:domain` y `:data` son Kotlin puro: no dependen de Android ni de Hilt ([ADR 0001](docs/adr/0001-modulos-domain-y-data-en-kotlin-puro.md)).
- La UI (pantallas, ViewModels, navegación, tema y textos) va en `:presentation`, que solo depende de `:domain` ([ADR 0006](docs/adr/0006-modulo-presentation.md)).
- `:app` es la raíz de composición: los objetos de `:domain` y `:data` se crean en `di/AppModule.kt` con `@Provides`.
- `:data` solo expone lo necesario. Implementaciones, API, DTOs y mappers son `internal`; el punto de entrada público es `createShowRepository()` en `ShowRepositoryFactory.kt`.

### Código

El estilo lo imponen ktlint (`.editorconfig`: estilo `android_studio`, 120 columnas) y detekt (`config/detekt/detekt.yml`). Además:

- **Nombres por capa:** `XxxUseCase` con `operator fun invoke`, `XxxRepository` (interfaz en `:domain`) y `XxxRepositoryImpl` (en `:data`), `XxxDto` para red, mappers como funciones de extensión `toXxx()`, `XxxViewModel`, `XxxUiState` y `XxxScreen` (en `:presentation`).
- **Errores:** las operaciones que pueden fallar devuelven `Outcome<T>` con un `DomainError`; las excepciones no salen de `:data` ([ADR 0003](docs/adr/0003-cache-en-memoria-y-errores-tipados.md)). La UI traduce cada `DomainError` a un texto con `DomainError.messageRes()`.
- **Estado de la UI:** un `sealed interface XxxUiState` con un caso por estado posible (`Loading`, `Success`, `Empty`, `Error`…). El ViewModel lo expone como `StateFlow` de solo lectura y recibe los eventos con funciones `onXxx()`.
- **Corrutinas:** las búsquedas y peticiones que pueden quedar obsoletas se cancelan con `flatMapLatest` ([ADR 0002](docs/adr/0002-cancelacion-de-busquedas-con-flatmaplatest.md)). El tiempo se inyecta con `kotlin.time.TimeSource`; no se lee el reloj del sistema.
- **Accesibilidad:** cada componente interactivo tiene la semántica correcta (rol, `contentDescription`, `heading`, `liveRegion` para cambios de estado) y un test de semántica en `presentation/src/androidTest`.
- **Comentarios:** KDoc para explicar el porqué, no el qué.

### Tests

- Todo cambio de comportamiento lleva su test; un bug, un test que lo reproduzca antes de corregirlo.
- JUnit 4, con nombres en formato `` `given … when … then …` ``.
- Corrutinas con `runTest`, flows con Turbine (`.test { }`), dobles de prueba con MockK, tiempo con `TestTimeSource` y dispatcher principal con `MainDispatcherRule`.
- Sin `Thread.sleep` ni esperas reales: los tests son deterministas.
- Si cambia el número de tests, actualiza la sección "Tests" del README.
