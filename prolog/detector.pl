:- consult('data/entrada_actual.pl').

/*
   Base de conocimientos y reglas del sistema experto.
   Scala genera los hechos de entrada y Prolog realiza la inferencia.
*/

puntuacion_base(0).

puntuar(V, P) :-
    ( alerta_activa(true) -> A = 2 ; A = 0 ),
    ( conexion_sospechosa(true) -> C = 3 ; C = 0 ),
    intentos_fallidos(I),
    ( I >= 10 -> F = 4
    ; I >= 5 -> F = 3
    ; I >= 3 -> F = 1
    ; F = 0
    ),
    ( comportamiento_extrano(true) -> E = 2 ; E = 0 ),
    V is A + C + F + E,
    P is V.

amenaza_y_razones(fuerza_bruta, Razones) :-
    intentos_fallidos(I), I >= 5,
    Razones = ['Se detectaron 5 o más intentos fallidos de inicio de sesión.'].

amenaza_y_razones(conexion_sospechosa, Razones) :-
    conexion_sospechosa(true),
    Razones = ['La conexión de origen fue marcada como sospechosa.'].

amenaza_y_razones(comportamiento_anomalo, Razones) :-
    comportamiento_extrano(true),
    Razones = ['Se observó un comportamiento fuera del patrón esperado.'].

amenaza_y_razones(amenaza_combinada, Razones) :-
    alerta_activa(true),
    conexion_sospechosa(true),
    comportamiento_extrano(true),
    intentos_fallidos(I), I >= 5,
    Razones = [
        'Existe una alerta de seguridad activa.',
        'La conexión fue considerada sospechosa.',
        'Se detectaron múltiples intentos fallidos.',
        'El comportamiento observado presenta anomalías.'
    ].

clasificar(P, critico) :- P >= 9, !.
clasificar(P, alto) :- P >= 6, !.
clasificar(P, medio) :- P >= 3, !.
clasificar(_, bajo).

nombre_nivel(critico, 'CRITICO').
nombre_nivel(alto, 'ALTO').
nombre_nivel(medio, 'MEDIO').
nombre_nivel(bajo, 'BAJO').

amenaza_final(P, 'Amenaza combinada') :-
    P >= 9,
    amenaza_y_razones(amenaza_combinada, _), !.
amenaza_final(_, 'Posible ataque de fuerza bruta') :-
    amenaza_y_razones(fuerza_bruta, _), !.
amenaza_final(_, 'Conexión sospechosa') :-
    amenaza_y_razones(conexion_sospechosa, _), !.
amenaza_final(_, 'Comportamiento anómalo') :-
    amenaza_y_razones(comportamiento_anomalo, _), !.
amenaza_final(_, 'Sin amenaza significativa').

razones_final(P, Razones) :-
    findall(R, (amenaza_y_razones(_, Rs), member(R, Rs)), Razones0),
    sort(Razones0, Razones1),
    ( Razones1 = [] ->
        Razones = ['No se encontraron indicadores relevantes.']
    ; Razones = Razones1
    ).

recomendacion(critico, 'Aislar el origen, revisar credenciales y escalar el incidente al responsable de seguridad.') :- !.
recomendacion(alto, 'Bloquear temporalmente el origen y revisar registros de autenticación y red.') :- !.
recomendacion(medio, 'Mantener monitoreo y verificar los registros asociados al evento.') :- !.
recomendacion(bajo, 'Continuar con el monitoreo normal y registrar el evento.').

mostrar_razones([]).
mostrar_razones([R|Rs]) :-
    write(R),
    ( Rs = [] -> true ; write(';') ),
    mostrar_razones(Rs).

analizar :-
    puntuar(P, _),
    clasificar(P, NivelAtom),
    nombre_nivel(NivelAtom, Nivel),
    amenaza_final(P, Amenaza),
    razones_final(P, Razones),
    recomendacion(NivelAtom, Recomendacion),
    format('nivel=~w~n', [Nivel]),
    format('amenaza=~w~n', [Amenaza]),
    format('puntuacion=~w~n', [P]),
    format('recomendacion=~w~n', [Recomendacion]),
    write('razones='), mostrar_razones(Razones), nl.

ejecutar :- analizar.
