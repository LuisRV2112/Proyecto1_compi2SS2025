package com.usac.contacto3d.constructores;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionArreglo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionVariable;
import com.usac.contacto3d.ast.declaraciones.Funcion;
import com.usac.contacto3d.ast.declaraciones.Importacion;
import com.usac.contacto3d.ast.declaraciones.Parametro;
import com.usac.contacto3d.ast.expresiones.Acceso;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.expresiones.IncrementoDecremento;
import com.usac.contacto3d.ast.expresiones.Leer;
import com.usac.contacto3d.ast.expresiones.Literal;
import com.usac.contacto3d.ast.expresiones.LiteralLista;
import com.usac.contacto3d.ast.expresiones.LlamadaFuncion;
import com.usac.contacto3d.ast.expresiones.NuevoObjeto;
import com.usac.contacto3d.ast.expresiones.OperacionBinaria;
import com.usac.contacto3d.ast.expresiones.OperacionUnaria;
import com.usac.contacto3d.ast.expresiones.Operador;
import com.usac.contacto3d.ast.expresiones.Sufijo;
import com.usac.contacto3d.ast.expresiones.SufijoAtributo;
import com.usac.contacto3d.ast.expresiones.SufijoIndice;
import com.usac.contacto3d.ast.expresiones.SufijoMetodo;
import com.usac.contacto3d.ast.instrucciones.Asignacion;
import com.usac.contacto3d.ast.instrucciones.ControlCiclo;
import com.usac.contacto3d.ast.instrucciones.Hacer;
import com.usac.contacto3d.ast.instrucciones.Imprimir;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.ast.instrucciones.Mientras;
import com.usac.contacto3d.ast.instrucciones.Para;
import com.usac.contacto3d.ast.instrucciones.Rama;
import com.usac.contacto3d.ast.instrucciones.Retorno;
import com.usac.contacto3d.ast.instrucciones.Si;
import com.usac.contacto3d.parser.PigLatinBaseVisitor;
import com.usac.contacto3d.parser.PigLatinParser.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.usac.contacto3d.constructores.Ayudante.columna;
import static com.usac.contacto3d.constructores.Ayudante.linea;

public class ConstructorAstPig extends PigLatinBaseVisitor<Nodo> {

    private final String archivo;

    public ConstructorAstPig(String archivo) {
        this.archivo = archivo;
    }

    public Programa construir(ProgramaContext ctx) {
        List<Importacion> importaciones = ctx.importacion().stream().map(this::importacion).toList();
        List<Instruccion> globales = declaraciones(ctx.declaracion());
        List<Funcion> funciones = ctx.funcion().stream().map(f -> (Funcion) visit(f)).toList();
        List<Instruccion> principal = instrucciones(ctx.instruccion());
        Programa programa = Programa.dePigLatin(importaciones, globales, funciones, principal,
                linea(ctx), columna(ctx));
        Ayudante.asignarArchivo(programa, archivo);
        return programa;
    }

    private Importacion importacion(ImportacionContext ctx) {
        List<String> partes = ctx.ID().stream().map(TerminalNode::getText).toList();
        return new Importacion(partes, linea(ctx), columna(ctx));
    }

    private List<Instruccion> declaraciones(List<DeclaracionContext> contextos) {
        return contextos.stream().map(this::visit).filter(Objects::nonNull)
                .map(Instruccion.class::cast).toList();
    }

    @Override
    public Nodo visitDeclVariable(DeclVariableContext ctx) {
        return visit(ctx.declaracionVariable());
    }

    @Override
    public Nodo visitDeclArreglo(DeclArregloContext ctx) {
        return declaracionArreglo(ctx.declaracionArreglo());
    }

    @Override
    public Nodo visitDeclStructuraObsoleta(DeclStructuraObsoletaContext ctx) {
        return null;
    }

    @Override
    public Nodo visitVarPrimitiva(VarPrimitivaContext ctx) {
        return new DeclaracionVariable(ctx.ID().getText(), Tipo.desdePalabra(ctx.tipoPrimitivo().getText()),
                expr(ctx.expr()), linea(ctx.ID()), columna(ctx.ID()));
    }

    @Override
    public Nodo visitVarBoolSinTipo(VarBoolSinTipoContext ctx) {
        TerminalNode literal = ctx.VERUM() != null ? ctx.VERUM() : ctx.FALSUS();
        Literal valor = new Literal(ctx.VERUM() != null, Tipo.BOOLEANO, linea(literal), columna(literal));
        return new DeclaracionVariable(ctx.ID().getText(), Tipo.BOOLEANO, valor, true,
                linea(ctx.ID()), columna(ctx.ID()));
    }

