# AstroVitals

A fit-for-duty check for astronauts, on the watch.

Built for the NASA Space Apps Challenge 2026, challenge "Health Monitoring Software for Astronauts".

## The problem

On the ISS, astronauts call it "space fog": slow reflexes and fuzzy thinking after too little sleep. The dangerous part is that a tired person usually doesn't notice. They feel fine, they say they're fine, and then they make a mistake during an EVA or a maneuver.

Commercial wearables measure the body: heart rate, blood oxygen. None of them tell you whether your head is actually in the game.

## What it does

AstroVitals adds a short reaction test to the watch and turns the result into a single readiness number.

The screen goes black. A circle appears. You tap it as fast as you can. The test runs from 30 seconds to 3 minutes. The engine compares your times against your own normal baseline and returns green, yellow, or red. That cognitive score is then combined with heart rate and SpO2 into one Readiness Score from 0 to 100.

The test itself is NASA's own. The Reaction Self-Test (PVT-B) has run on the ISS since 2009 on the onboard laptops. AstroVitals moves it to the wrist, offline, next to the vital signs.

## Beyond a single number

- **Perceived vs measured.** If you report feeling good but your reflexes say otherwise, the app tells you so: "You feel fine, but your reaction times say you're not." This is the part that catches the blind spot the challenge calls out, the astronaut who can't judge their own state.
- **Advice after every test.** Short and practical. Take a break, drink water, avoid critical tasks, redo the test in an hour.
- **Escalation to the ground.** Two red results in a row and the app says to contact the flight surgeon at the next communication window. During a blackout the message waits in a queue and goes out when contact returns.
- **History.** A chart of the last seven sessions, so a slow decline over several bad nights is easy to see.
- **Body map.** A figure with three zones: head for cognition, heart for heart rate, lungs for SpO2. Tap a zone to read its status.

Everything works offline. Each session is cached on the watch and synced to the ground when a window opens.

## How it's built

One codebase, two watches, no phone app.

- `shared` holds all the logic in Kotlin Multiplatform: the test engine, the scoring, the advice and escalation rules. No calculation happens in the UI.
- `androidApp` is the Wear OS app, written in Jetpack Compose for Wear.
- `iosApp` is the watchOS app, written in SwiftUI.

Both watches run the same engine, so a Pixel Watch and an Apple Watch give the same result from the same input.

### Build

- Wear OS: `./gradlew :androidApp:assembleDebug`
- watchOS: open `iosApp` in Xcode and run it, or `cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=watchOS Simulator' build`

## Presentation

Slides: [assets/presentation.pptx](./assets/presentation.pptx)
