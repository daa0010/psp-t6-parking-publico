# Tarea · Parking público · Semáforos, synchronized y monitores

PSP · Tema 6 · 2.º A DAM · 2026/2027 · Nombre y apellidos: David Alonso Aguilar

> Completa los apartados. Borra las líneas que empiezan por `>` antes de entregar.
> La guía «PSP_T6_Guia_Alumno_Parking_Publico» explica cada nivel: tenla abierta mientras trabajas.

## Estado de la tarea

| Nivel | Clase                | Qué hay que hacer                            | Hecho |
|-------|----------------------|----------------------------------------------|-------|
| 1     | `Nivel1SinControl`   | Ejecutar y observar (no se cambia el código) | ☑     |
| 2     | `Nivel2Synchronized` | TODO 2a-2c                                   | ☑     |
| 3     | `Nivel3Semaforos`    | TODO 3a-3g                                   | ☑     |
| 4     | `Nivel4Monitor`      | TODO 4a-4c                                   | ☐     |

## Capturas

> Pega la salida final («CIERRE DEL NIVEL») de los niveles 2, 3 y 4 con todas las comprobaciones en OK.

### Nivel 2

```text
===== CIERRE DEL NIVEL 2 =====
Coches que aparcaron: 3 · se fueron sin aparcar: 5
OK Nunca hubo más coches que plazas (máximo 3/3)
OK La caja cuadra (6 €)
OK Al cerrar quedan 3 plazas libres
PERO 5 coches se han ido sin aparcar: nadie espera su turno.
```

### Nivel 3

```text
===== CIERRE DEL NIVEL 3 =====
Pagos: 7 · recaudado: 14 €
OK     Nunca hubo más coches que plazas (máximo 3/3)
OK     La caja cuadra
OK     Al cerrar, el semáforo tiene sus 3 permisos
Han aparcado 7 de 8 coches (el impaciente puede irse sin aparcar).
```

### Nivel 4

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
   Si el coche leyera las plazas con `getLibres()` en una llamada separada tras salir de `entrar()`, soltaría el cerrojo
   entre ambas operaciones y otro hilo podría entrar o salir en ese instante modificando `libres`. Como resultado, el
   coche mostraría un valor que ya no corresponde al momento en que realmente entró (por ejemplo, dos coches seguidos
   podrían imprimir el mismo número de plazas libres). Al devolver `libres` desde dentro de `entrar()`, la modificación
   y la lectura forman una única operación protegida bajo el mismo cerrojo.
4. Quita `synchronized` solo de `salir()`. ¿Qué comprobación puede fallar ahora?
   Basta con dejar un método sin sincronizar para que reaparezca la condición de carrera. Principalmente falla *
   *`La caja cuadra`** (dos coches que salen a la vez leen el mismo valor de `recaudado`, esperan en `Util.dormir(10)` y
   al sumar pisan uno de los pagos, perdiéndose dinero). También pueden fallar el recuento de **`plazas libres`** al
   cerrar o el control de **`máximo de coches dentro`**, ya que `salir()` modifica `libres++` y `dentro--` sin cerrojo
   mientras otro hilo puede estar modificándolos a la vez en `entrar()` o en otro `salir()`.

### Nivel 3

5. Cambia `new Semaphore(PLAZAS, true)` por `new Semaphore(PLAZAS)`. ¿Siguen entrando en orden de llegada? ¿Qué problema
   podría aparecer con muchos coches?
   **No siempre.** Al quitar el parámetro `true`, el semáforo pasa a ser no justo (*unfair*), por lo que no garantiza un
   orden estricto de llegada (FIFO): cuando se libera una plaza, un coche recién llegado puede adelantarse a otro que ya
   llevaba tiempo esperando. Con muchos coches llegando de forma continua, esto puede provocar *
   *inanición (*starvation*)**, es decir, que algún hilo tenga mala suerte y se quede esperando indefinidamente sin
   conseguir nunca permiso para entrar.
6. Mueve `plazas.release()` fuera del `finally`, justo después de pagar. ¿En qué situación dejaría de funcionar?
   Dejaría de funcionar si se produce una **excepción** (por ejemplo, una `InterruptedException` al esperar en
   `cajero.acquire()` o cualquier error en tiempo de ejecución mientras el coche está dentro del parking) antes de
   llegar a la línea de `plazas.release()`. Al no estar dentro de un `finally`, el permiso nunca se devolvería: esa
   plaza quedaría perdida para siempre (el parking «encogería»), fallaría la comprobación final de permisos del semáforo
   y los coches que esperasen podrían quedar bloqueados indefinidamente.
7. ¿Qué pasaría si el cajero fuera `new Semaphore(2)`? ¿Y `new Semaphore(0)`?
    - **Con `new Semaphore(2)`:** el semáforo tendría 2 permisos, así que podrían pagar **dos conductores a la vez** en
      el cajero automático. Aunque el dinero en `Caja` seguiría cuadrando porque sus métodos son `synchronized`, se
      incumpliría la restricción del sistema de que solo existe un único cajero.
    - **Con `new Semaphore(0)`:** el cajero arrancaría sin ningún permiso disponible. El primer coche que intentara
      pagar quedaría bloqueado para siempre en `cajero.acquire()`, y como ningún hilo puede llegar a ejecutar
      `cajero.release()`, todos los coches acabarían colgados indefinidamente (**bloqueo total / *deadlock***).

### Nivel 4

8. En el panel, ¿en qué estado está un coche que espera plaza? ¿Y uno que está aparcado? ¿Por qué no son el mismo?
9. Cambia `notifyAll()` por `notify()`. Aquí sigue funcionando. ¿En qué tipo de programa dejaría de funcionar?
10. Cambia el `while` por `if` y ejecuta varias veces. ¿Qué comprobación falla? Explica con una secuencia concreta de
    coches por qué ocurre.

## Uso de IA

> Solo está permitido el NotebookLM del módulo. Indica si lo has usado y para qué.
