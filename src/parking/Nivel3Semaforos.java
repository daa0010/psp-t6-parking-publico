package parking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * NIVEL 3 · Semáforos: los coches ESPERAN su turno.
 *
 * Dos semáforos, cada uno con un papel distinto:
 *
 *   plazas  = new Semaphore(PLAZAS, true)   semáforo CONTADOR: hay N plazas.
 *             acquire() resta una plaza; si no quedan, el coche espera (WAITING).
 *             release() devuelve la plaza y despierta al primero de la cola.
 *             true = semáforo JUSTO: atiende por orden de llegada (nadie se cuela).
 *
 *   cajero  = new Semaphore(1)              semáforo BINARIO: un único cajero automático.
 *             Solo un conductor paga a la vez: funciona como un cerrojo.
 *
 * Y la caja (el dinero) es un monitor con synchronized: semáforo y synchronized se combinan.
 *
 * Además, el último coche es IMPACIENTE: usa tryAcquire con tiempo máximo y,
 * si no consigue plaza en 400 ms, se va.
 */
public class Nivel3Semaforos {

    /** La caja registradora: monitor con synchronized. */
    static class Caja {
        private int recaudado = 0;
        private int pagos = 0;
        synchronized void cobrar(int euros) { recaudado += euros; pagos++; }
        synchronized int getRecaudado() { return recaudado; }
        synchronized int getPagos() { return pagos; }
    }

    static final int PLAZAS = 3, COCHES = 8, TARIFA = 2;
    static final Semaphore plazas = new Semaphore(PLAZAS, true);
    static final Semaphore cajero = new Semaphore(1);
    static final Caja caja = new Caja();

    // Solo para COMPROBAR que el semáforo funciona: cuántos hay dentro y el máximo.
    private static int dentro = 0, maxDentro = 0;
    private static synchronized void contarEntrada() { dentro++; maxDentro = Math.max(maxDentro, dentro); }
    private static synchronized void contarSalida() { dentro--; }
    private static synchronized int getMaxDentro() { return maxDentro; }

    /** Lo que hace cada coche. */
    static void aparcar(boolean impaciente) {
        try {
            if (impaciente) {
                Util.log("llega con prisa: espera como mucho 400 ms");
                if (!plazas.tryAcquire(400, TimeUnit.MILLISECONDS)) {   // espera limitada (TIMED_WAITING)
                    Util.log("se cansa de esperar y se va");
                    return;
                }
            } else {
                Util.log("llega a la barrera. Plazas libres: " + plazas.availablePermits());
                plazas.acquire();                                       // si no hay plaza, espera aquí (WAITING)
            }
            // A partir de aquí tenemos una plaza: hay que devolverla SIEMPRE -> try/finally
            try {
                contarEntrada();
                Util.log("ENTRA. Plazas libres: " + plazas.availablePermits());
                Util.dormir(Util.azar(200, 500));                       // aparcado

                cajero.acquire();                                       // un solo cajero
                try {
                    Util.log("paga " + TARIFA + " € en el cajero");
                    Util.dormir(50);
                    caja.cobrar(TARIFA);
                } finally {
                    cajero.release();
                }
            } finally {
                contarSalida();
                plazas.release();                                       // devuelve la plaza
                Util.log("SALE. Plazas libres: " + plazas.availablePermits());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Util.log("interrumpido");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Thread> coches = new ArrayList<>();
        for (int i = 1; i <= COCHES; i++) {
            final boolean impaciente = (i == COCHES);
            coches.add(new Thread(() -> aparcar(impaciente), "Coche-" + i));
        }
        for (Thread c : coches) {
            c.start();
            Util.dormir(20);              // llegan casi a la vez, pero en orden: así se ve que el semáforo justo respeta la cola
        }
        for (Thread c : coches) c.join();

        System.out.println("\n===== CIERRE DEL NIVEL 3 =====");
        System.out.println("Pagos: " + caja.getPagos() + " · recaudado: " + caja.getRecaudado() + " €");
        Util.comprobar("Nunca hubo más coches que plazas (máximo " + getMaxDentro() + "/" + PLAZAS + ")", getMaxDentro() <= PLAZAS);
        Util.comprobar("La caja cuadra", caja.getRecaudado() == caja.getPagos() * TARIFA);
        Util.comprobar("Al cerrar, el semáforo tiene sus " + PLAZAS + " permisos", plazas.availablePermits() == PLAZAS);
        System.out.println("  Han aparcado " + caja.getPagos() + " de " + COCHES + " coches (el impaciente puede irse sin aparcar).");
    }
}
