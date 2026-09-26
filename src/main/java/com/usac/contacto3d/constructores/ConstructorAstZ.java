package com.usac.contacto3d.constructores;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionClase;
import com.usac.contacto3d.ast.declaraciones.DeclaracionConstructor;
import com.usac.contacto3d.ast.declaraciones.DeclaracionMetodo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionVariable;
import com.usac.contacto3d.ast.declaraciones.Parametro;
import com.usac.contacto3d.ast.expresiones.Acceso;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.expresiones.IncrementoDecremento;
import com.usac.contacto3d.ast.expresiones.Leer;
import com.usac.contacto3d.ast.expresiones.Literal;
import com.usac.contacto3d.ast.expresiones.LiteralLista;
import com.usac.contacto3d.ast.expresiones.LlamadaFuncion;
import com.usac.contacto3d.ast.expresiones.NuevoArreglo;
import com.usac.contacto3d.ast.expresiones.NuevoObjeto;
import com.usac.contacto3d.ast.expresiones.OperacionBinaria;
import com.usac.contacto3d.ast.expresiones.OperacionUnaria;
import com.usac.contacto3d.ast.expresiones.Operador;
import com.usac.contacto3d.ast.expresiones.Sufijo;
import com.usac.contacto3d.ast.expresiones.SufijoAtributo;
import com.usac.contacto3d.ast.expresiones.SufijoIndice;
import com.usac.contacto3d.ast.expresiones.SufijoMetodo;
import com.usac.contacto3d.ast.expresiones.Ternario;
import com.usac.contacto3d.ast.instrucciones.Asignacion;
import com.usac.contacto3d.ast.instrucciones.Bloque;
import com.usac.contacto3d.ast.instrucciones.Caso;
import com.usac.contacto3d.ast.instrucciones.ControlCiclo;
import com.usac.contacto3d.ast.instrucciones.Elegir;
import com.usac.contacto3d.ast.instrucciones.Hacer;
import com.usac.contacto3d.ast.instrucciones.Imprimir;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.ast.instrucciones.Mientras;
import com.usac.contacto3d.ast.instrucciones.Para;
import com.usac.contacto3d.ast.instrucciones.Rama;
import com.usac.contacto3d.ast.instrucciones.Retorno;
import com.usac.contacto3d.ast.instrucciones.Si;
import com.usac.contacto3d.parser.ZetarianoBaseVisitor;
import com.usac.contacto3d.parser.ZetarianoParser.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import static com.usac.contacto3d.constructores.Ayudante.columna;
import static com.usac.contacto3d.constructores.Ayudante.linea;

/**
 * Convierte el parse tree de Zetariano en el AST comun.
 *
 * Dos normalizaciones propias de este lenguaje:
 *  - "int x, y = 5;" se separa en una DeclaracionVariable por nombre
 *  - "if / else if / else" encadenados se aplanan en un solo Si con varias
 *    ramas, igual que el si/sino de Y?
 */
public class ConstructorAstZ extends ZetarianoBaseVisitor<Nodo> {

    private final String archivo;

    public ConstructorAstZ(String archivo) {
        this.archivo = archivo;
    }

    public Programa construir(ProgramaContext ctx) {
        Programa programa = Programa.deZetariano(clase(ctx.clase()), linea(ctx), columna(ctx));
        Ayudante.asignarArchivo(programa, archivo);
        return programa;
    }

    /* ---------- clase ---------- */

    private DeclaracionClase clase(ClaseContext ctx) {
        List<DeclaracionVariable> atributos = new ArrayList<>();
        List<DeclaracionConstructor> constructores = new ArrayList<>();
        List<DeclaracionMetodo> metodos = new ArrayList<>();

        for (MiembroContext miembro : ctx.miembro()) {
            switch (miembro) {
                case MiembroConstructorContext m -> constructores.add(new DeclaracionConstructor(
                        m.ID().getText(), parametros(m.listaParametros()), bloque(m.bloque()),
                        linea(m.ID()), columna(m.ID())));
                case MiembroMetodoVoidContext m -> metodos.add(new DeclaracionMetodo(
                        m.ID().getText(), parametros(m.listaParametros()), Tipo.VACIO, bloque(m.bloque()),
                        linea(m.ID()), columna(m.ID())));
                case MiembroConTipoContext m -> {
                    Tipo tipo = tipo(m.tipo());
                    switch (m.restoMiembro()) {
                        case RestoMetodoContext r -> metodos.add(new DeclaracionMetodo(
                                m.ID().getText(), parametros(r.listaParametros()), tipo, bloque(r.bloque()),
                                linea(m.ID()), columna(m.ID())));
                        case RestoAtributoContext r -> {
                            atributos.add(new DeclaracionVariable(m.ID().getText(), tipo, expr(r.expr()),
                                    linea(m.ID()), columna(m.ID())));
                            for (DeclaradorContext d : r.declarador()) {
                                atributos.add(declarador(tipo, d));
                            }
                        }
                        default -> throw new IllegalStateException("Miembro no contemplado: " + m.getText());
                    }
                }
                default -> throw new IllegalStateException("Miembro no contemplado: " + miembro.getText());
            }
        }
        return new DeclaracionClase(ctx.ID().getText(), atributos, constructores, metodos,
                linea(ctx.ID()), columna(ctx.ID()));
    }

