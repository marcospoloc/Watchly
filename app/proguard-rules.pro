# R8 is enabled for release builds.
#
# No project-specific rules are needed today: kotlinx.serialization, Retrofit,
# OkHttp, Hilt, Coil and Navigation ship their own consumer rules, which cover
# the @Serializable DTOs in :data, the type-safe navigation routes and the
# suspend Retrofit interface. Add rules here only when R8 reports a missing one
# (see app/build/outputs/mapping/release/missing_rules.txt).
