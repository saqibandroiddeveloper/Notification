# Notification Scheduler — Architecture & Production Guide

## 1. Architecture Overview

This module implements **Clean Architecture** with **MVI** in the presentation layer.

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation (Compose + MVI)                                │
│  Intent → ViewModel → State / Event                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Domain                                                       │
│  UseCases → NotificationRepository (interface)                 │
│  ScheduledNotification, RepeatType                           │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  Data                                                        │
│  NotificationRepositoryImpl                                  │
│  Room DAO · AlarmSchedulerImpl · Receivers                   │
└─────────────────────────────────────────────────────────────┘
```

### Why AlarmManager instead of WorkManager?

| Concern | AlarmManager | WorkManager |
|--------|--------------|-------------|
| Exact time | `setExactAndAllowWhileIdle()` fires at a specific wall-clock instant | Minimum 15 min deferral; inexact batching |
| App killed | Alarm is held by system; BroadcastReceiver runs in your process | Also survives, but not exact for reminders |
| Doze | `setExactAndAllowWhileIdle` exempted for exact alarms (with permission) | Uses JobScheduler constraints |
| Use case | Alarms, reminders, calendar, medication | Deferrable sync, upload, cleanup |

**WorkManager is wrong for “fire at 9:00 AM exactly”** — it is designed for guaranteed *eventually* work, not user-visible scheduled alarms.

### Doze mode behavior

- In Doze, normal inexact alarms are deferred to maintenance windows.
- **`setExactAndAllowWhileIdle(RTC_WAKEUP, …)`** wakes the device at the scheduled time when the app holds **exact alarm** capability (`canScheduleExactAlarms()` on Android 12+).
- After delivery, repeating schedules chain the **next** exact alarm (daily/weekly/custom) — the pattern used by clock/reminder apps.

### Exact alarms (Android 12+)

- `SCHEDULE_EXACT_ALARM` — user can grant via Settings; `AlarmManager.canScheduleExactAlarms()`.
- `USE_EXACT_ALARM` — for apps in the “Alarms & reminders” category (manifest; Play policy applies).
- Without permission, schedule fails gracefully and UI prompts the user.

### OEM / battery restrictions

Samsung, Xiaomi, Oppo, etc. may kill apps aggressively or require “autostart” / battery exemption. Production apps should:

- Document battery optimization steps for users
- Use `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` sparingly and only when justified
- Persist schedules in Room and **restore on BOOT_COMPLETED**
- Never rely only on in-memory state

### How reminder / alarm apps work internally

1. Persist every schedule in SQLite/Room.
2. Register one `PendingIntent` per alarm id with `AlarmManager`.
3. On fire: `BroadcastReceiver` → show notification → if repeating, compute next trigger and schedule again.
4. On reboot: `BOOT_COMPLETED` → read DB → re-register all alarms.
5. Tap: `Activity` `singleTop` + intent extras / deep link → Compose navigation.

---

## 2. Package Structure

```
com.app.notifications
├── core
│   ├── navigation          # NavHost, routes, deep links
│   ├── notification        # Channel, Helper, foreground tracker
│   ├── permission          # POST_NOTIFICATIONS, exact alarm
│   └── utils               # PendingIntent flags, date format
├── data
│   ├── local               # Room entity, DAO, database
│   ├── receiver            # Alarm + Boot receivers
│   ├── repository          # NotificationRepositoryImpl
│   └── scheduler           # AlarmScheduler, AlarmSchedulerImpl
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── presentation
│   └── notification
│       ├── intent / state / event
│       ├── viewmodel
│       └── screen
└── di                      # Hilt modules
```

---

## 3. Step-by-Step Implementation Flow

### Schedule

1. UI sends `NotificationIntent.ScheduleNotification`.
2. ViewModel checks notification + exact-alarm permission.
3. `ScheduleNotificationUseCase` validates input.
4. `NotificationRepositoryImpl` assigns id, calls `AlarmSchedulerImpl.setExactAndAllowWhileIdle`, saves to Room.

### Fire (app killed or swiped from recents)

1. System delivers `PendingIntent` to `NotificationAlarmReceiver`.
2. `goAsync()` + coroutine calls `onNotificationDelivered(id)`.
3. `NotificationHelper.showNotification()` posts to NotificationManager.
4. If repeating, update `triggerAtMillis` in Room and schedule next alarm; else disable row.

### Reboot

1. `BootReceiver` receives `BOOT_COMPLETED`.
2. `RescheduleAllNotificationsUseCase` → reload enabled rows → skip/advance past-due → `setExactAndAllowWhileIdle` for each.

### Notification tap

1. Content `PendingIntent` opens `MainActivity` with `ACTION_NOTIFICATION_CLICK` + extras.
2. `NotificationNavGraph` navigates to `notification_detail/{id}`.

---

## 4. Gradle & Hilt Setup

See `gradle/libs.versions.toml` and `app/build.gradle.kts`:

- Hilt + KSP
- Room
- Navigation Compose
- Lifecycle / Coroutines

Application class: `@HiltAndroidApp` on `NotificationApplication`.

---

## 5. Manifest Checklist

- `POST_NOTIFICATIONS`
- `RECEIVE_BOOT_COMPLETED`
- `SCHEDULE_EXACT_ALARM`
- Receivers: alarm (exported=false), boot (exported=true)
- `MainActivity` `launchMode=singleTop` for notification re-entry

---

## 6. ProGuard

See `app/proguard-rules.pro` — keep Hilt, Room entities, receivers.

---

## 7. Testing

- `ScheduleNotificationUseCaseTest` — validation paths
- `CancelNotificationUseCaseTest` — cancel flow
- `NotificationViewModelTest` — MVI state update
- `FakeNotificationRepository` — in-memory test double

Run: `./gradlew test`

---

## 8. Best Practices

- Use **immutable** `NotificationState` and sealed `Intent` / `Event`.
- **Never** use `GlobalScope` in receivers; use `goAsync()` + structured concurrency.
- **FLAG_IMMUTABLE** on all PendingIntents (Android 12+).
- **Room** is the source of truth for reboot recovery.
- Chain repeating alarms on delivery (not `setRepeating()`).
- Request permissions before schedule; degrade gracefully.
- `singleTop` + `onNewIntent` for notification clicks when app is alive.

---

## 9. Common Mistakes

| Mistake | Fix |
|--------|-----|
| Only storing alarm in memory | Persist in Room |
| Using WorkManager for exact time | Use AlarmManager |
| `setRepeating()` for precise daily alarm | Chain `setExactAndAllowWhileIdle` |
| Mutable PendingIntent without IMMUTABLE | Use `PendingIntentUtils` |
| Forgetting BOOT_COMPLETED | `BootReceiver` + reschedule |
| Not handling Android 13 permission | `POST_NOTIFICATIONS` launcher |
| Assuming exact alarm without check | `canScheduleExactAlarms()` |
| exported=true on alarm receiver | Keep false; explicit intents only |

---

## 10. Battery Optimization Notes

- Exact alarms are intentional wakeups — minimize count, batch non-critical work separately.
- Cancel alarms when user disables notifications.
- Avoid polling; event-driven only.
- For enterprise/OEM devices, in-app FAQ for battery whitelist is common in production reminder apps.
