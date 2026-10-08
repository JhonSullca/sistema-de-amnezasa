# Sistema Experto de Detección de Amenazas Informáticas

Proyecto para el Avance 2 del curso Lenguajes de Programación.

## Idea del sistema

La aplicación recibe eventos de seguridad y analiza cuatro indicadores principales:

- Intentos fallidos de inicio de sesión.
- Conexiones sospechosas.
- Alertas de seguridad activas.
- Comportamientos extraños o anómalos.

Scala se utiliza para los modelos, servicios, servidor web, análisis del historial y reportes. Prolog contiene los hechos y reglas que determinan el nivel de riesgo y la amenaza principal.

## Requisitos

- JDK instalado.
- IntelliJ IDEA con soporte para Scala.
- Scala 3.9.0 mediante sbt.
- SWI-Prolog instalado.
- `swipl` disponible en el PATH o ruta configurada en `config/app.properties`.

## Ejecutar en IntelliJ IDEA

1. Abre IntelliJ IDEA.
2. Selecciona **Open** y elige la carpeta `sistema-experto-amenazas`.
3. Espera a que IntelliJ cargue el proyecto sbt.
4. Configura el JDK del proyecto.
5. Abre `src/main/scala/app/Main.scala`.
6. Ejecuta `Main`.
7. Abre `http://localhost:8080`.

## Verificar SWI-Prolog

En PowerShell:

```powershell
swipl --version
```

Si no funciona, coloca la ruta completa en `config/app.properties`, por ejemplo:

```properties
swipl.path=C:/Program Files/swipl/bin/swipl.exe
```

## Prueba rápida

En la web, pulsa **Cargar ejemplo** y luego **Analizar con Prolog**.

El ejemplo combina alerta activa, conexión sospechosa, 10 intentos fallidos y comportamiento extraño. El motor debe clasificarlo como `CRITICO` y mostrar `Amenaza combinada`.

## Relación con los requisitos del Avance 1

### Scala

- Modelos: `EventoSeguridad`, `AnalisisAmenaza`, `RegistroAnalisis`, `Reporte`.
- Funciones: servicios y funciones auxiliares.
- Listas: razones, historial y datos agrupados.
- Pattern matching: clasificación HTTP y resolución de tipos MIME.
- Case classes: todos los modelos principales.
- Manejo de eventos y reportes.

### Prolog

- Hechos: indicadores y hechos dinámicos generados por Scala.
- Reglas: fuerza bruta, conexión sospechosa, comportamiento anómalo y amenaza combinada.
- Consultas: `consultas.pl` contiene consultas demostrativas.
- Inferencia: `detector.pl` calcula puntuación, nivel, amenaza y recomendación.

## Estructura

```text
sistema-experto-amenazas/
├── build.sbt
├── project/
│   └── build.properties
├── config/
│   └── app.properties
├── src/main/scala/app/
│   ├── Main.scala
│   ├── modelos/
│   │   ├── AnalisisAmenaza.scala
│   │   ├── EventoSeguridad.scala
│   │   └── Reporte.scala
│   ├── servicios/
│   │   ├── AnalisisService.scala
│   │   └── PrologService.scala
│   └── web/
│       ├── JsonUtil.scala
│       └── WebServer.scala
├── prolog/
│   ├── detector.pl
│   ├── hechos.pl
│   ├── reglas.pl
│   └── consultas.pl
├── data/
│   └── entrada_actual.pl
├── web/
│   ├── index.html
│   ├── css/styles.css
│   └── js/app.js
├── docs/
└── README.md
```

## Arquitectura

```text
Usuario
   ↓
Web HTML/CSS/JS
   ↓
Servidor HTTP en Scala
   ↓
AnalisisService
   ↓
PrologService
   ↓
SWI-Prolog
   ↓
Hechos + Reglas + Inferencia
   ↓
Resultado de amenaza
   ↓
Scala genera historial y reporte
```

## Nota académica

Para este avance se eligió invocar SWI-Prolog desde Scala mediante un proceso del sistema. Esto permite demostrar claramente la separación de responsabilidades: Scala gestiona la aplicación web y Prolog ejecuta las reglas de decisión. En una etapa posterior se puede reemplazar esta comunicación por JPL u otra integración más directa.
