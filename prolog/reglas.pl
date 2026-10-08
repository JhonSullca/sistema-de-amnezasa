/* Archivo de reglas documentado para el informe del Avance 1. */

regla_fuerza_bruta :- intentos_fallidos(I), I >= 5.
regla_conexion_sospechosa :- conexion_sospechosa(true).
regla_comportamiento_anomalo :- comportamiento_extrano(true).
regla_amenaza_combinada :-
    alerta_activa(true),
    conexion_sospechosa(true),
    comportamiento_extrano(true),
    intentos_fallidos(I), I >= 5.
