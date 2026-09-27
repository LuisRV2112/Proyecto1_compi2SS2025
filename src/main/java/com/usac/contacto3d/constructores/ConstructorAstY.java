package com.usac.contacto3d.constructores;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.declaraciones.CampoEstructura;
import com.usac.contacto3d.ast.declaraciones.DeclaracionArreglo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionEstructura;
import com.usac.contacto3d.ast.declaraciones.DeclaracionVariable;
import com.usac.contacto3d.ast.declaraciones.Funcion;
import com.usac.contacto3d.ast.declaraciones.Parametro;
import com.usac.contacto3d.ast.expresiones.Acceso;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.expresiones.IncrementoDecremento;
import com.usac.contacto3d.ast.expresiones.Leer;
import com.usac.contacto3d.ast.expresiones.Literal;
import com.usac.contacto3d.ast.expresiones.LiteralLista;
import com.usac.contacto3d.ast.expresiones.LlamadaFuncion;
import com.usac.contacto3d.ast.expresiones.OperacionBinaria;
import com.usac.contacto3d.ast.expresiones.OperacionUnaria;
import com.usac.contacto3d.ast.expresiones.Operador;
import com.usac.contacto3d.ast.expresiones.Sufijo;
import com.usac.contacto3d.ast.expresiones.SufijoAtributo;
import com.usac.contacto3d.ast.expresiones.SufijoIndice;
import com.usac.contacto3d.ast.instrucciones.Asignacion;
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
import com.usac.contacto3d.parser.LenguajeYBaseVisitor;
import com.usac.contacto3d.parser.LenguajeYParser.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.usac.contacto3d.constructores.Ayudante.columna;
import static com.usac.contacto3d.constructores.Ayudante.linea;

public class ConstructorAstY extends LenguajeYBaseVisitor<Nodo> {

    private final String archivo;

    public ConstructorAstY(String archivo) {
        this.archivo = archivo;
    }

    public Programa construir(ProgramaContext ctx) {
        List<DeclaracionEstructura> estructuras = ctx.estructura().stream().map(this::estructura).toList();
        List<Funcion> funciones = ctx.funcion().stream().map(this::funcion).toList();
        Programa programa = Programa.deY(estructuras, funciones, linea(ctx), columna(ctx));
        Ayudante.asignarArchivo(programa, archivo);
        return programa;
    }

    private DeclaracionEstructura estructura(EstructuraContext ctx) {
        List<CampoEstructura> campos = ctx.campo().stream().map(this::campo).toList();
        return new DeclaracionEstructura(ctx.ID().getText(), campos, linea(ctx.ID()), columna(ctx.ID()));
    }

    private CampoEstructura campo(CampoContext ctx) {
        Tipo tipo = tipo(ctx.tipo());
        if (!ctx.dimensionConstante().isEmpty()) {
            List<Integer> dimensiones = ctx.dimensionConstante().stream()
                    .map(d -> Integer.parseInt(d.LIT_ENTERO().getText()))
                    .toList();
            tipo = Tipo.arreglo(tipo, dimensiones);
        }
        return new CampoEstructura(ctx.ID().getText(), tipo, linea(ctx.ID()), columna(ctx.ID()));
    }

    private Funcion funcion(FuncionContext ctx) {
        List<Parametro> parametros = ctx.listaParametros() == null ? List.of()
                : ctx.listaParametros().parametro().stream().map(this::parametro).toList();
        Tipo retorno = ctx.tipo() == null ? Tipo.VACIO : tipo(ctx.tipo());
        return new Funcion(ctx.ID().getText(), parametros, retorno, bloque(ctx.bloque()),
                linea(ctx.ID()), columna(ctx.ID()));
    }

    private Parametro parametro(ParametroContext ctx) {
        return switch (ctx) {
            case ParamValorContext p ->
                    new Parametro(p.ID().getText(), tipo(p.tipo()), false, linea(p.ID()), columna(p.ID()));
            case ParamArregloContext p -> {
                List<Integer> dimensiones = new ArrayList<>();
                p.CORCH_A().forEach(c -> dimensiones.add(null));
                yield new Parametro(p.ID().getText(), Tipo.arreglo(tipo(p.tipo()), dimensiones), true,
                        linea(p.ID()), columna(p.ID()));
            }
            case ParamEstructuraContext p ->
                    new Parametro(p.ID(1).getText(), Tipo.estructura(p.ID(0).getText()), true,
                            linea(p.ID(1)), columna(p.ID(1)));
            default -> throw new IllegalStateException("Parametro no contemplado: " + ctx.getText());
        };
    }

