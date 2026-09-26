package com.usac.contacto3d;

import com.usac.contacto3d.ast.ImpresorAst;
import com.usac.contacto3d.ast.Lenguaje;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.constructores.ConstructorAstPig;
import com.usac.contacto3d.constructores.ConstructorAstY;
import com.usac.contacto3d.constructores.ConstructorAstZ;
import com.usac.contacto3d.errores.ErrorCompilacion;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta el analisis de los archivos fuente.
 *
 * Por ahora (fase 3) llega hasta el analisis semantico. Desde consola:
 *
 *     java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador [opciones] archivo...
 *
 *     --tokens  tokens del archivo      --arbol  parse tree del archivo
 *     --ast     AST de cada modulo      --tabla  tabla de simbolos con la memoria
 */
public class Compilador {

    private final ListaErrores errores = new ListaErrores();
    /** El parser del ultimo archivo; hace falta para imprimir su arbol con nombres de regla. */
    private Parser ultimoParser;
    private ParserRuleContext ultimoArbol;
    /* Del archivo que se compila (no de sus imports), para --arbol. */
    private Parser parserPrincipal;
    private ParserRuleContext arbolPrincipal;
    private List<Programa> modulos = List.of();

    public ListaErrores getErrores() {
        return errores;
    }

    /**
     * Analiza un archivo segun su extension y construye su AST. Devuelve null
     * si hubo errores lexicos o sintacticos: sobre un parse tree incompleto el
     * AST no tendria sentido.
     */
    public Programa analizar(Path archivo) throws IOException {
        String nombre = archivo.getFileName().toString();
        errores.setArchivoActual(nombre);

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

    /**
     * Compila un archivo completo hasta el semantico. Un .pig arrastra lo que
     * importa; un .y o .z se analiza solo. Devuelve null si hubo errores antes
     * del semantico (de parseo o de imports): con archivos faltantes o rotos,
     * el semantico reportaria en cascada cosas que no existen.
     */
    public ResultadoSemantico compilar(Path archivo) throws IOException {
        Programa principal = analizar(archivo);
        parserPrincipal = ultimoParser;
        arbolPrincipal = ultimoArbol;
        if (principal == null) {
            return null;
        }
        modulos = principal.getLenguaje() == Lenguaje.PIGLATIN
                ? new ResolvedorImports(errores, this::analizar).resolver(archivo, principal)
                : List.of(principal);
        if (errores.hayErrores()) {
            return null;
        }
        return new AnalizadorSemantico(errores).analizar(modulos);
    }

    /** El lexer del lenguaje segun la extension, ya conectado a la lista de errores. */
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
        List<Path> archivos = new ArrayList<>();

        for (String arg : args) {
            switch (arg) {
                case "--tokens" -> verTokens = true;
                case "--arbol" -> verArbol = true;
                case "--ast" -> verAst = true;
                case "--tabla" -> verTabla = true;
                default -> archivos.add(Path.of(arg));
            }
        }
        if (archivos.isEmpty()) {
            System.err.println("Uso: Compilador [--tokens] [--arbol] [--ast] [--tabla] archivo...");
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
                // INDENT y DEDENT de Y? no estan en el vocabulario del lexer; su texto ya los describe
                String nombre = lexer.getVocabulary().getSymbolicName(t.getType());
                String texto = t.getText().replace("\n", "\\n").replace("\r", "\\r");
                System.out.printf("  %3d:%-3d %-14s %s%n", t.getLine(), t.getCharPositionInLine(),
                        nombre != null ? nombre : texto, texto);
            }
        }
    }
}
