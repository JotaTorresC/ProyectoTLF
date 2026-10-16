//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Simulador del AFD para combos de juegos de pelea (Temática 35).
 *
 * Autores: Jason Steven Torres Caballero - Juan David Arango Valencia
 *
 * Lenguaje: L = { a1 > a2 > ... > an | n >= 1, ai en {p, k, dfp, fdfp, dbk} }
 * ER:       A(>A)*   con   A = p | k | dfp | fdfp | dbk
 *
 * El AFD se implementa de forma manual, sin java.util.regex ni parsers
 * genéricos. La función de transición δ(q, a) se guarda en un Map con claves
 * "estado|símbolo". Toda transición no listada va al estado trampa qt, de modo
 * que δ es una función total.
 *
 * Compilar: javac SimuladorAFDCombos.java
 * Ejecutar: java SimuladorAFDCombos
 */
public class SimuladorAFDCombos {

    // ------------------------------------------------------------------
    // Registro de un paso de la traza: δ(origen, simbolo) = destino
    // ------------------------------------------------------------------
    static class Paso {
        final String origen;
        final char simbolo;
        final String destino;

        Paso(String origen, char simbolo, String destino) {
            this.origen = origen;
            this.simbolo = simbolo;
            this.destino = destino;
        }
    }

    // ------------------------------------------------------------------
    // Resultado de procesar una cadena
    // ------------------------------------------------------------------
    static class Resultado {
        final boolean aceptada;
        final List<Paso> traza;
        final String estadoFinal;

        Resultado(boolean aceptada, List<Paso> traza, String estadoFinal) {
            this.aceptada = aceptada;
            this.traza = traza;
            this.estadoFinal = estadoFinal;
        }
    }

    // ------------------------------------------------------------------
    // AFD  M = (Q, Σ, δ, q0, F)
    // ------------------------------------------------------------------
    static class AFD {
        final Set<String> estados = new HashSet<>();           // Q
        final Set<Character> alfabeto = new HashSet<>();       // Σ
        final Map<String, String> transiciones = new HashMap<>(); // δ
        final String inicial = "q0";                           // q0
        final Set<String> aceptacion = new HashSet<>();        // F
        final String trampa = "qt";                            // estado trampa

