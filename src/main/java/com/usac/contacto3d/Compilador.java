package com.usac.contacto3d;

import com.usac.contacto3d.ast.ImpresorAst;
import com.usac.contacto3d.c3d.GeneradorCuartetas;
import com.usac.contacto3d.c3d.InterpreteCuartetas;
import com.usac.contacto3d.c3d.ListaCuartetas;
import com.usac.contacto3d.ast.Lenguaje;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.constructores.ConstructorAstPig;
import com.usac.contacto3d.constructores.ConstructorAstY;
import com.usac.contacto3d.constructores.ConstructorAstZ;
import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.generador.GeneradorRiscV;
import com.usac.contacto3d.generador.TraductorC;
import com.usac.contacto3d.errores.ErroresLexicos;
import com.usac.contacto3d.errores.ErroresSintacticos;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;
import com.usac.contacto3d.parser.LenguajeYLexer;
import com.usac.contacto3d.parser.LenguajeYParser;
import com.usac.contacto3d.parser.PigLatinLexer;
import com.usac.contacto3d.parser.PigLatinParser;
import com.usac.contacto3d.parser.ZetarianoLexer;
import com.usac.contacto3d.parser.ZetarianoParser;
import com.usac.contacto3d.riscv.Simulador;
import com.usac.contacto3d.semantico.AnalizadorSemantico;
import com.usac.contacto3d.semantico.ResolvedorImports;
import com.usac.contacto3d.semantico.ResultadoSemantico;
import com.usac.contacto3d.simbolos.Simbolo;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Compilador {

    private final ListaErrores errores = new ListaErrores();
    private Parser ultimoParser;
    private ParserRuleContext ultimoArbol;
    private Parser parserPrincipal;
    private ParserRuleContext arbolPrincipal;
    private List<Programa> modulos = List.of();

    public ListaErrores getErrores() {
        return errores;
    }

    public Programa analizar(Path archivo) throws IOException {
        String nombre = archivo.getFileName().toString();
        errores.setArchivoActual(nombre);
        if (!Files.isRegularFile(archivo)) {
            errores.agregar(TipoError.LEXICO, nombre, "No existe el archivo " + archivo, 0, 0);
            return null;
        }

        Lexer lexer = crearLexer(nombre, CharStreams.fromPath(archivo));
        if (lexer == null) {
            errores.agregar(TipoError.LEXICO, nombre,
                    "Extension no soportada. Se esperaba .y, .z o .pig", 0, 0);
            return null;
        }

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ultimoArbol = switch (lexer) {
            case LenguajeYLexer y -> preparar(new LenguajeYParser(tokens)).programa();
            case ZetarianoLexer z -> preparar(new ZetarianoParser(tokens)).programa();
            case PigLatinLexer p -> preparar(new PigLatinParser(tokens)).programa();
            default -> throw new IllegalStateException("Lexer sin parser: " + lexer.getClass());
        };

        if (!errores.parseoExitoso()) {
            return null;
        }
        return switch (ultimoArbol) {
            case LenguajeYParser.ProgramaContext ctx -> new ConstructorAstY(nombre).construir(ctx);
            case ZetarianoParser.ProgramaContext ctx -> new ConstructorAstZ(nombre).construir(ctx);
            case PigLatinParser.ProgramaContext ctx -> new ConstructorAstPig(nombre).construir(ctx);
            default -> throw new IllegalStateException("Arbol sin constructor: " + ultimoArbol.getClass());
        };
    }

    public ResultadoSemantico compilar(Path archivo) throws IOException {
        Programa principal = analizar(archivo);
        parserPrincipal = ultimoParser;
        arbolPrincipal = ultimoArbol;
        if (principal == null) {
            return null;
        }
        modulos = switch (principal.getLenguaje()) {
            case PIGLATIN -> new ResolvedorImports(errores, this::analizar).resolver(archivo, principal);
            case ZETARIANO -> conClasesVecinas(archivo, principal);
            default -> List.of(principal);
        };
        if (errores.hayErrores()) {
            return null;
        }
        ResultadoSemantico resultado = new AnalizadorSemantico(errores).analizar(modulos);
        if (principal.getLenguaje() == Lenguaje.ZETARIANO) {
            // Los errores de las clases vecinas se ven al analizar cada una
            String nombre = archivo.getFileName().toString();
            errores.getErrores().removeIf(e -> !e.getArchivo().equals(nombre));
        }
        return resultado;
    }

    /**
     * Zetariano no tiene import: una clase solo conoce a las otras cuando un .pig las importa.
     * Para analizar un .z solo, se cargan los demás .z de su carpeta, como si el .pig los hubiera
     * importado. Los que no parsean se ignoran (cada uno se parsea con su propia lista de errores).
     */
    private List<Programa> conClasesVecinas(Path archivo, Programa principal) throws IOException {
        List<Programa> lista = new ArrayList<>();
        lista.add(principal);
        Path carpeta = archivo.toAbsolutePath().getParent();
        Path propio = archivo.toRealPath();
        List<Path> vecinos;
        try (var recorrido = Files.list(carpeta)) {
            vecinos = recorrido
                    .filter(p -> p.getFileName().toString().endsWith(".z"))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        }
        for (Path vecino : vecinos) {
            if (vecino.toRealPath().equals(propio)) {
                continue;
            }
            Programa modulo = new Compilador().analizar(vecino);
            if (modulo != null) {
                lista.add(modulo);
            }
        }
        return lista;
    }

    /**
     * Los .pig debajo de {@code base} que importan {@code archivo}, el más cercano primero. Sirve para
     * ejecutar desde un .y o un .z, que no tienen programa principal. Un .pig que no parsea se ignora.
     */
    public static List<Path> programasQueImportan(Path archivo, Path base) throws IOException {
        Path buscado = archivo.toRealPath();
        Path carpeta = buscado.getParent();
        List<Path> programas;
        try (var recorrido = Files.walk(base, 8)) {
            programas = recorrido
                    .filter(p -> p.getFileName().toString().endsWith(".pig"))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .toList();
        }
        List<Path> encontrados = new ArrayList<>();
        for (Path programa : programas) {
            Programa ast = new Compilador().analizar(programa);
            if (ast == null) {
                continue;
            }
            for (Path importado : ResolvedorImports.rutasImportadas(programa, ast)) {
                if (importado.toRealPath().equals(buscado)) {
                    encontrados.add(programa);
                    break;
                }
            }
        }
        encontrados.sort(java.util.Comparator.comparingInt(p -> distancia(carpeta, p.toAbsolutePath().getParent())));
        return encontrados;
    }

    private static int distancia(Path a, Path b) {
        Path comun = a;
        while (comun != null && !b.startsWith(comun)) {
            comun = comun.getParent();
        }
        int subida = comun == null ? a.getNameCount() : a.getNameCount() - comun.getNameCount();
        int bajada = comun == null ? b.getNameCount() : b.getNameCount() - comun.getNameCount();
        return subida + bajada;
    }

    public ListaCuartetas generarCuartetas(ResultadoSemantico resultado) {
        return new GeneradorCuartetas().generar(resultado);
    }

    public String traducirAC(ListaCuartetas cuartetas) {
        return new TraductorC().traducir(cuartetas);
    }

    public String generarRiscV(ListaCuartetas cuartetas) {
        return new GeneradorRiscV().generar(cuartetas);
    }

    private Lexer crearLexer(String nombre, CharStream entrada) {
        Lexer lexer;
        if (nombre.endsWith(".y")) {
            lexer = new LenguajeYLexer(entrada);
        } else if (nombre.endsWith(".z")) {
            lexer = new ZetarianoLexer(entrada);
        } else if (nombre.endsWith(".pig")) {
            lexer = new PigLatinLexer(entrada);
        } else {
            return null;
        }
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ErroresLexicos(errores));
        return lexer;
    }

    private <P extends Parser> P preparar(P parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(new ErroresSintacticos(errores));
        ultimoParser = parser;
        return parser;
    }

    public static void main(String[] args) throws IOException {
        boolean verTokens = false;
        boolean verArbol = false;
        boolean verAst = false;
        boolean verTabla = false;
        boolean verC3d = false;
        boolean ejecutar = false;
        boolean simular = false;
        Path salidaC = null;
        Path salidaRiscV = null;
        List<Path> archivos = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--c" -> salidaC = Path.of(args[++i]);
                case "--riscv" -> salidaRiscV = Path.of(args[++i]);
                case "--simular" -> simular = true;
                case "--tokens" -> verTokens = true;
                case "--arbol" -> verArbol = true;
                case "--ast" -> verAst = true;
                case "--tabla" -> verTabla = true;
                case "--c3d" -> verC3d = true;
                case "--ejecutar" -> ejecutar = true;
                default -> archivos.add(Path.of(arg));
            }
        }
        if (archivos.isEmpty()) {
            System.err.println("Uso: Compilador [--tokens] [--arbol] [--ast] [--tabla] [--c3d] [--ejecutar] [--c salida.c] [--riscv salida.s] [--simular] archivo...");
            System.exit(2);
        }

        boolean todoBien = true;
        for (Path archivo : archivos) {
            Compilador compilador = new Compilador();
            System.out.println("== " + archivo);

            if (verTokens) {
                Lexer lexer = compilador.crearLexer(archivo.toString(), CharStreams.fromPath(archivo));
                if (lexer != null) {
                    imprimirTokens(lexer);
                }
            }

            ResultadoSemantico resultado = compilador.compilar(archivo);
            if (verArbol && compilador.arbolPrincipal != null) {
                System.out.println(compilador.arbolPrincipal.toStringTree(compilador.parserPrincipal));
            }
            if (verAst) {
                compilador.modulos.forEach(m -> System.out.print(ImpresorAst.dibujar(m)));
            }
            if (verTabla && resultado != null) {
                imprimirTabla(resultado);
            }
            if ((verC3d || ejecutar || simular || salidaC != null || salidaRiscV != null) && resultado != null && !compilador.getErrores().hayErrores()) {
                ListaCuartetas cuartetas = compilador.generarCuartetas(resultado);
                if (verC3d) {
                    System.out.print(cuartetas);
                }
                if (salidaC != null) {
                    if (salidaC.getParent() != null) {
                        java.nio.file.Files.createDirectories(salidaC.getParent());
                    }
                    java.nio.file.Files.writeString(salidaC, compilador.traducirAC(cuartetas));
                    System.out.println("C generado en " + salidaC);
                }
                String entrada = ejecutar || simular ? new String(System.in.readAllBytes()) : "";
                if (salidaRiscV != null || simular) {
                    String ensamblador = compilador.generarRiscV(cuartetas);
                    if (salidaRiscV != null) {
                        if (salidaRiscV.getParent() != null) {
                            java.nio.file.Files.createDirectories(salidaRiscV.getParent());
                        }
                        java.nio.file.Files.writeString(salidaRiscV, ensamblador);
                        System.out.println("RISC-V generado en " + salidaRiscV);
                    }
                    if (simular) {
                        System.out.println("-- salida --");
                        System.out.print(Simulador.ejecutar(ensamblador, entrada));
                        System.out.println("-- fin --");
                    }
                }
                if (ejecutar) {
                    System.out.println("-- salida --");
                    System.out.print(InterpreteCuartetas.ejecutar(cuartetas, entrada));
                    System.out.println("-- fin --");
                }
            }

            compilador.getErrores().ordenar();
            List<ErrorCompilacion> lista = compilador.getErrores().getErrores();
            System.out.println(lista.size() + " error(es)");
            lista.forEach(e -> System.out.println("  " + e));
            todoBien &= lista.isEmpty();
        }
        System.exit(todoBien ? 0 : 1);
    }

    private static void imprimirTabla(ResultadoSemantico resultado) {
        System.out.printf("  %-10s %-24s %-18s %-24s %-10s %-18s %s%n",
                "CATEGORIA", "NOMBRE", "TIPO", "AMBITO", "ZONA[POS]", "ARCHIVO:LINEA", "DETALLE");
        for (Simbolo s : resultado.tabla().getHistorial()) {
            String zona = s.getAlmacenamiento() == Simbolo.Almacenamiento.NINGUNO ? "-"
                    : s.getAlmacenamiento() + "[" + s.getPosicion() + "]";
            System.out.printf("  %-10s %-24s %-18s %-24s %-10s %-18s %s%n",
                    s.getCategoria().getDescripcion(), s.getNombre(), s.getTipo(), s.getNombreAmbito(), zona,
                    s.getArchivo() + ":" + s.getLinea(), s.getDetalle());
        }
        System.out.println("  Zona global: " + resultado.tamanioGlobales() + " celda(s)"
                + (resultado.principal() == null ? ""
                : "   |   marco de MAIOR>: " + resultado.principal().getTamanioMarco() + " celda(s)"));
    }

    private static void imprimirTokens(Lexer lexer) {
        for (Token t = lexer.nextToken(); t.getType() != Token.EOF; t = lexer.nextToken()) {
            if (t.getChannel() == Token.DEFAULT_CHANNEL) {
                String nombre = lexer.getVocabulary().getSymbolicName(t.getType());
                String texto = t.getText().replace("\n", "\\n").replace("\r", "\\r");
                System.out.printf("  %3d:%-3d %-14s %s%n", t.getLine(), t.getCharPositionInLine(),
                        nombre != null ? nombre : texto, texto);
            }
        }
    }
}
