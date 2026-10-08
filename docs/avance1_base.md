# Base para el Informe Técnico — Avance 1

## Título
Sistema Experto de Detección de Amenazas Informáticas utilizando Scala y Prolog

## Resumen
El proyecto desarrolla un sistema experto que analiza eventos de seguridad para identificar posibles amenazas informáticas. El sistema recibe datos como intentos fallidos de inicio de sesión, conexiones sospechosas, alertas activas y comportamientos extraños. Scala gestiona la interfaz web, los modelos, el procesamiento del historial y los reportes; Prolog implementa los hechos, reglas e inferencia lógica para clasificar el nivel de riesgo.

## Problemática
Los sistemas y usuarios pueden generar múltiples eventos de seguridad que deben ser revisados para determinar si existe un comportamiento riesgoso. La revisión manual de cada evento puede dificultar la identificación rápida de patrones como fuerza bruta, conexiones sospechosas o combinaciones de varios indicadores.

## Objetivo general
Desarrollar un sistema experto web que permita analizar eventos de seguridad informática e identificar posibles amenazas utilizando Scala y Prolog.

## Objetivos específicos
- Registrar eventos de seguridad mediante Scala.
- Aplicar modelos, funciones, listas, case classes y pattern matching.
- Crear una base de conocimientos y reglas de detección en Prolog.
- Realizar consultas y procesos de inferencia.
- Mostrar resultados y reportes mediante una interfaz web dinámica.

## Arquitectura
Usuario → Interfaz web → Scala → Prolog → Inferencia → Resultado → Reporte.

## Funcionalidades del avance
- Formulario dinámico de eventos.
- Ejecución de reglas Prolog.
- Clasificación BAJO, MEDIO, ALTO y CRITICO.
- Identificación de amenaza principal.
- Recomendación de acción.
- Historial de análisis.
- Estadísticas de eventos analizados.

## Evidencias para la presentación
1. IntelliJ mostrando el proyecto Scala.
2. Ejecución de `Main.scala`.
3. Web en `localhost:8080`.
4. Un análisis crítico mediante el botón `Cargar ejemplo`.
5. Captura de `prolog/detector.pl` mostrando reglas.
6. Captura de `prolog/consultas.pl` con consultas.
7. Tabla de historial y estadísticas.
