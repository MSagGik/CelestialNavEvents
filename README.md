# 🌌 CelestialNavEvents

**CelestialNavEvents** is a lightweight Kotlin library for calculating common Sun and Moon events—sunrise, sunset, twilight phases, moonrise, moonset, lunar phases (in days), illumination, and more. It works offline worldwide using only geographic coordinates and time in the Java standard `ZonedDateTime` format.

[![](https://jitpack.io/v/MSagGik/CelestialNavEvents.svg)](https://jitpack.io/#MSagGik/CelestialNavEvents)

---

## 📖 Overview

CelestialNavEvents solves everyday needs for applications that react to or display daily astronomical events. Typical scenarios include:

- 🔆 Dynamic day/night backgrounds in apps
- 📸 Photography planning (magic hour / blue hour)
- 🌑 Night mode scheduling
- 🌊 Tide and weather forecasting tools
- 🧭 Hiking/navigation planners
- 🗓 Calendar integrations
- 🌓 Enhancing user experience with light/dark theme transitions

All calculations run locally; no internet connection is required.

---

## ✨ Key Features

### 🌞 Solar Events

- Sunrise & sunset
- Civil, nautical & astronomical twilight intervals
- Magic hour & blue hour
- Duration of stay above/below horizon
- Meridian & anti-meridian crossings
- Next upcoming solar event
- Polar day / night support (high-latitude handling)

### 🌕 Lunar Events

- Moonrise & moonset
- Lunar phase (in days) & illumination percentage
- Meridian & anti-meridian crossings
- Daylight / nighttime durations
- Next upcoming lunar event
- Polar day / night support (high-latitude handling)



### ⚙️ Technical Highlights

- Offline, worldwide support (no external data sources)
- Uses standard `java.time.ZonedDateTime` for accurate timezone handling
- Simple Kotlin-style API (DSL-like usage)
- Unit-tested against public data (e.g., timeanddate.com)
- Modular design: solar and lunar calculators are separate components

---

## 📦 Installation

### Using JitPack

1. In your project `settings.gradle` (or `settings.gradle.kts`):

   ```kotlin
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```

2. In your module `build.gradle` (or `build.gradle.kts`):

   ```kotlin
   dependencies {
       implementation("com.github.MSagGik:CelestialNavEvents:0.1.0")
   }
   ```

---

## ✨ Quick Start

```kotlin
import io.github.msaggik.celestialnavevents.CelestialNavEvents
import java.time.ZonedDateTime

fun main() {
    val celestial = CelestialNavEvents().provide()

    val solarEvents = celestial.solar().calculateSolarEventDay(
        latitude = 12.345,
        longitude = 12.345,
        dateTime = ZonedDateTime.now()
    )
    println("Sunrise & Sunset: ${solarEvents.events}")

    val lunarEvents = celestial.lunar().calculateLunarEventDay(
        latitude = 12.345,
        longitude = 12.345,
        dateTime = ZonedDateTime.now()
    )
    println("Moonrise & Moonset: ${lunarEvents.events}")
    println("Lunar illumination: ${lunarEvents.illuminationPercent}%")
}
```

---

## 🔭 API Overview

### Entry Point

```kotlin
val celestial: CelestialEventsCalculator = CelestialNavEvents().provide()
```

### Solar Events

- `calculateSolarEventDay(...)`
- `findUpcomingSolarAbsoluteEventDay(...)`
- `findUpcomingSolarRelativeEventDay(...)`
- `findUpcomingSolarRelativeShortEventDay(...)`
- `calculateMagicHourPeriod(...)`
- `calculateBlueHourPeriod(...)`
- `calculateCivilTwilightPeriod(...)`
- `calculateNauticalTwilightPeriod(...)`
- `calculateAstronomicalTwilightPeriod(...)`

### Lunar Events

- `calculateLunarEventDay(...)`
- `findUpcomingLunarAbsoluteEventDay(...)`
- `findUpcomingLunarRelativeEventDay(...)`
- `findUpcomingLunarRelativeShortEventDay(...)`

---

## 🧐 Use Cases

- Dynamic UI Backgrounds
- Photography Planning
- Moon Phase Widgets
- Astronomy / Weather Apps
- Navigation Tools
- Calendar Integrations

---

## 🏗️ Architecture & Design

- **Modular Structure**️\
  Factory via `CelestialNavEvents.provide()`\
  Interfaces: `CelestialEventsCalculator`, `SolarEventsCalculator`, `LunarEventsCalculator`

- **Patterns & Practices** \
  Factory, Facade, Data Classes\
  Separation of solar/lunar logic\
  Clear Kotlin DSL conventions

- **Testing** \
  Unit-tested with public data references\
  Handles polar edge cases

---
## 📡 Scientific Foundations and Algorithm Sources

CelestialNavEvents implements astronomical calculations based on well-established scientific works and formulas, balancing performance with reasonable precision. Key references include:

- van Flandern, T.C., & Pulkkinen, K.F. (1979).  
  *Low-precision formulae for planetary positions*, Astrophysical Journal Supplement Series, 41, 391–411.

- Reda, I. & Andreas, A. (2008).  
  *Solar Position Algorithm for Solar Radiation Applications*, NREL Report TP-560-34302.  
  [https://www.nrel.gov/docs/fy08osti/34302.pdf](https://www.nrel.gov/docs/fy08osti/34302.pdf)

- Meeus, J. (1998).  
  *Astronomical Algorithms* (2nd ed.).

- Espenak, F. & Meeus, J.  
  *Five Millennium Canon of Solar Eclipses: -1999 to +3000*, NASA Technical Publication TP-2006-214141.

- Espenak, F. (2014).  
  *Polynomial Expressions for Delta T.*
  [https://www.eclipsewise.com/help/deltatpoly2014.html](https://www.eclipsewise.com/help/deltatpoly2014.html)

Additional time calculations use commonly accepted astronomical methods implemented independently.

---

## 🚀 Accuracy Disclaimer

This library offers **practical, non-scientific precision** for JVM-based applications. It's suitable for:

- Photography & golden hour planning
- Night mode or UI theming
- Basic navigation tools
- General-purpose mobile & desktop apps

🔬 About Accuracy
- Based on widely recognized approximations from scientific literature
- Optimized for mid-latitude regions
- Accuracy may vary due to:
  - Observer elevation
  - Atmospheric pressure & temperature
  - Nearby obstructions (horizon profile)

Not intended for:
- Scientific research or precision-demanding simulations
- Professional astronomical observatories or ephemeris services
- Mission-critical, safety-related, or real-time systems

✅ Design Highlights
- No external dependencies — fully offline & local computation
- Original implementation — no direct reuse of third-party code
- Sources cited for transparency (see above)

⚠️ This software is provided “as is” without warranties.  
It is **not a substitute for professional-grade astronomical software**.  
Use at your own risk.

---

## 🤝 Contributing

Contributions are welcome:

- Report bugs
- Improve docs
- Add features or events
- Suggest improvements

---

## 📩 Feedback & Contact

- GitHub: [CelestialNavEvents](https://github.com/MSagGik/CelestialNavEvents)
- Issues & discussions on GitHub
- Open to collaboration and real-world usage feedback

---

## 📄 License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for full details.

Users redistributing or modifying this library, especially in academic or professional contexts, are encouraged to credit the scientific sources listed above.

> ⚠️ This library is provided *as is*, without warranties or guarantees. It is intended for practical, general-purpose use and is open to community contributions.