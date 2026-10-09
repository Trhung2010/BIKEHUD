# OBD HUD / GPS HUD

The main screen now has two explicit hardware dashboards. It starts in GPS HUD.
Switching to OBD HUD never silently substitutes GPS speed when the adapter is unavailable.

| Dashboard | Readings | Source |
| --- | --- | --- |
| OBD HUD | Speed, RPM, coolant temperature, throttle position | Valid ELM327 Mode 01 replies |
| OBD HUD | Adapter supply voltage | Valid ATRV reply; not a battery state-of-charge estimate |
| GPS HUD | Speed, direction of travel, altitude, position accuracy, latitude, longitude | Fresh non-mock FusedLocationProviderClient fixes, using hasSpeed/hasBearing/hasAltitude/hasAccuracy |

A dash means unavailable, unsupported, disconnected, or expired data. Actual zero readings
remain zero. GPS HUD does not infer gear, engine RPM, coolant temperature, fuel level,
or satellite count. Fused location can combine phone positioning sources; the UI does
not claim a pure GNSS fix or a measured satellite count.

## Freshness

Elapsed realtime, not wall-clock time, determines age. Speed/RPM and GPS fixes expire
after 5 seconds. Coolant/throttle expire after 30 seconds and adapter voltage after
60 seconds to accommodate their slower polling. Invalid PID replies do not renew
receive timestamps. Disconnect resets all PID timestamps. Values are published per
PID as soon as they are received.

## Interface

OBD HUD uses green accents, an RPM panel and engine measurement cards. GPS HUD uses
cyan accents and position measurement cards. The layout switches to two columns at
680dp and scrolls vertically on smaller displays or large fonts. The controls stay
unmirrored while the telemetry area supports reflection. The selector exposes selected
tab semantics, missing speed is announced as unavailable, and controls have minimum
48dp height. Threshold editing is explicitly in km/h; speed and displayed thresholds
convert together when mph is selected.

The main screen replaces the previous combined demonstration cockpit. Its route,
media, lean-angle, message and SOS panels are not mounted on the new dashboards; their
existing components and ViewModel methods remain in the project. This is a deliberate
scope change to keep these two screens focused on measured telemetry.

## Validation

Executed: 10 RealHudStateTest JUnit cases compiled with Kotlin 2.2.20 against the actual
model and projection source. A temporary Color constructor shim was used only to avoid
requiring Compose for these data-only tests; it is not part of the app. Cases cover
unknown readings, zero speed, individual PID expiry, disconnect, stale GPS, GPS fixes
without speed, future timestamps, source isolation, and AUTO compatibility.

Executed: syntax parsing of all Kotlin files and git diff --check.

Added but not executed: RealHudDashboardTest Compose/Robolectric checks for mph speed
labels, selected tabs, unavailable speed and explicit source selection.

Not executed: Gradle Android build, full test suite, APK rendering, TalkBack/device
checks, live ELM327/GPS verification. This checkout has no gradlew executable/JAR and
the execution environment has no Android SDK or installed Gradle. Validate with the
project's Android toolchain before merging.

Manual acceptance checks:

1. Grant location permission on first launch: updates must begin without restarting.
2. Connect an ELM327 adapter; choose OBD HUD and compare speed/RPM with vehicle readings.
3. Exercise unsupported PIDs, NO DATA replies, Bluetooth disconnect/reconnect and stale
   fixes; no defaults or old values should be presented as fresh measurements.
4. Verify GPS has no engine cards and displays a dash when a fix has no speed.
5. Verify both orientations, large fonts, reflection, km/h and mph, and TalkBack.
6. Verify threshold changes and data loss do not leave overspeed warnings stuck on.
