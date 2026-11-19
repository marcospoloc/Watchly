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

## Convenciones

- **Ramas:** `feat/...`, `fix/...`, `docs/...` a partir de `main`.
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/), por ejemplo `feat(search): add pagination` o `fix(data): handle empty genres`. Cada commit compila y lleva sus tests.
- **Arquitectura:** respeta la dirección `:app → :domain ← :data`. `:domain` y `:data` no dependen de Android ni de Hilt (ver [ADR 0001](docs/adr/0001-modulos-domain-y-data-en-kotlin-puro.md)).
- **Decisiones:** si cambias una decisión de arquitectura, añade o actualiza un ADR en `docs/adr/`.
- **Textos:** todo texto visible va en `strings.xml`, en inglés (por defecto) y en español.
