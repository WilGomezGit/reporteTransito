# 🚦 Reporte Tránsito

App Android para reporte de incidentes de tráfico en tiempo real para ciudades colombianas. Similar a Waze, permite reportar novedades viales y ver el estado del tráfico.

## Funcionalidades

- **Mapa en tiempo real** con iconos de novedades activas
- **Tipos de reporte**: Accidente, Retén policial, Congestión, Obra en vía, Obstáculo, Inundación, Vía cerrada
- **Auto-expiración**: las novedades desaparecen automáticamente tras 1 hora sin confirmaciones
- **Confirmar novedad**: los usuarios pueden confirmar que una novedad sigue activa (reinicia el contador de 1 hora)
- **Pico y Placa**: consulta las restricciones de hoy para Bogotá, Medellín, Cali y más ciudades
- **Consulta de placa**: ingresa tu placa y sabe si tienes restricción hoy
- **Múltiples ciudades**: Bogotá, Medellín, Cali, Barranquilla, Bucaramanga, Pereira, Manizales

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| Mapas | Google Maps SDK para Android |
| Backend | Firebase Firestore (tiempo real) |
| Auth | Firebase Anonymous Auth |
| DI | Hilt |
| Arquitectura | MVVM + Repository Pattern |

## Configuración inicial

### 1. Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un nuevo proyecto (ej: `reporte-transito`)
3. Registra la app Android con el paquete `com.reportetransito.app`
4. Descarga `google-services.json` y colócalo en `app/google-services.json`
5. Activa **Firestore Database** → modo Producción
6. Activa **Authentication** → Proveedor: Anónimo
7. En Firestore → Reglas, copia el contenido de `firestore.rules`

### 2. Google Maps API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com)
2. Activa la API **Maps SDK for Android**
3. Crea una API Key y restringe por paquete: `com.reportetransito.app`
4. En `local.properties` (NO subas este archivo a git):
   ```
   MAPS_API_KEY=AIza...tu_clave_aqui
   ```

### 3. Índices Firestore

Crea este índice compuesto en Firestore para las consultas por ciudad:

| Colección | Campo 1 | Campo 2 |
|-----------|---------|---------|
| incidents | cityId (ASC) | lastConfirmedAt (DESC) |

### 4. Compilar y ejecutar

```bash
# Requiere Android Studio Hedgehog o superior
# O con Gradle desde terminal:
./gradlew assembleDebug
```

## Estructura del proyecto

```
app/src/main/java/com/reportetransito/app/
├── data/
│   ├── model/          # Incident, IncidentType, PicoPlacaInfo
│   └── repository/     # IncidentRepository (Firebase), PicoPlacaRepository
├── di/                 # Módulos Hilt
├── ui/
│   ├── navigation/     # Navegación con bottom bar
│   ├── screens/        # MapScreen, PicoPlacaScreen, ReportIncidentScreen
│   ├── components/     # IncidentInfoCard
│   └── theme/          # Colores, tipografía, tema
└── viewmodel/          # MapViewModel, PicoPlacaViewModel
```

## Notas sobre Pico y Placa

Las restricciones están codificadas según la normativa vigente en 2025. Dado que las alcaldías pueden modificar las reglas, se recomienda verificar con la fuente oficial de cada ciudad antes de publicar la app.

- **Bogotá**: Lunes–Viernes, 6:00–8:30 y 15:00–19:30
- **Medellín**: Lunes–Viernes, 7:00–8:30 y 17:30–20:00
- **Cali**: Lunes–Viernes, 6:30–8:30 y 15:30–19:30

## Contribuir

Este proyecto está diseñado para ser extendido. Ideas para mejoras:
- Notificaciones push cuando hay un incidente cerca
- Modo conductor (pantalla grande con voz)
- Historial de reportes del usuario
- Integración con HERE Maps Traffic para el estado del tráfico en tiempo real
- Soporte offline con Room