    @Override
    public Nodo visitVarEstructura(VarEstructuraContext ctx) {
        return new DeclaracionVariable(ctx.ID(0).getText(), Tipo.estructura(ctx.ID(1).getText()),
                listaValores(ctx.listaValores()), linea(ctx.ID(0)), columna(ctx.ID(0)));
    }

    @Override
    public Nodo visitVarSinValor(VarSinValorContext ctx) {
        return new DeclaracionVariable(ctx.ID(0).getText(), Tipo.estructura(ctx.ID(1).getText()), null,
                linea(ctx.ID(0)), columna(ctx.ID(0)));
    }

    @Override
    public Nodo visitVarObjeto(VarObjetoContext ctx) {
        NuevoObjeto objeto = nuevoObjeto(ctx.nuevoObjeto());
        return new DeclaracionVariable(ctx.ID().getText(), Tipo.objeto(objeto.getClase()), objeto,
                linea(ctx.ID()), columna(ctx.ID()));
    }

    private DeclaracionArreglo declaracionArreglo(DeclaracionArregloContext ctx) {
        List<Expresion> dimensiones = ctx.dimension().stream().map(d -> expr(d.expr())).toList();
        Expresion valor = ctx.listaValores() == null ? null : listaValores(ctx.listaValores());
        return new DeclaracionArreglo(ctx.ID().getText(), tipo(ctx.tipo()), dimensiones, valor,
                linea(ctx.ID()), columna(ctx.ID()));
    }

    private LiteralLista listaValores(ListaValoresContext ctx) {
        List<Expresion> valores = ctx.valor().stream().map(v -> switch (v) {
            case ValorExprContext e -> expr(e.expr());
            case ValorListaContext l -> (Expresion) listaValores(l.listaValores());
            default -> throw new IllegalStateException("Valor no contemplado: " + v.getText());
        }).toList();
        return new LiteralLista(valores, linea(ctx), columna(ctx));
    }

    private Tipo tipo(TipoContext ctx) {
        return Tipo.desdePalabra(ctx.getText());
    }

    @Override
    public Nodo visitFuncionActio(FuncionActioContext ctx) {
        return new Funcion(ctx.ID().getText(), parametros(ctx.listaParametros()), Tipo.VACIO,
                cuerpoFuncion(ctx.cuerpoFuncion()), linea(ctx.ID()), columna(ctx.ID()));
    }

    @Override
    public Nodo visitFuncionRatio(FuncionRatioContext ctx) {
        return new Funcion(ctx.ID().getText(), parametros(ctx.listaParametros()), tipo(ctx.tipo()),
                cuerpoFuncion(ctx.cuerpoFuncion()), linea(ctx.ID()), columna(ctx.ID()));
    }

    private List<Parametro> parametros(ListaParametrosContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.parametro().stream()
                .map(p -> new Parametro(p.ID().getText(), tipo(p.tipo()), false, linea(p.ID()), columna(p.ID())))
                .toList();
    }

    private List<Instruccion> cuerpoFuncion(CuerpoFuncionContext ctx) {
        List<Instruccion> cuerpo = new ArrayList<>();
        if (ctx.bloqueVariables() != null) {
            cuerpo.addAll(declaraciones(ctx.bloqueVariables().declaracion()));
        }
        cuerpo.addAll(instrucciones(ctx.instruccion()));
        return cuerpo;
    }

    private List<Instruccion> bloque(BloqueContext ctx) {
        return instrucciones(ctx.instruccion());
    }

    private List<Instruccion> instrucciones(List<InstruccionContext> contextos) {
        return contextos.stream().map(this::visit).map(Instruccion.class::cast).toList();
    }

    @Override
    public Nodo visitInstDeclVariable(InstDeclVariableContext ctx) {
        return visit(ctx.declaracionVariable());
    }

    @Override
    public Nodo visitInstDeclArreglo(InstDeclArregloContext ctx) {
        return declaracionArreglo(ctx.declaracionArreglo());
    }

    @Override
    public Nodo visitInstAsignacion(InstAsignacionContext ctx) {
        return visit(ctx.asignacion());
    }

