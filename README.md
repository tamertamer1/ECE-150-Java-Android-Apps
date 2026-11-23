# Android App Suite

Five native Android apps in Java, built for UCSB's ECE 150 (Mobile Embedded Systems). They cover the core of Android development: device sensors, GPS and maps, persistent state, background services, networking, and calling external AI APIs.

![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android_SDK-3DDC84?logo=android&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google_Maps_API-4285F4?logo=googlemaps&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI_API-412991?logo=openai&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)

## The apps

| App | What it does | Key concepts |
|---|---|---|
| [**StockSage**](StockSage%20-%20Final/StockSage) · *final project* | Chat-style stock advisor. Type a ticker, it pulls the latest news from the Marketaux API, sends it to GPT for sentiment analysis, and replies with **Buy / Sell / Hold** and its reasoning. | REST APIs, background threads, LLM prompting, custom list adapter, history saved with Gson |
| [**LocationPlus**](LocationPlus/HW4%20-%20LocationPlus%20Template) | Live location on Google Maps with geofencing alerts and a list of visible GPS satellites. | Maps SDK, LocationManager, Geofencing API, GNSS status callbacks, background services |
| [**StepItUp**](StepItUp/StepItUp) | Step counter with daily goals. Reads the hardware step sensor and runs its own accelerometer-based step detector. | SensorManager, signal processing, SharedPreferences |
| [**GauchoPay**](GauchoPay/GauchoPay) | Payment-card wallet. Add cards, then pick one when a payment request arrives from a web server. | HTTP polling, permissions, list adapters, saved instance state |
| [**Pickture**](Pickture/Pickture) | Profile screen with a picture chosen from a gallery screen. | Multi-activity navigation, intents, activity results |

## Highlight: StockSage

```
user types "AAPL"
      │
      ▼
Marketaux news API ── latest headline + summary for AAPL
      │
      ▼
OpenAI chat completion ── "Start with Buy, Sell or Hold, then explain"
      │
      ▼
reply shown in the chat thread, conversation saved between launches
```

## Highlight: StepItUp's step detector

Besides the phone's built-in step counter, the app detects steps directly from the accelerometer: it takes the magnitude of each sample, keeps a rolling average over the last 1,000 samples, and counts a step when a sample spikes clearly above that baseline.

## Running an app

1. Open the app's folder (e.g. `StockSage - Final/StockSage`) in Android Studio.
2. Add your API keys to `local.properties` in that folder. This file is git-ignored.

   ```properties
   # StockSage
   OPENAI_API_KEY=your-openai-key
   MARKETAUX_API_KEY=your-marketaux-key

   # LocationPlus
   MAPS_API_KEY=your-google-maps-key
   ```
3. Run on an emulator or a device (API 24+).