    private List<Parametro> parametros(ListaParametrosContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.parametro().stream().map(p -> {
            Tipo tipo = tipo(p.tipo());
            // Como en Java: arreglos y objetos pasan por referencia, sin marca
            boolean referencia = tipo.esArreglo() || tipo.esObjeto();
            return new Parametro(p.ID().getText(), tipo, referencia, linea(p.ID()), columna(p.ID()));
        }).toList();
    }

    /** En Zetariano un tipo con nombre siempre es una clase: no hay estructuras. */
    private Tipo tipo(TipoContext ctx) {
        Tipo tipo = tipoBase(ctx.tipoBase());
        if (ctx.CORCH_A().isEmpty()) {
            return tipo;
        }
        List<Integer> dimensiones = new ArrayList<>();
        ctx.CORCH_A().forEach(c -> dimensiones.add(null));   // el tamanio llega con el new
        return Tipo.arreglo(tipo, dimensiones);
    }

    private Tipo tipoBase(TipoBaseContext ctx) {
        Tipo tipo = Tipo.desdePalabra(ctx.getText());
        return tipo.esEstructura() ? Tipo.objeto(tipo.getNombre()) : tipo;
    }

    /* ---------- instrucciones ---------- */

    private List<Instruccion> bloque(BloqueContext ctx) {
        return instrucciones(ctx.instruccion());
    }

    private List<Instruccion> instrucciones(List<InstruccionContext> contextos) {
        List<Instruccion> lista = new ArrayList<>();
        for (InstruccionContext ctx : contextos) {
            if (ctx instanceof InstDeclaracionContext d) {
                lista.addAll(declaraciones(d.declaracionVariable()));
            } else {
                lista.add((Instruccion) visit(ctx));
            }
        }
        return lista;
    }

    /** Cuerpo de if/while/for: sin llaves es una sola instruccion. */
    private List<Instruccion> cuerpo(InstruccionContext ctx) {
        return ctx instanceof InstBloqueContext b ? bloque(b.bloque()) : instrucciones(List.of(ctx));
    }

    private List<Instruccion> declaraciones(DeclaracionVariableContext ctx) {
        Tipo tipo = tipo(ctx.tipo());
        return ctx.declarador().stream().map(d -> (Instruccion) declarador(tipo, d)).toList();
    }

    private DeclaracionVariable declarador(Tipo tipo, DeclaradorContext ctx) {
        return new DeclaracionVariable(ctx.ID().getText(), tipo, expr(ctx.expr()),
                linea(ctx.ID()), columna(ctx.ID()));
    }