    private Tipo tipo(TipoContext ctx) {
        return Tipo.desdePalabra(ctx.getText());
    }

    private List<Instruccion> bloque(BloqueContext ctx) {
        return instrucciones(ctx.instruccion());
    }

    private List<Instruccion> instrucciones(List<? extends ParserRuleContext> contextos) {
        return contextos.stream().map(this::visit).filter(Objects::nonNull)
                .map(Instruccion.class::cast).toList();
    }

    @Override
    public Nodo visitInstSimple(InstSimpleContext ctx) {
        return visit(ctx.instruccionSimple());
    }

    @Override
    public Nodo visitInstEstructura(InstEstructuraContext ctx) {
        return estructura(ctx.estructura());
    }

    @Override
    public Nodo visitInstSi(InstSiContext ctx) {
        SentenciaSiContext si = ctx.sentenciaSi();
        List<Rama> ramas = new ArrayList<>();
        ramas.add(new Rama(expr(si.expr()), bloque(si.bloque()), linea(si), columna(si)));
        for (RamaSinoContext sino : si.ramaSino()) {
            ramas.add(new Rama(expr(sino.expr()), bloque(sino.bloque()), linea(sino), columna(sino)));
        }
        if (si.ramaContrario() != null) {
            RamaContrarioContext contrario = si.ramaContrario();
            ramas.add(new Rama(null, bloque(contrario.bloque()), linea(contrario), columna(contrario)));
        }
        return new Si(ramas, linea(si), columna(si));
    }

    @Override
    public Nodo visitInstElegir(InstElegirContext ctx) {
        SentenciaElegirContext elegir = ctx.sentenciaElegir();
        List<Caso> casos = new ArrayList<>();
        for (CasoContext caso : elegir.caso()) {
            casos.add(new Caso(expr(caso.expr()), cuerpoCaso(caso.cuerpoCaso()), linea(caso), columna(caso)));
        }
        if (elegir.casoSiempre() != null) {
            CasoSiempreContext siempre = elegir.casoSiempre();
            casos.add(new Caso(null, cuerpoCaso(siempre.cuerpoCaso()), linea(siempre), columna(siempre)));
        }
        return new Elegir(expr(elegir.expr()), casos, linea(elegir), columna(elegir));
    }

    private List<Instruccion> cuerpoCaso(CuerpoCasoContext ctx) {
        return switch (ctx) {
            case CuerpoBloqueContext c -> bloque(c.bloque());
            case CuerpoEnLineaContext c -> instrucciones(List.of(c.instruccionSimple()));
            case CuerpoVacioContext c -> List.of();
            default -> throw new IllegalStateException("Cuerpo de caso no contemplado: " + ctx.getText());
        };
    }

    @Override
    public Nodo visitInstPara(InstParaContext ctx) {
        SentenciaParaContext para = ctx.sentenciaPara();
        Nodo inicio = switch (para.inicioPara()) {
            case InicioDeclaracionContext i -> declaracion(i.declaracion());
            case InicioAsignacionContext i -> asignacion(i.asignacion());
            default -> throw new IllegalStateException("Inicio de para no contemplado");
        };
        Nodo actualizacion = switch (para.actualizacionPara()) {
            case ActualizacionIncrementoContext a -> incremento(a.incremento());
            case ActualizacionAsignacionContext a -> asignacion(a.asignacion());
            default -> throw new IllegalStateException("Actualizacion de para no contemplada");
        };
        return new Para(List.of((Instruccion) inicio), expr(para.expr()), List.of((Instruccion) actualizacion),
                bloque(para.bloque()), linea(para), columna(para));
    }

    @Override
    public Nodo visitInstMientras(InstMientrasContext ctx) {
        SentenciaMientrasContext mientras = ctx.sentenciaMientras();
        return new Mientras(expr(mientras.expr()), bloque(mientras.bloque()), linea(mientras), columna(mientras));
    }

