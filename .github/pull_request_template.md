<!--
Base: `develop` (solo las releases y los hotfix van a `main`; ver CONTRIBUTING.md).
El título del PR, en Conventional Commits, será el mensaje del commit fusionado.
-->

## Qué cambia

<!-- Describe el cambio y por qué es necesario. Enlaza el issue si existe. -->

## Tipo de cambio

- [ ] `feat`: funcionalidad nueva
- [ ] `fix`: corrección de un error
- [ ] `refactor`: sin cambio de comportamiento
- [ ] `build` / `ci`: build, dependencias o CI
- [ ] `docs`: documentación

## Checklist

- [ ] `./gradlew ktlintCheck detekt test assembleDebug assembleRelease lintDebug` pasa en local
- [ ] He añadido o actualizado tests para el comportamiento que cambia
- [ ] He actualizado el README o un ADR si cambia una decisión de arquitectura
- [ ] Los textos nuevos están en `strings.xml` en inglés y en español
