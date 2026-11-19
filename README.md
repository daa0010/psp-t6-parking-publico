# Parking público · Semáforos, synchronized y monitores

PSP · Tema 6 · Sincronización de varios hilos · 2.º A DAM · 2026/2027 · Java 17, sin dependencias.

Un parking de **3 plazas** al que llegan **8 coches** (8 hilos) a la vez. El mismo problema resuelto cuatro veces, de peor a mejor:

| Nivel | Clase | Qué se aprende | ¿Funciona? |
|---|---|---|---|
| 1 | `Nivel1SinControl` | Condición de carrera: comprobar y actuar no es atómico | No: más coches que plazas y la caja no cuadra |
| 2 | `Nivel2Synchronized` | Monitor con `synchronized` (exclusión mutua) | Sí, pero si está lleno el coche se va |
| 3 | `Nivel3Semaforos` | `Semaphore` contador (plazas), binario (cajero), justo y `tryAcquire` con tiempo | Sí, y los coches esperan su turno |
| 4 | `Nivel4Monitor` | El mismo parking con `while` + `wait()` + `notifyAll()` y un panel demonio con los estados | Sí |

`Util` tiene lo común: `dormir()`, `azar()`, `log()` (con instante y nombre del hilo) y `comprobar()`.

## Abrirlo en IntelliJ IDEA

1. *File > Open* y elegir esta carpeta.
2. Clic derecho en `src` > *Mark Directory as > Sources Root*.
3. *File > Project Structure > Project*: SDK 17 o superior.
4. Abrir `Nivel1SinControl.java` y pulsar el triángulo verde junto a `main`.

## Desde la terminal

```bash
javac --release 17 -encoding UTF-8 -d out src/parking/*.java
java -cp out parking.Nivel1SinControl
java -cp out parking.Nivel2Synchronized
java -cp out parking.Nivel3Semaforos
java -cp out parking.Nivel4Monitor
```

Cada nivel termina con unas líneas `OK` / `ERROR` que comprueban si el parking ha funcionado. Ejecuta cada nivel **varias veces**: con hilos, cada ejecución es distinta.
