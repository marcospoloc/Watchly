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
   gh release create vX.Y.Z --generate-notes
   ```

**Hotfix**

1. Crea `hotfix/X.Y.Z` desde `main`, corrige el error y sube `versionName` y `versionCode` en `app/build.gradle.kts`.
2. Abre el PR contra `main` y fusiona con merge commit.
3. Crea el tag y la release como en una release normal.
4. Abre un PR `main` → `develop` y fusiónalo con merge commit, para que la corrección no se pierda en la siguiente release.

## Convenciones

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/), por ejemplo `feat(search): add pagination` o `fix(data): handle empty genres`. Cada commit compila y lleva sus tests.
- **Arquitectura:** respeta la dirección `:app → :domain ← :data`. `:domain` y `:data` no dependen de Android ni de Hilt (ver [ADR 0001](docs/adr/0001-modulos-domain-y-data-en-kotlin-puro.md)).
- **Decisiones:** si cambias una decisión de arquitectura, añade o actualiza un ADR en `docs/adr/`.
- **Textos:** todo texto visible va en `strings.xml`, en inglés (por defecto) y en español.