    @Override
    public Nodo visitInstHacer(InstHacerContext ctx) {
        SentenciaHacerContext hacer = ctx.sentenciaHacer();
        return new Hacer(bloque(hacer.bloque()), expr(hacer.expr()), linea(hacer), columna(hacer));
    }

    @Override
    public Nodo visitSimpleDeclaracion(SimpleDeclaracionContext ctx) {
        return declaracion(ctx.declaracion());
    }

    @Override
    public Nodo visitSimpleAsignacion(SimpleAsignacionContext ctx) {
        return asignacion(ctx.asignacion());
    }

    @Override
    public Nodo visitSimpleIncremento(SimpleIncrementoContext ctx) {
        return incremento(ctx.incremento());
    }

    @Override
    public Nodo visitSimpleLlamada(SimpleLlamadaContext ctx) {
        return llamada(ctx.llamada());
    }

    @Override
    public Nodo visitSimpleImprimir(SimpleImprimirContext ctx) {
        return new Imprimir(expresiones(ctx.listaExpresiones()), true, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitSimpleLeer(SimpleLeerContext ctx) {
        return new Leer(null, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitSimpleRetornar(SimpleRetornarContext ctx) {
        return new Retorno(expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitSimpleRomper(SimpleRomperContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.ROMPER, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitSimpleContinuar(SimpleContinuarContext ctx) {
        return new ControlCiclo(ControlCiclo.Accion.CONTINUAR, linea(ctx), columna(ctx));
    }

    private Nodo declaracion(DeclaracionContext ctx) {
        String nombre = ctx.ID().getText();
        Tipo tipo = tipo(ctx.tipo());
        Expresion valor = expr(ctx.expr());
        if (ctx.dimension().isEmpty()) {
            return new DeclaracionVariable(nombre, tipo, valor, linea(ctx.ID()), columna(ctx.ID()));
        }
        List<Expresion> dimensiones = ctx.dimension().stream().map(d -> expr(d.expr())).toList();
        return new DeclaracionArreglo(nombre, tipo, dimensiones, valor, linea(ctx.ID()), columna(ctx.ID()));
    }

    private Asignacion asignacion(AsignacionContext ctx) {
        return new Asignacion(acceso(ctx.acceso()), null, expr(ctx.expr()), linea(ctx), columna(ctx));
    }

    private IncrementoDecremento incremento(IncrementoContext ctx) {
        return new IncrementoDecremento(acceso(ctx.acceso()), ctx.INCREMENTO() != null, false,
                linea(ctx), columna(ctx));
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
    public Nodo visitExprLlamada(ExprLlamadaContext ctx) {
        return llamada(ctx.llamada());
    }

    @Override
    public Nodo visitExprAcceso(ExprAccesoContext ctx) {
        return acceso(ctx.acceso());
    }

    @Override
    public Nodo visitExprLeer(ExprLeerContext ctx) {
        return new Leer(null, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitExprLista(ExprListaContext ctx) {
        return new LiteralLista(expresiones(ctx.listaExpresiones()), linea(ctx), columna(ctx));
    }

    private LlamadaFuncion llamada(LlamadaContext ctx) {
        return new LlamadaFuncion(ctx.ID().getText(), expresiones(ctx.listaExpresiones()),
                linea(ctx), columna(ctx));
    }

    private Acceso acceso(AccesoContext ctx) {
        List<Sufijo> sufijos = ctx.sufijo().stream().map(this::sufijo).toList();
        return Acceso.deVariable(ctx.ID().getText(), sufijos, linea(ctx), columna(ctx));
    }

    private Sufijo sufijo(SufijoContext ctx) {
        return switch (ctx) {
            case SufijoCampoContext s -> new SufijoAtributo(s.ID().getText(), linea(s.ID()), columna(s.ID()));
            case SufijoIndiceContext s -> new SufijoIndice(expr(s.expr()), linea(s), columna(s));
            default -> throw new IllegalStateException("Sufijo no contemplado: " + ctx.getText());
        };
    }

    @Override
    public Nodo visitLitEntero(LitEnteroContext ctx) {
        return new Literal(Ayudante.entero(ctx.getText()), Tipo.ENTERO, linea(ctx), columna(ctx));
    }

    @Override
    public Nodo visitLitFlotante(LitFlotanteContext ctx) {
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
        return new Literal(ctx.VERDADERO() != null, Tipo.BOOLEANO, linea(ctx), columna(ctx));
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
