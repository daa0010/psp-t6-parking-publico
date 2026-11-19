# Tarea · Parking público · Semáforos, synchronized y monitores

PSP · Tema 6 · 2.º A DAM · 2026/2027 · Nombre y apellidos: David Alonso Aguilar

> Completa los apartados. Borra las líneas que empiezan por `>` antes de entregar.
> La guía «PSP_T6_Guia_Alumno_Parking_Publico» explica cada nivel: tenla abierta mientras trabajas.

## Estado de la tarea

| Nivel | Clase                | Qué hay que hacer                            | Hecho |
|-------|----------------------|----------------------------------------------|-------|
| 1     | `Nivel1SinControl`   | Ejecutar y observar (no se cambia el código) | ☑    |
| 2     | `Nivel2Synchronized` | TODO 2a-2c                                   | ☐    |
| 3     | `Nivel3Semaforos`    | TODO 3a-3g                                   | ☐    |
| 4     | `Nivel4Monitor`      | TODO 4a-4c                                   | ☐    |

## Capturas

> Pega la salida final («CIERRE DEL NIVEL») de los niveles 2, 3 y 4 con todas las comprobaciones en OK.

## Respuestas a «Para pensar»

> Haz los cambios que pide cada pregunta en una copia o **deshazlos después**: el código que entregas debe ser el
> correcto.

### Nivel 1

1. Quita `Util.dormir(5);` de `entrar()` y ejecuta diez veces. ¿Sigue fallando siempre? ¿Significa eso que el programa
   ya es correcto?
   No sigue fallando siempre, en muchas ejecuciones puede terminar sin errores porque, al quitar
   la pausa de 5 ms, la ventana de tiempo entre comprobar `if (libres > 0)` y restar `libres--` se vuelve minúscula. Sin
   embargo, **el programa sigue siendo incorrecto**. La condición de carrera no ha desaparecido, solo es menos probable,
   como el orden de ejecución depende del planificador del sistema operativo, un fallo que no aparece en diez pruebas
   puede manifestarse una vez entre mil en cualquier momento.
2. Si cambias `libres--` por `libres = libres - 1`, ¿se arregla algo? ¿Por qué? **No se arregla nada.** En primer lugar,
   ninguna de las dos instrucciones internamente hacen
   exactamente los mismos tres pasos (leer el valor actual de `libres`, restarle 1 y volver a escribirlo), por lo que
   dos hilos pueden leer el mismo valor y pisarse al escribir. Además, aunque la resta fuera atómica, el problema
   principal continuaría porque comprobar `if (libres > 0)` y descontar la plaza siguen siendo pasos separados sin 
   exclusión mutua, permitiendo que dos hilos entren a la vez en el `if`.

### Nivel 2

3. `entrar()` devuelve las plazas que quedan en vez de que el coche las lea después con `getLibres()`. ¿Qué podría pasar
   si las leyera después?
4. Quita `synchronized` solo de `salir()`. ¿Qué comprobación puede fallar ahora?

### Nivel 3

5. Cambia `new Semaphore(PLAZAS, true)` por `new Semaphore(PLAZAS)`. ¿Siguen entrando en orden de llegada? ¿Qué problema
   podría aparecer con muchos coches?
6. Mueve `plazas.release()` fuera del `finally`, justo después de pagar. ¿En qué situación dejaría de funcionar?
7. ¿Qué pasaría si el cajero fuera `new Semaphore(2)`? ¿Y `new Semaphore(0)`?

### Nivel 4

8. En el panel, ¿en qué estado está un coche que espera plaza? ¿Y uno que está aparcado? ¿Por qué no son el mismo?
9. Cambia `notifyAll()` por `notify()`. Aquí sigue funcionando. ¿En qué tipo de programa dejaría de funcionar?
10. Cambia el `while` por `if` y ejecuta varias veces. ¿Qué comprobación falla? Explica con una secuencia concreta de
    coches por qué ocurre.

## Uso de IA

> Solo está permitido el NotebookLM del módulo. Indica si lo has usado y para qué.