        AFD() {
            estados.addAll(Arrays.asList(
                    "q0", "q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "qt"));
            for (char c : new char[] {'d', 'f', 'b', 'p', 'k', '>'}) {
                alfabeto.add(c);
            }
            aceptacion.add("q1");

            // q0 (inicial) y q8 (tras '>') esperan el comienzo de una acción
            for (String q : new String[] {"q0", "q8"}) {
                agregar(q, 'd', "q2");
                agregar(q, 'f', "q4");
                agregar(q, 'p', "q1");
                agregar(q, 'k', "q1");
            }
            agregar("q1", '>', "q8"); // acción completa -> separador
            agregar("q2", 'f', "q3"); // d f    (fireball)
            agregar("q2", 'b', "q7"); // d b    (hurricane)
            agregar("q3", 'p', "q1"); // dfp
            agregar("q7", 'k', "q1"); // dbk
            agregar("q4", 'd', "q5"); // f d    (dragon punch)
            agregar("q5", 'f', "q6"); // f d f
            agregar("q6", 'p', "q1"); // fdfp
        }

        private void agregar(String estado, char simbolo, String destino) {
            transiciones.put(estado + "|" + simbolo, destino);
        }

        /** δ(q, a): función de transición total. */
        String delta(String estado, char simbolo) {
            if (!alfabeto.contains(simbolo)) {
                return trampa;
            }
            return transiciones.getOrDefault(estado + "|" + simbolo, trampa);
        }

        /** Recorre la cadena símbolo a símbolo guardando la traza. */
        Resultado procesar(String cadena) {
            String estado = inicial;
            List<Paso> traza = new ArrayList<>();
            for (int i = 0; i < cadena.length(); i++) {
                char simbolo = cadena.charAt(i);
                String destino = delta(estado, simbolo);
                traza.add(new Paso(estado, simbolo, destino));
                estado = destino;
            }
            return new Resultado(aceptacion.contains(estado), traza, estado);
        }
    }

    // ------------------------------------------------------------------
    // Presentación: traza, tabla y pruebas
    // ------------------------------------------------------------------
    static final String[] VALIDOS = {
            "p", "k", "dfp", "fdfp", "dbk", "p>k", "dfp>p", "p>k>fdfp", "dbk>dfp>k"
    };
    static final String[] INVALIDOS = {
            "", "dfk", "dp", "fdp", "pp", "p>", ">p", "p>>k", "dfpp", "bk"
    };

    static boolean mostrarTraza(AFD afd, String cadena) {
        Resultado r = afd.procesar(cadena);
        System.out.println();
        System.out.println("Cadena: \"" + cadena + "\"  (|w| = " + cadena.length() + ")");
        if (r.traza.isEmpty()) {
            System.out.println("  (cadena vacía: no se lee ningún símbolo)");
        }
        int n = 1;
        for (Paso p : r.traza) {
            String extra = p.destino.equals(afd.trampa) ? "   <- estado trampa" : "";
            System.out.println("  paso " + n + ": δ(" + p.origen + ", " + p.simbolo
                    + ") = " + p.destino + extra);
            n++;
        }
        String motivo;
        if (afd.aceptacion.contains(r.estadoFinal)) {
            motivo = "acción completa (aceptación)";
        } else if (r.estadoFinal.equals(afd.trampa)) {
            motivo = "estado trampa (secuencia inválida)";
        } else {
            motivo = "no es estado de aceptación (combo incompleto)";
        }
        System.out.println("  Estado final: " + r.estadoFinal + " -> " + motivo);
        System.out.println("  Resultado: " + (r.aceptada ? "ACEPTADA" : "RECHAZADA"));
        System.out.println();
        return r.aceptada;
    }

    static void mostrarTabla(AFD afd) {
        char[] columnas = {'d', 'f', 'b', 'p', 'k', '>'};
        String[] orden = {"q0", "q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "qt"};
        System.out.println();
        System.out.println("Tabla de transiciones δ");
        StringBuilder cab = new StringBuilder("        ");
        for (char c : columnas) {
            cab.append(String.format("%5s", c));
        }
        System.out.println(cab);
        for (String q : orden) {
            String marca = (q.equals(afd.inicial) ? "→" : " ")
                    + (afd.aceptacion.contains(q) ? "*" : " ");
            StringBuilder fila = new StringBuilder(marca + " " + String.format("%-4s", q));
            for (char c : columnas) {
                fila.append(String.format("%5s", afd.delta(q, c)));
            }
            System.out.println(fila);
        }
        System.out.println("(→ inicial, * aceptación)");
        System.out.println();
    }

    static void ejecutarPruebas(AFD afd) {
        System.out.println();
        System.out.println("=== BATERÍA DE PRUEBAS ===");
        System.out.println(String.format("%-14s%-12s%-12s%s", "Cadena", "Esperado", "Obtenido", "OK"));
        int total = 0;
        int correctas = 0;
        for (int i = 0; i < VALIDOS.length + INVALIDOS.length; i++) {
            boolean esperado = i < VALIDOS.length;
            String cadena = esperado ? VALIDOS[i] : INVALIDOS[i - VALIDOS.length];
            boolean obtenido = afd.procesar(cadena).aceptada;
            boolean ok = (obtenido == esperado);
            total++;
            if (ok) {
                correctas++;
            }
            System.out.println(String.format("%-14s%-12s%-12s%s",
                    cadena.isEmpty() ? "ε" : cadena,
                    esperado ? "ACEPTADA" : "RECHAZADA",
                    obtenido ? "ACEPTADA" : "RECHAZADA",
                    ok ? "OK" : "FALLO"));
        }
        System.out.println();
        System.out.println("Resultado: " + correctas + "/" + total + " pruebas correctas ("
                + VALIDOS.length + " válidas, " + INVALIDOS.length + " inválidas)");
        System.out.println();
    }

    static void ayuda() {
        System.out.println();
        System.out.println("Comandos:");
        System.out.println("  <cadena>   evalúa la cadena y muestra la traza (símbolos: d f b p k >)");
        System.out.println("  eps        evalúa la cadena vacía ε");
        System.out.println("  pruebas    ejecuta la batería de pruebas");
        System.out.println("  tabla      muestra la tabla de transiciones");
        System.out.println("  ayuda      muestra este mensaje");
        System.out.println("  salir      termina el programa");
        System.out.println("Ejemplos válidos: p, dfp>p, dbk>dfp>k     Inválidos: pp, p>, dfk");
        System.out.println();
    }

    public static void main(String[] args) {
        AFD afd = new AFD();
        Scanner sc = new Scanner(System.in, "UTF-8");
        System.out.println("=== Simulador AFD: combos de juegos de pelea ===");
        System.out.println("Escribe 'ayuda' para ver los comandos.");
        while (true) {
            System.out.print("combo> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String entrada = sc.nextLine().trim();
            if (entrada.equals("salir")) {
                break;
            } else if (entrada.isEmpty() || entrada.equals("ayuda")) {
                ayuda();
            } else if (entrada.equals("pruebas")) {
                ejecutarPruebas(afd);
            } else if (entrada.equals("tabla")) {
                mostrarTabla(afd);
            } else if (entrada.equals("eps")) {
                mostrarTraza(afd, "");
            } else {
                mostrarTraza(afd, entrada);
            }
        }
        System.out.println("Hasta luego.");
        sc.close();
    }
}
