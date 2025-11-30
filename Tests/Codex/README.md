# Palette Picker (Tests/Codex)

Android-приложение на Kotlin + Jetpack Compose для выбора изображения через системный Photo Picker и извлечения пяти доминирующих цветов (HEX).

## Сборка и запуск
1. Установите JDK 17 и Android SDK с `compileSdk=34`.
2. Из корня репозитория перейдите в проект: `cd Tests/Codex`.
3. Соберите debug APK (Gradle wrapper jar скачается автоматически при первом запуске):
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. Запустите unit-тесты:
   ```bash
   ./gradlew :app:testDebug
   ```
5. При необходимости обновить версию wrapper выполните:
   ```bash
   ./gradlew wrapper --gradle-version 8.7
   ```
6. Установите APK на устройство/эмулятор из `app/build/outputs/apk/debug/app-debug.apk`.

## Использование
- Нажмите «Выбрать фото», чтобы открыть системный Photo Picker.
- После выбора изображение отображается превью, ниже показываются 5 уникальных HEX-кодов в порядке убывания `population` из Palette API.
- Кнопка **Copy** копирует HEX в буфер обмена и показывает уведомление.
- Выбор изображения и палитра сохраняются при повороте экрана.

## CI
Workflow `.github/workflows/build.yml` запускается на `push` и `pull_request`,
собирает `:app:assembleDebug` в каталоге `Tests/Codex` и загружает артефакт `app-debug.apk` из `app/build/outputs/apk/debug/`.