    @Override
    public Nodo visitInstBloque(InstBloqueContext ctx) {
        return new Bloque(bloque(ctx.bloque()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstAsignacion(InstAsignacionContext ctx) {
        return asignacion(ctx.asignacion());
    }

    @Override
    public Nodo visitInstIncremento(InstIncrementoContext ctx) {
        return visit(ctx.incremento());
    }

    @Override
    public Nodo visitInstLlamada(InstLlamadaContext ctx) {
        return visit(ctx.acceso());
    }

    @Override
    public Nodo visitInstIf(InstIfContext ctx) {
        List<Rama> ramas = new ArrayList<>();
        SentenciaIfContext actual = ctx.sentenciaIf();
        while (true) {
            ramas.add(new Rama(expr(actual.expr()), cuerpo(actual.instruccion(0)), linea(actual), columna(actual)));
            if (actual.ELSE() == null) {
                break;
            }
            InstruccionContext sino = actual.instruccion(1);
            if (sino instanceof InstIfContext elseIf) {
                actual = elseIf.sentenciaIf();
                continue;
            }
            ramas.add(new Rama(null, cuerpo(sino), linea(sino), columna(sino)));
            break;
        }
        return new Si(ramas, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstSwitch(InstSwitchContext ctx) {
        SentenciaSwitchContext sw = ctx.sentenciaSwitch();
        List<Caso> casos = sw.seccionSwitch().stream().map(seccion -> switch (seccion) {
            case SeccionCaseContext s -> new Caso(expr(s.expr()), instrucciones(s.instruccion()), linea(s), columna(s));
            case SeccionDefaultContext s -> new Caso(null, instrucciones(s.instruccion()), linea(s), columna(s));
            default -> throw new IllegalStateException("Seccion no contemplada: " + seccion.getText());
        }).toList();
        return new Elegir(expr(sw.expr()), casos, linea(sw), columna(sw));
    }

    @Override
    public Nodo visitInstFor(InstForContext ctx) {
        SentenciaForContext para = ctx.sentenciaFor();
        List<Instruccion> inicio = switch (para.inicioFor()) {
            case null -> List.of();
            case InicioDeclaracionContext i -> declaraciones(i.declaracionVariable());
            case InicioAsignacionesContext i -> actualizaciones(i.listaActualizacion());
            default -> throw new IllegalStateException("Inicio de for no contemplado");
        };
        return new Para(inicio, expr(para.expr()), actualizaciones(para.listaActualizacion()),
                cuerpo(para.instruccion()), linea(para), columna(para));
    }

    private List<Instruccion> actualizaciones(ListaActualizacionContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.actualizacion().stream().map(a -> (Instruccion) switch (a) {
            case ActualizacionAsignacionContext x -> asignacion(x.asignacion());
            case ActualizacionIncrementoContext x -> visit(x.incremento());
            default -> throw new IllegalStateException("Actualizacion no contemplada");
        }).toList();
    }

    @Override
    public Nodo visitInstWhile(InstWhileContext ctx) {
        SentenciaWhileContext w = ctx.sentenciaWhile();
        return new Mientras(expr(w.expr()), cuerpo(w.instruccion()), linea(w), columna(w));
    }

    @Override
    public Nodo visitInstDoWhile(InstDoWhileContext ctx) {
        SentenciaDoWhileContext d = ctx.sentenciaDoWhile();
        return new Hacer(cuerpo(d.instruccion()), expr(d.expr()), linea(d), columna(d));
    }

    @Override
    public Nodo visitInstPrintln(InstPrintlnContext ctx) {
        List<Expresion> valores = ctx.expr() == null ? List.of() : List.of(expr(ctx.expr()));
        return new Imprimir(valores, true, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstPrint(InstPrintContext ctx) {
        return new Imprimir(List.of(expr(ctx.expr())), false, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstReadln(InstReadlnContext ctx) {
        return new Leer(null, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstBreak(InstBreakContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.ROMPER, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstContinue(InstContinueContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.CONTINUAR, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstReturn(InstReturnContext ctx) {
        return new Retorno(expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    private Asignacion asignacion(AsignacionContext ctx) {
        Operador operador = Operador.deAsignacion(ctx.getChild(1).getText());
        return new Asignacion(destino(ctx.acceso()), operador, expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitIncrementoPostfijo(IncrementoPostfijoContext ctx) {
        return new IncrementoDecremento(destino(ctx.acceso()), ctx.INCREMENTO() != null, false,
                linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitIncrementoPrefijo(IncrementoPrefijoContext ctx) {
        return new IncrementoDecremento(destino(ctx.acceso()), ctx.INCREMENTO() != null, true,
                linea(ctx), columna(ctx));
    }

    /**
     * Lado izquierdo de una asignacion o un incremento. La gramatica deja pasar
     * "f() = 3": se envuelve en un Acceso para que el semantico lo reporte como
     * no asignable en vez de fallar aqui.
     */
    private Acceso destino(AccesoContext ctx) {
        Nodo nodo = visit(ctx);
        return nodo instanceof LlamadaFuncion llamada
                ? Acceso.deLlamada(llamada, List.of(), llamada.getLinea(), llamada.getColumna())
                : (Acceso) nodo;
    }

    /* ---------- expresiones ---------- */

    private Expresion expr(ExprContext ctx) {
        return ctx == null ? null : (Expresion) visit(ctx);
    }

    private List<Expresion> expresiones(ListaExpresionesContext ctx) {
        return ctx == null ? List.of() : ctx.expr().stream().map(this::expr).toList();
    }

    private Expresion binaria(ParserRuleContext ctx, ExprContext izquierda, ExprContext derecha) {
        TerminalNode operador = Ayudante.operador(ctx);
        return new OperacionBinaria(Operador.binario(operador.getText()), expr(izquierda), expr(derecha),
                linea(operador), columna(operador));
    }

    @Override
    public Nodo visitExprAgrupacion(ExprAgrupacionContext ctx) {
        return expr(ctx.expr());   // los parentesis ya fijaron la precedencia: no dejan nodo
    }

    @Override
    public Nodo visitExprIncremento(ExprIncrementoContext ctx) {
        return visit(ctx.incremento());
    }

    @Override
    public Nodo visitExprUnaria(ExprUnariaContext ctx) {
        return new OperacionUnaria(Operador.unario(ctx.getChild(0).getText()), expr(ctx.expr()),
                linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprMulDivMod(ExprMulDivModContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprSumRes(ExprSumResContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprRelacional(ExprRelacionalContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprIgualdad(ExprIgualdadContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprAnd(ExprAndContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprOr(ExprOrContext ctx) {
        return binaria(ctx, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public Nodo visitExprTernario(ExprTernarioContext ctx) {
        return new Ternario(expr(ctx.expr(0)), expr(ctx.expr(1)), expr(ctx.expr(2)),
                linea(ctx.INTERROGACION()), columna(ctx.INTERROGACION()));
    }

    @Override
    public Nodo visitExprLiteral(ExprLiteralContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public Nodo visitExprAcceso(ExprAccesoContext ctx) {
        return visit(ctx.acceso());
    }

    @Override
    public Nodo visitExprNuevoObjeto(ExprNuevoObjetoContext ctx) {
        return new NuevoObjeto(ctx.ID().getText(), expresiones(ctx.listaExpresiones()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprNuevoArreglo(ExprNuevoArregloContext ctx) {
        List<Expresion> dimensiones = ctx.expr().stream().map(this::expr).toList();
        return new NuevoArreglo(tipoBase(ctx.tipoBase()), dimensiones, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprArregloLiteral(ExprArregloLiteralContext ctx) {
        return new LiteralLista(expresiones(ctx.listaExpresiones()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprReadln(ExprReadlnContext ctx) {
        return new Leer(null, linea(ctx), columna(ctx));
    }

    /** Una llamada sola, sin nada despues, queda como LlamadaFuncion y no como Acceso. */
    @Override
    public Nodo visitAcceso(AccesoContext ctx) {
        List<Sufijo> sufijos = ctx.sufijo().stream().map(this::sufijo).toList();
        int linea = linea(ctx);
        int columna = columna(ctx);
        return switch (ctx.raizAcceso()) {
            case RaizLlamadaContext r -> {
                LlamadaFuncion llamada = new LlamadaFuncion(r.ID().getText(),
                        expresiones(r.listaExpresiones()), linea, columna);
                yield sufijos.isEmpty() ? llamada : Acceso.deLlamada(llamada, sufijos, linea, columna);
            }
            case RaizIdContext r -> Acceso.deVariable(r.ID().getText(), sufijos, linea, columna);
            case RaizThisContext r -> Acceso.deThis(sufijos, linea, columna);
            default -> throw new IllegalStateException("Raiz de acceso no contemplada: " + ctx.getText());
        };
    }

    private Sufijo sufijo(SufijoContext ctx) {
        return switch (ctx) {
            case SufijoMetodoContext s -> new SufijoMetodo(s.ID().getText(), expresiones(s.listaExpresiones()),
                    linea(s.ID()), columna(s.ID()));
            case SufijoAtributoContext s -> new SufijoAtributo(s.ID().getText(), linea(s.ID()), columna(s.ID()));
            case SufijoIndiceContext s -> new SufijoIndice(expr(s.expr()), linea(s), columna(s));
            default -> throw new IllegalStateException("Sufijo no contemplado: " + ctx.getText());
        };
    }

    @Override
    public Nodo visitLitEntero(LitEnteroContext ctx) {
        return new Literal(Ayudante.entero(ctx.getText()), Tipo.ENTERO, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitDecimal(LitDecimalContext ctx) {
        return new Literal(Double.parseDouble(ctx.getText()), Tipo.FLOTANTE, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitCadena(LitCadenaContext ctx) {
        return new Literal(Ayudante.cadena(ctx.getText()), Tipo.CADENA, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitCaracter(LitCaracterContext ctx) {
        return new Literal(Ayudante.caracter(ctx.getText()), Tipo.CARACTER, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitBooleano(LitBooleanoContext ctx) {
        return new Literal(ctx.TRUE() != null, Tipo.BOOLEANO, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitNulo(LitNuloContext ctx) {
        return new Literal(null, Tipo.NULO, linea(ctx), columna(ctx));
    }

    /* ---------- recorrido por defecto ---------- */

    // Por si alguna regla cae en visitChildren: que los tokens sueltos no pisen el resultado
    @Override
    protected Nodo aggregateResult(Nodo acumulado, Nodo siguiente) {
        return acumulado != null ? acumulado : siguiente;
    }

    @Override
    public Nodo visitTerminal(TerminalNode nodo) {
        return null;
    }
}
