# 0004. Actualización de dependencias sin migrar a AGP 9

- **Estado:** aceptada
- **Fecha:** 2026-11

## Contexto

El proyecto usaba versiones de finales de 2024 y `targetSdk 35`. Google Play exige `targetSdk 36` para apps nuevas y actualizaciones desde el 31 de agosto de 2026. Las últimas versiones de muchas librerías exigen Android Gradle Plugin 9, que es un salto con cambios incompatibles.

## Decisión

Hacer una actualización **moderada**: subir `compileSdk` y `targetSdk` a 36 y llevar cada librería a la versión más reciente compatible con **AGP 8.13**, sin migrar todavía a AGP 9.

Las versiones que se quedaron por detrás de la última disponible, y el motivo comprobado durante la actualización:

| Dependencia | Versión usada | Motivo |
|---|---|---|
| core-ktx, activity-compose, lifecycle, navigation-compose, hilt-navigation-compose | 1.18.0, 1.12.4, 2.10.0, 2.9.8, 1.3.0 | Las versiones siguientes declaran `minAndroidGradlePluginVersion` 9.1 en su metadata AAR |
| Compose BOM | 2026.06.01 | Compose 1.12 (BOM 2026.08+) exige AGP 9.2 y compileSdk 37 |
| Hilt | 2.58 | Hilt 2.59+ exige AGP 9 |
| Kotlin / KSP | 2.3.21 / 2.3.14 | Hilt 2.58 solo lee metadata de Kotlin hasta la versión 2.3 |
| kotlinx-coroutines / serialization | 1.10.2 / 1.10.0 | Las versiones 1.11 y 1.13 están compiladas con Kotlin 2.4 |
| OkHttp / Retrofit | 4.12.0 / 2.12.0 | OkHttp 5 y Retrofit 3 son versiones mayores con cambios de API; quedan fuera de esta actualización |
| Coil | 2.7.0 | Coil 3 cambia de paquete (`io.coil-kt.coil3`) |

## Consecuencias

- La app cumple el requisito actual de Google Play y todo el build (tests, ktlint, detekt, lint, R8) pasa con las nuevas versiones.
- Dependabot agrupa las actualizaciones de Gradle en un único pull request semanal e ignora los saltos mayores de AGP. Si ese pull request falla en CI porque alguna versión exige AGP 9, se revisa junto con la migración en lugar de aceptarlo por partes.
- La migración a AGP 9 queda como trabajo pendiente, y desbloquea el resto de la tabla en un solo paso.

## Alertas de seguridad en dependencias de build

Al enviar el grafo de dependencias a GitHub, Dependabot abrió 58 alertas (3 críticas, 24 altas, 26 medias, 5 bajas). Todas están en el classpath de los plugins de build: Netty, BouncyCastle, jdom2, jose4j, commons-compress y logback, que llegan a través de AGP 8.13 y del Kotlin Gradle Plugin 2.3. Ninguna de esas librerías está en `releaseRuntimeClasspath`, así que no se distribuyen en el APK.

Se descartaron en GitHub como **riesgo tolerable**: solo se ejecutan al compilar, en la máquina de desarrollo y en el CI, y la del Kotlin Gradle Plugin no se puede parchear sin Kotlin 2.4.20, que Hilt 2.58 no admite. Se resuelven con la migración a AGP 9. Las alertas que aparezcan en dependencias de runtime de la app sí se tratan como bloqueantes.
