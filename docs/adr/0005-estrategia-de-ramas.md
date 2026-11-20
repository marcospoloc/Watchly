# 0005. Estrategia de ramas: `main` para versiones publicadas y `develop` para el trabajo diario

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

Hasta ahora solo existía `main`, sin protección: se podía hacer push directo y force-push, y algún commit entró sin pasar por un pull request. Además, `main` mezclaba el trabajo en curso con lo publicado y no había forma de saber qué commit correspondía a cada versión.

Se quiere que `main` contenga solo versiones publicadas y que el trabajo diario se haga en otra rama.

## Decisión

Usar un Git Flow simplificado, con las reglas aplicadas por GitHub (rulesets y checks obligatorios) en lugar de depender de la disciplina.

| Rama | Propósito | Sale de | Entra en | Tipo de merge |
|---|---|---|---|---|
| `main` | Solo versiones publicadas; cada merge es una release con tag | — | — | — |
| `develop` | Integración diaria; siempre compila y pasa el CI | `main` | `main`, en cada release | Merge commit |
| `feat/`, `fix/`, `refactor/`, `test/`, `build/`, `ci/`, `docs/`, `chore/` | Un cambio por rama, de vida corta | `develop` | `develop` | Squash |
| `hotfix/x.y.z` | Corrección urgente de una versión publicada | `main` | `main`, y después `main` → `develop` | Merge commit |
| `dependabot/*` | Actualizaciones automáticas (`target-branch: develop`) | `develop` | `develop` | Squash |

- **Release:** se sube `versionName`/`versionCode` en `develop`, se abre un PR `develop` → `main` titulado `release: vX.Y.Z`, se fusiona con merge commit y se crea el tag anotado `vX.Y.Z` con su GitHub Release. Versionado SemVer.
- **Rama por defecto:** `main`, para que quien visite el repositorio vea la última versión publicada.
- **Rulesets** (sin bypass):
  - `develop`: solo por PR, CI obligatorio con la rama al día, sin force-push ni borrado. Admite squash (cambios normales) y merge commit (solo para traer un hotfix desde `main`).
  - `main`: solo por PR con merge commit, checks obligatorios `Build, test and lint` y `Release source branch`, sin force-push ni borrado.
- **Check `Release source branch`** (`.github/workflows/branch-policy.yml`): falla si un PR a `main` no viene de `develop` ni de `hotfix/*`. Los rulesets no pueden limitar la rama de origen de un PR.

## Por qué

- **`main` como registro de lo publicado.** `git log --first-parent main` lista exactamente las releases, cada una con su tag, así que se puede reconstruir cualquier APK publicado. La portada del repositorio no muestra trabajo a medias.
- **Squash en `develop`.** El CI valida el estado final de cada PR, no sus commits intermedios. Con squash, cada commit de `develop` es un PR completo que pasó el CI y su título sigue Conventional Commits. Los commits originales siguen en el PR y en el cuerpo del commit.
- **Merge commit de `develop` a `main`.** Si se aplastara, `main` y `develop` dejarían de compartir historia y cada release siguiente daría conflictos. Con merge commit, todo lo publicado sigue siendo parte de la historia de `develop`.
- **`main` no exige la rama al día.** Si lo exigiera, cada release obligaría a fusionar antes el merge commit de la release anterior en `develop`. Ese commit no cambia ningún fichero, así que no aporta nada.
- **0 aprobaciones obligatorias.** GitHub no permite aprobar los propios PRs; con un único desarrollador bloquearía todos los cambios. La revisión la cubren el CI y la auto-revisión del PR.
- **Sin ramas `release/*`.** Sirven para estabilizar una versión mientras otras personas siguen integrando en `develop`. Con un solo desarrollador, `develop` ya queda congelada durante la release.

## Consecuencias

- Cada cambio se integra dos veces (en `develop` y al publicar) y cada hotfix necesita un PR de vuelta a `develop`. `CONTRIBUTING.md` documenta los pasos.
- Las actualizaciones de seguridad de Dependabot siempre apuntan a la rama por defecto (`main`), y el check de origen las bloquea. Se aplican como hotfix o se integran en `develop` y salen en la siguiente release.
- El grafo de dependencias se envía solo desde `main`, así que las alertas de seguridad corresponden a lo publicado.
- Los merge commits de #3 y #4 ya estaban en `main` y no se reescriben. El estado de `main` al adoptar esta decisión se publica como `v1.0.0`.
- Si entran más desarrolladores o la app se publica en Google Play con fases de prueba, se valorará añadir ramas `release/*` y aprobaciones obligatorias en un ADR nuevo.