    @Override
    public Nodo visitAsignacionExpr(AsignacionExprContext ctx) {
        return new Asignacion(destino(ctx.acceso()), null, expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitAsignacionLista(AsignacionListaContext ctx) {
        return new Asignacion(destino(ctx.acceso()), null, listaValores(ctx.listaValores()),
                linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstIncremento(InstIncrementoContext ctx) {
        return incremento(ctx.incremento());
    }

    @Override
    public Nodo visitInstLlamada(InstLlamadaContext ctx) {
        return visit(ctx.acceso());
    }

    @Override
    public Nodo visitInstSi(InstSiContext ctx) {
        SentenciaSiContext si = ctx.sentenciaSi();
        List<Rama> ramas = new ArrayList<>();
        ramas.add(new Rama(expr(si.expr()), bloque(si.bloque()), linea(si), columna(si)));
        for (RamaAliterSiContext aliter : si.ramaAliterSi()) {
            ramas.add(new Rama(expr(aliter.expr()), bloque(aliter.bloque()), linea(aliter), columna(aliter)));
        }
        if (si.ramaAliter() != null) {
            RamaAliterContext aliter = si.ramaAliter();
            ramas.add(new Rama(null, bloque(aliter.bloque()), linea(aliter), columna(aliter)));
        }
        return new Si(ramas, linea(si), columna(si));
    }

    @Override
    public Nodo visitInstDum(InstDumContext ctx) {
        SentenciaDumContext dum = ctx.sentenciaDum();
        return new Mientras(expr(dum.expr()), bloque(dum.bloque()), linea(dum), columna(dum));
    }

    @Override
    public Nodo visitInstFacere(InstFacereContext ctx) {
        SentenciaFacereContext facere = ctx.sentenciaFacere();
        return new Hacer(bloque(facere.bloque()), expr(facere.expr()), linea(facere), columna(facere));
    }

    @Override
    public Nodo visitInstPer(InstPerContext ctx) {
        SentenciaPerContext per = ctx.sentenciaPer();
        Instruccion inicio = switch (per.inicioPer()) {
            case InicioDeclaracionContext i -> new DeclaracionVariable(i.ID().getText(), tipo(i.tipo()),
                    expr(i.expr()), linea(i.ID()), columna(i.ID()));
            case InicioAsignacionContext i -> new Asignacion(destino(i.acceso()), null, expr(i.expr()),
                    linea(i), columna(i));
            default -> throw new IllegalStateException("Inicio de per no contemplado");
        };
        Instruccion actualizacion = switch (per.actualizacionPer()) {
            case ActualizacionIncrementoContext a -> incremento(a.incremento());
            case ActualizacionAsignacionContext a -> new Asignacion(destino(a.acceso()), null, expr(a.expr()),
                    linea(a), columna(a));
            default -> throw new IllegalStateException("Actualizacion de per no contemplada");
        };
        return new Para(List.of(inicio), expr(per.expr()), List.of(actualizacion), bloque(per.bloque()),
                linea(per), columna(per));
    }

    @Override
    public Nodo visitInstPerge(InstPergeContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.CONTINUAR, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstInterrumpe(InstInterrumpeContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.ROMPER, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstReddere(InstReddereContext ctx) {
        return new Retorno(expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstImprimir(InstImprimirContext ctx) {
        return new Imprimir(ctx.expr().stream().map(this::expr).toList(), true, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstLeerVariable(InstLeerVariableContext ctx) {
        return new Leer(destino(ctx.acceso()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitInstLeer(InstLeerContext ctx) {
        return new Leer(null, linea(ctx), columna(ctx));
    }

    private IncrementoDecremento incremento(IncrementoContext ctx) {
        return new IncrementoDecremento(destino(ctx.acceso()), ctx.INCREMENTO() != null, false,
                linea(ctx), columna(ctx));
    }

    private Acceso destino(AccesoContext ctx) {
        Nodo nodo = visit(ctx);
        return nodo instanceof LlamadaFuncion llamada
                ? Acceso.deLlamada(llamada, List.of(), llamada.getLinea(), llamada.getColumna())
                : (Acceso) nodo;
    }

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
        return expr(ctx.expr());
    }

    @Override
    public Nodo visitExprIncremento(ExprIncrementoContext ctx) {
        return incremento(ctx.incremento());
    }

    @Override
    public Nodo visitExprUnaria(ExprUnariaContext ctx) {
        return new OperacionUnaria(Operador.unario(ctx.getChild(0).getText()), expr(ctx.expr()),
                linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprMulDiv(ExprMulDivContext ctx) {
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
    public Nodo visitExprLiteral(ExprLiteralContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public Nodo visitExprAcceso(ExprAccesoContext ctx) {
        return visit(ctx.acceso());
    }

    @Override
    public Nodo visitExprNuevoObjeto(ExprNuevoObjetoContext ctx) {
        return nuevoObjeto(ctx.nuevoObjeto());
    }

    private NuevoObjeto nuevoObjeto(NuevoObjetoContext ctx) {
        return new NuevoObjeto(ctx.ID().getText(), expresiones(ctx.listaExpresiones()), linea(ctx), columna(ctx));
    }

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
        return new Literal(ctx.VERUM() != null, Tipo.BOOLEANO, linea(ctx), columna(ctx));
    }

    @Override
    protected Nodo aggregateResult(Nodo acumulado, Nodo siguiente) {
        return acumulado != null ? acumulado : siguiente;
    }

    @Override
    public Nodo visitTerminal(TerminalNode nodo) {
        return null;
    }
}
