# Instrucciones para agentes de IA

Normas para cualquier asistente de IA que trabaje en Watchly. Este archivo recoge lo específico de la IA; las convenciones del proyecto están en [CONTRIBUTING.md](CONTRIBUTING.md) y las decisiones en los [ADR](docs/adr/), que son la fuente de verdad.

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
- Si cambias dependencias, red o serialización, comprueba que `app/build/outputs/mapping/release/missing_rules.txt` no existe o está vacío: R8 está activo en release y solo lo crea si faltan reglas.
- Informa de lo que no hayas podido ejecutar; no lo des por bueno.

## Convenciones de idioma, arquitectura, código y tests

Están en [CONTRIBUTING.md](CONTRIBUTING.md#convenciones) y son obligatorias. Léelas antes de escribir código, commits o documentación.
