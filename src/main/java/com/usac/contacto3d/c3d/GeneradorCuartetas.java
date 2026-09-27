package com.usac.contacto3d.c3d;

import com.usac.contacto3d.ast.Lenguaje;
import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.declaraciones.CampoEstructura;
import com.usac.contacto3d.ast.declaraciones.DeclaracionArreglo;
import com.usac.contacto3d.ast.declaraciones.DeclaracionClase;
import com.usac.contacto3d.ast.declaraciones.DeclaracionConstructor;
import com.usac.contacto3d.ast.declaraciones.DeclaracionEstructura;
import com.usac.contacto3d.ast.declaraciones.DeclaracionMetodo;
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
import com.usac.contacto3d.semantico.Compatibilidad;
import com.usac.contacto3d.semantico.ResultadoSemantico;
import com.usac.contacto3d.simbolos.Simbolo;
import com.usac.contacto3d.simbolos.SimboloEstructura;
import com.usac.contacto3d.simbolos.SimboloFuncion;
import com.usac.contacto3d.simbolos.SimboloVariable;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.usac.contacto3d.c3d.Operacion.*;

public class GeneradorCuartetas implements Visitante<String> {

    private record Ubicacion(boolean enHeap, String direccion) { }

    private record Paso(Ubicacion ubicacion, String valor, Tipo tipo) { }

    private record Arreglo(String puntero, String total) { }

    private final ListaCuartetas codigo = new ListaCuartetas();
    private TablaSimbolos tabla;

    private SimboloFuncion funcionActual;
    private SimboloEstructura claseActual;
    private final Deque<String> destinosRomper = new ArrayDeque<>();
    private final Deque<String> destinosContinuar = new ArrayDeque<>();

    private final Map<String, Integer> literales = new LinkedHashMap<>();
    private int siguienteLiteral = 1;

    private final Map<String, SimboloFuncion> constructoresImplicitos = new HashMap<>();

    public ListaCuartetas generar(ResultadoSemantico resultado) {
        tabla = resultado.tabla();

        for (Programa modulo : resultado.modulos()) {
            for (DeclaracionClase clase : modulo.getClases()) {
                if (clase.getConstructores().isEmpty()) {
                    constructoresImplicitos.put(clase.getNombre(), constructorImplicito(clase));
                }
            }
        }
        for (Programa modulo : resultado.modulos()) {
            for (Funcion funcion : modulo.getFunciones()) {
                generarFuncion(funcion.getSimbolo(), funcion.getCuerpo(), List.of());
            }
            modulo.getClases().forEach(this::generarClase);
            if (modulo.getLenguaje() == Lenguaje.PIGLATIN) {
                generarPrincipal(modulo, resultado.principal());
            }
        }

        ListaCuartetas programa = new ListaCuartetas();
        arranque(programa, resultado);
        programa.agregarTodas(codigo);
        return programa;
    }

    private void arranque(ListaCuartetas programa, ResultadoSemantico resultado) {
        programa.agregar(FUNCION, "main", null, null);
        programa.agregar(ESCRIBIR_HEAP, "0", "-1", null);
        for (Map.Entry<String, Integer> literal : literales.entrySet()) {
            String texto = literal.getKey();
            int direccion = literal.getValue();
            for (int i = 0; i < texto.length(); i++) {
                programa.agregar(ESCRIBIR_HEAP, String.valueOf(direccion + i), String.valueOf((int) texto.charAt(i)), null);
            }
            programa.agregar(ESCRIBIR_HEAP, String.valueOf(direccion + texto.length()), "-1", null);
        }
        programa.agregar(ASIGNAR, String.valueOf(siguienteLiteral), null, "H");
        programa.agregar(ASIGNAR, String.valueOf(resultado.tamanioGlobales()), null, "P");
        if (resultado.principal() != null) {
            programa.agregar(CALL, resultado.principal().getEtiqueta(), null, null);
        }
        programa.agregar(HALT, null, null, null);
        programa.agregar(FIN_FUNCION, "main", null, null);
    }

    private void generarFuncion(SimboloFuncion funcion, List<Instruccion> cuerpo,
                                List<DeclaracionVariable> atributos) {
        funcionActual = funcion;
        emitir(FUNCION, funcion.getEtiqueta(), null, null);
        for (DeclaracionVariable atributo : atributos) {
            if (atributo.tieneValor()) {
                Ubicacion ubicacion = new Ubicacion(true,
                        sumar(cargarThis(), claseActual.getCampo(atributo.getNombre()).getOffset()));
                inicializarCon(ubicacion, atributo.getTipo(), atributo.getValor());
            }
        }
        instrucciones(cuerpo);
        emitir(FIN_FUNCION, funcion.getEtiqueta(), null, null);
    }

    private void generarClase(DeclaracionClase clase) {
        claseActual = clase.getSimbolo();
        for (DeclaracionConstructor constructor : clase.getConstructores()) {
            generarFuncion(constructor.getSimbolo(), constructor.getCuerpo(), clase.getAtributos());
        }
        SimboloFuncion implicito = constructoresImplicitos.get(clase.getNombre());
        if (implicito != null) {
            generarFuncion(implicito, List.of(), clase.getAtributos());
        }
        for (DeclaracionMetodo metodo : clase.getMetodos()) {
            generarFuncion(metodo.getSimbolo(), metodo.getCuerpo(), List.of());
        }
        claseActual = null;
    }

    private static SimboloFuncion constructorImplicito(DeclaracionClase clase) {
        SimboloFuncion implicito = new SimboloFuncion(clase.getNombre(), Tipo.VACIO, new ArrayList<>(), true,
                clase.getNombre(), clase.getArchivo(), clase.getLinea(), clase.getColumna());
        implicito.setEtiqueta(clase.getNombre() + "_constructor_implicito");
        implicito.setTamanioMarco(SimboloFuncion.CELDA_THIS + 1);
        return implicito;
    }

    private void generarPrincipal(Programa modulo, SimboloFuncion principal) {
        funcionActual = principal;
        emitir(FUNCION, principal.getEtiqueta(), null, null);
        instrucciones(modulo.getGlobales());
        instrucciones(modulo.getPrincipal());
        emitir(FIN_FUNCION, principal.getEtiqueta(), null, null);
    }

    private String llamar(SimboloFuncion funcion, List<Expresion> argumentos, String objeto) {
        List<SimboloVariable> parametros = funcion.getParametros();
        List<Object> valores = new ArrayList<>();
        for (int i = 0; i < parametros.size(); i++) {
            SimboloVariable parametro = parametros.get(i);
            Expresion argumento = argumentos.get(i);
            if (parametro.getTipo().esEstructura()) {
                Ubicacion ubicacion = recorrer((Acceso) argumento).ubicacion();
                valores.add(parametro.esPorReferencia() ? ubicacion.direccion() : ubicacion);
            } else {
                valores.add(valorPara(argumento, parametro.getTipo()));
            }
        }

        String marco = String.valueOf(funcionActual.getTamanioMarco());
        String base = operar(SUMA, "P", marco);
        if (objeto != null) {
            guardar(new Ubicacion(false, sumar(base, SimboloFuncion.CELDA_THIS)), objeto);
        }
        for (int i = 0; i < parametros.size(); i++) {
            Ubicacion destino = new Ubicacion(false, sumar(base, parametros.get(i).getPosicion()));
            if (valores.get(i) instanceof Ubicacion origen) {
                copiar(origen, destino, parametros.get(i).getTamanio());
            } else {
                guardar(destino, (String) valores.get(i));
            }
        }

        emitir(SUMA, "P", marco, "P");
        emitir(CALL, funcion.getEtiqueta(), null, null);
        String retorno = funcion.sinRetorno() ? null
                : cargar(new Ubicacion(false, sumar("P", SimboloFuncion.CELDA_RETORNO)));
        emitir(RESTA, "P", marco, "P");
        return retorno;
    }

    private void instrucciones(List<Instruccion> lista) {
        lista.forEach(instruccion -> instruccion.aceptar(this));
    }

    @Override
    public String visitarDeclaracionVariable(DeclaracionVariable nodo) {
        Ubicacion ubicacion = ubicacionDe(nodo.getSimbolo());
        if (nodo.tieneValor()) {
            inicializarCon(ubicacion, nodo.getTipo(), nodo.getValor());
        } else {
            porDefecto(ubicacion, nodo.getTipo());
        }
        return null;
    }

    @Override
    public String visitarDeclaracionArreglo(DeclaracionArreglo nodo) {
        SimboloVariable simbolo = nodo.getSimbolo();
        Tipo elemento = simbolo.getTipo().getTipoElemento();
        int rango = nodo.getDimensiones().size();
        String puntero;
        if (nodo.tieneValor() && !(nodo.getValor() instanceof LiteralLista)) {
            puntero = evaluar(nodo.getValor());
        } else {
            List<String> dimensiones = nodo.getDimensiones().stream().map(this::evaluar).toList();
            Arreglo arreglo = reservarArreglo(dimensiones, tamanio(elemento));
            if (nodo.tieneValor()) {
                llenarArreglo(arreglo.puntero(), rango, elemento, (LiteralLista) nodo.getValor());
            } else {
                inicializarElementos(arreglo, rango, elemento);
            }
            puntero = arreglo.puntero();
        }
        guardar(ubicacionDe(simbolo), puntero);
        return null;
    }

    @Override
    public String visitarDeclaracionEstructura(DeclaracionEstructura nodo) {
        return null;
    }

    @Override
    public String visitarBloque(Bloque nodo) {
        instrucciones(nodo.getInstrucciones());
        return null;
    }

    @Override
    public String visitarAsignacion(Asignacion nodo) {
        Acceso destino = nodo.getDestino();
        Tipo tipo = destino.getTipo();
        Ubicacion ubicacion = recorrer(destino).ubicacion();
        if (!nodo.esCompuesta()) {
            inicializarCon(ubicacion, tipo, nodo.getValor());
            return null;
        }
        String anterior = cargar(ubicacion);
        Expresion valor = nodo.getValor();
        String operando = evaluar(valor);
        Tipo resultado = Compatibilidad.binaria(nodo.getOperador(), tipo, valor.getTipo());
        guardar(ubicacion, aritmetica(nodo.getOperador(), anterior, tipo, operando, valor.getTipo(), resultado, nodo));
        return null;
    }

    @Override
    public String visitarSi(Si nodo) {
        String fin = etiqueta();
        for (Rama rama : nodo.getRamas()) {
            if (rama.esPorDefecto()) {
                instrucciones(rama.getCuerpo());
            } else {
                String siguiente = etiqueta();
                emitir(IF_FALSE, evaluar(rama.getCondicion()), null, siguiente);
                instrucciones(rama.getCuerpo());
                emitir(GOTO, null, null, fin);
                emitir(ETIQUETA, null, null, siguiente);
            }
        }
        emitir(ETIQUETA, null, null, fin);
        return null;
    }

    @Override
    public String visitarElegir(Elegir nodo) {
        String valor = evaluar(nodo.getValor());
        boolean cadenas = nodo.getValor().getTipo().getBase() == Tipo.Base.CADENA;
        String fin = etiqueta();
        String porDefecto = null;
        List<String> entradas = new ArrayList<>();
        for (Caso caso : nodo.getCasos()) {
            String entrada = etiqueta();
            entradas.add(entrada);
            if (caso.esPorDefecto()) {
                porDefecto = entrada;
            } else {
                String comparacion = operar(cadenas ? IGUAL_CADENAS : IGUAL, valor, evaluar(caso.getValor()));
                emitir(IF_TRUE, comparacion, null, entrada);
            }
        }
        emitir(GOTO, null, null, porDefecto != null ? porDefecto : fin);

        destinosRomper.push(fin);
        for (int i = 0; i < nodo.getCasos().size(); i++) {
            emitir(ETIQUETA, null, null, entradas.get(i));
            instrucciones(nodo.getCasos().get(i).getCuerpo());
        }
        destinosRomper.pop();
        emitir(ETIQUETA, null, null, fin);
        return null;
    }

    @Override
    public String visitarMientras(Mientras nodo) {
        String inicio = etiqueta();
        String fin = etiqueta();
        emitir(ETIQUETA, null, null, inicio);
        emitir(IF_FALSE, evaluar(nodo.getCondicion()), null, fin);
        cuerpoDeCiclo(nodo.getCuerpo(), fin, inicio);
        emitir(GOTO, null, null, inicio);
        emitir(ETIQUETA, null, null, fin);
        return null;
    }

    @Override
    public String visitarHacer(Hacer nodo) {
        String inicio = etiqueta();
        String condicion = etiqueta();
        String fin = etiqueta();
        emitir(ETIQUETA, null, null, inicio);
        cuerpoDeCiclo(nodo.getCuerpo(), fin, condicion);
        emitir(ETIQUETA, null, null, condicion);
        emitir(IF_TRUE, evaluar(nodo.getCondicion()), null, inicio);
        emitir(ETIQUETA, null, null, fin);
        return null;
    }

    @Override
    public String visitarPara(Para nodo) {
        instrucciones(nodo.getInicio());
        String inicio = etiqueta();
        String actualizacion = etiqueta();
        String fin = etiqueta();
        emitir(ETIQUETA, null, null, inicio);
        if (nodo.getCondicion() != null) {
            emitir(IF_FALSE, evaluar(nodo.getCondicion()), null, fin);
        }
        cuerpoDeCiclo(nodo.getCuerpo(), fin, actualizacion);
        emitir(ETIQUETA, null, null, actualizacion);
        instrucciones(nodo.getActualizacion());
        emitir(GOTO, null, null, inicio);
        emitir(ETIQUETA, null, null, fin);
        return null;
    }

    private void cuerpoDeCiclo(List<Instruccion> cuerpo, String romper, String continuar) {
        destinosRomper.push(romper);
        destinosContinuar.push(continuar);
        instrucciones(cuerpo);
        destinosRomper.pop();
        destinosContinuar.pop();
    }

    @Override
    public String visitarControlCiclo(ControlCiclo nodo) {
        emitir(GOTO, null, null, nodo.esRomper() ? destinosRomper.peek() : destinosContinuar.peek());
        return null;
    }

    @Override
    public String visitarRetorno(Retorno nodo) {
        if (nodo.getValor() != null) {
            String valor = valorPara(nodo.getValor(), funcionActual.getTipoRetorno());
            guardar(new Ubicacion(false, sumar("P", SimboloFuncion.CELDA_RETORNO)), valor);
        }
        emitir(RETURN, null, null, null);
        return null;
    }

    @Override
    public String visitarImprimir(Imprimir nodo) {
        for (Expresion valor : nodo.getValores()) {
            emitir(IMPRIMIR, evaluar(valor), etiquetaTipo(valor.getTipo(), valor), null);
        }
        if (nodo.conSaltoLinea()) {
            emitir(SALTO_LINEA, null, null, null);
        }
        return null;
    }

    @Override
    public String visitarLiteral(Literal nodo) {
        return switch (nodo.getTipo().getBase()) {
            case CADENA -> literal((String) nodo.getValor());
            case CARACTER -> String.valueOf((int) (Character) nodo.getValor());
            case BOOLEANO -> (Boolean) nodo.getValor() ? "1" : "0";
            case NULO -> "0";
            default -> nodo.getValor().toString();
        };
    }

    @Override
    public String visitarOperacionBinaria(OperacionBinaria nodo) {
        Operador operador = nodo.getOperador();
        if (operador == Operador.AND || operador == Operador.OR) {
            String resultado = temporal();
            String fin = etiqueta();
            emitir(ASIGNAR, evaluar(nodo.getIzquierda()), null, resultado);
            emitir(operador == Operador.AND ? IF_FALSE : IF_TRUE, resultado, null, fin);
            emitir(ASIGNAR, evaluar(nodo.getDerecha()), null, resultado);
            emitir(ETIQUETA, null, null, fin);
            return resultado;
        }
        String izquierda = evaluar(nodo.getIzquierda());
        String derecha = evaluar(nodo.getDerecha());
        return aritmetica(operador, izquierda, nodo.getIzquierda().getTipo(), derecha, nodo.getDerecha().getTipo(),
                nodo.getTipo(), nodo);
    }

    private String aritmetica(Operador operador, String a, Tipo tipoA, String b, Tipo tipoB,
                              Tipo resultado, Nodo nodo) {
        return switch (operador) {
            case SUMA -> resultado.getBase() == Tipo.Base.CADENA
                    ? operar(CONCATENAR, aCadena(a, tipoA, nodo), aCadena(b, tipoB, nodo))
                    : operar(SUMA, a, b);
            case RESTA -> operar(RESTA, a, b);
            case MULTIPLICACION -> operar(MULTIPLICACION, a, b);
            case DIVISION -> operar(esEntero(resultado) ? DIVISION_ENTERA : DIVISION, a, b);
            case MODULO -> operar(MODULO, a, b);
            case MENOR -> operar(MENOR, a, b);
            case MAYOR -> operar(MAYOR, a, b);
            case MENOR_IGUAL -> operar(MENOR_IGUAL, a, b);
            case MAYOR_IGUAL -> operar(MAYOR_IGUAL, a, b);
            case IGUAL, DIFERENTE -> {
                boolean cadenas = tipoA.getBase() == Tipo.Base.CADENA && tipoB.getBase() == Tipo.Base.CADENA;
                if (!cadenas) {
                    yield operar(operador == Operador.IGUAL ? IGUAL : DIFERENTE, a, b);
                }
                String iguales = operar(IGUAL_CADENAS, a, b);
                yield operador == Operador.IGUAL ? iguales : operar(NOT, iguales, null);
            }
            default -> throw new IllegalStateException("Operador sin traduccion: " + operador);
        };
    }

    private static boolean esEntero(Tipo tipo) {
        return tipo.getBase() == Tipo.Base.ENTERO || tipo.getBase() == Tipo.Base.CARACTER
                || tipo.getBase() == Tipo.Base.BOOLEANO;
    }

    private String aCadena(String valor, Tipo tipo, Nodo nodo) {
        return switch (tipo.getBase()) {
            case CADENA -> valor;
            case NULO -> literal("null");
            default -> operar(A_CADENA, valor, etiquetaTipo(tipo, nodo));
        };
    }

    @Override
    public String visitarOperacionUnaria(OperacionUnaria nodo) {
        String operando = evaluar(nodo.getOperando());
        return operar(nodo.getOperador() == Operador.NEGATIVO ? NEGATIVO : NOT, operando, null);
    }

    @Override
    public String visitarAcceso(Acceso nodo) {
        Paso paso = recorrer(nodo);
        return paso.ubicacion() != null ? cargar(paso.ubicacion()) : paso.valor();
    }

    private Paso recorrer(Acceso acceso) {
        Paso actual;
        if (acceso.esThis()) {
            actual = new Paso(null, cargarThis(), claseActual.getTipo());
        } else if (acceso.getLlamada() != null) {
            actual = new Paso(null, evaluar(acceso.getLlamada()), acceso.getLlamada().getTipo());
        } else if (acceso.getCampoImplicito() != null) {
            SimboloEstructura.Campo campo = acceso.getCampoImplicito();
            actual = new Paso(new Ubicacion(true, sumar(cargarThis(), campo.getOffset())), null, campo.getTipo());
        } else {
            SimboloVariable variable = (SimboloVariable) acceso.getSimbolo();
            actual = new Paso(ubicacionDe(variable), null, variable.getTipo());
        }

        List<Sufijo> sufijos = acceso.getSufijos();
        int i = 0;
        while (i < sufijos.size()) {
            switch (sufijos.get(i)) {
                case SufijoAtributo atributo -> {
                    SimboloEstructura.Campo campo = atributo.getCampo();
                    if (actual.tipo().esEstructura()) {
                        actual = new Paso(desplazar(actual.ubicacion(), campo.getOffset()), null, campo.getTipo());
                    } else {
                        String objeto = valorDe(actual);
                        verificarNulo(objeto, atributo);
                        actual = new Paso(new Ubicacion(true, sumar(objeto, campo.getOffset())), null, campo.getTipo());
                    }
                    i++;
                }
                case SufijoMetodo metodo -> {
                    String objeto = valorDe(actual);
                    verificarNulo(objeto, metodo);
                    String retorno = llamar(metodo.getMetodo(), metodo.getArgumentos(), objeto);
                    actual = new Paso(null, retorno, metodo.getTipo());
                    i++;
                }
                case SufijoIndice primero -> {
                    List<SufijoIndice> indices = new ArrayList<>();
                    while (i < sufijos.size() && sufijos.get(i) instanceof SufijoIndice indice) {
                        indices.add(indice);
                        i++;
                    }
                    Ubicacion elemento = indexar(valorDe(actual), indices, actual.tipo());
                    actual = new Paso(elemento, null, actual.tipo().getTipoElemento());
                }
                default -> throw new IllegalStateException("Sufijo sin traduccion: " + sufijos.get(i).getEtiqueta());
            }
        }
        return actual;
    }

    private String valorDe(Paso paso) {
        return paso.ubicacion() != null ? cargar(paso.ubicacion()) : paso.valor();
    }

    private Ubicacion indexar(String arreglo, List<SufijoIndice> indices, Tipo tipoArreglo) {
        verificarNulo(arreglo, indices.get(0));
        int rango = indices.size();
        String posicion = null;
        for (int d = 0; d < rango; d++) {
            SufijoIndice sufijo = indices.get(d);
            String indice = evaluar(sufijo.getIndice());
            String dimension = cargar(new Ubicacion(true, sumar(arreglo, d)));
            verificarIndice(indice, dimension, sufijo);
            posicion = d == 0 ? indice : operar(SUMA, operar(MULTIPLICACION, posicion, dimension), indice);
        }
        int tamanioElemento = tamanio(tipoArreglo.getTipoElemento());
        String desplazamiento = tamanioElemento == 1 ? posicion
                : operar(MULTIPLICACION, posicion, String.valueOf(tamanioElemento));
        return new Ubicacion(true, operar(SUMA, sumar(arreglo, rango), desplazamiento));
    }

    @Override
    public String visitarLlamadaFuncion(LlamadaFuncion nodo) {
        SimboloFuncion funcion = nodo.getFuncion();
        return llamar(funcion, nodo.getArgumentos(), funcion.esMetodo() ? cargarThis() : null);
    }

    @Override
    public String visitarNuevoObjeto(NuevoObjeto nodo) {
        SimboloEstructura clase = nodo.getSimboloClase();
        String objeto = temporal();
        emitir(ASIGNAR, "H", null, objeto);
        emitir(SUMA, "H", String.valueOf(Math.max(1, clase.getTamanio())), "H");
        SimboloFuncion constructor = nodo.getConstructor() != null ? nodo.getConstructor()
                : constructoresImplicitos.get(clase.getNombre());
        llamar(constructor, nodo.getArgumentos(), objeto);
        return objeto;
    }

    @Override
    public String visitarNuevoArreglo(NuevoArreglo nodo) {
        List<String> dimensiones = nodo.getDimensiones().stream().map(this::evaluar).toList();
        Arreglo arreglo = reservarArreglo(dimensiones, tamanio(nodo.getTipoElemento()));
        inicializarElementos(arreglo, dimensiones.size(), nodo.getTipoElemento());
        return arreglo.puntero();
    }

    @Override
    public String visitarLiteralLista(LiteralLista nodo) {
        return arregloDesdeLiteral(nodo);
    }

    @Override
    public String visitarIncrementoDecremento(IncrementoDecremento nodo) {
        Ubicacion ubicacion = recorrer(nodo.getDestino()).ubicacion();
        String anterior = cargar(ubicacion);
        String nuevo = operar(nodo.esIncremento() ? SUMA : RESTA, anterior, "1");
        guardar(ubicacion, nuevo);
        return nodo.esPrefijo() ? nuevo : anterior;
    }

    @Override
    public String visitarTernario(Ternario nodo) {
        String resultado = temporal();
        String siFalso = etiqueta();
        String fin = etiqueta();
        emitir(IF_FALSE, evaluar(nodo.getCondicion()), null, siFalso);
        emitir(ASIGNAR, evaluar(nodo.getSiVerdadero()), null, resultado);
        emitir(GOTO, null, null, fin);
        emitir(ETIQUETA, null, null, siFalso);
        emitir(ASIGNAR, evaluar(nodo.getSiFalso()), null, resultado);
        emitir(ETIQUETA, null, null, fin);
        return resultado;
    }

    @Override
    public String visitarLeer(Leer nodo) {
        String texto = temporal();
        emitir(LEER_CADENA, null, null, texto);
        if (nodo.tieneDestino()) {
            Acceso destino = nodo.getDestino();
            Ubicacion ubicacion = recorrer(destino).ubicacion();
            Tipo tipo = destino.getTipo();
            String valor = tipo.getBase() == Tipo.Base.CADENA ? texto
                    : operar(CONVERTIR_CADENA, texto, tipo.getBase() == Tipo.Base.BOOLEANO ? "bool"
                            : etiquetaTipo(tipo, destino));
            guardar(ubicacion, valor);
        }
        return texto;
    }

    private void inicializarCon(Ubicacion destino, Tipo tipo, Expresion valor) {
        if (tipo.esEstructura()) {
            if (valor instanceof LiteralLista lista) {
                List<SimboloEstructura.Campo> campos = campos(tipo);
                for (int i = 0; i < campos.size(); i++) {
                    SimboloEstructura.Campo campo = campos.get(i);
                    inicializarCon(desplazar(destino, campo.getOffset()), campo.getTipo(), lista.getElementos().get(i));
                }
            } else {
                copiar(recorrer((Acceso) valor).ubicacion(), destino, tamanio(tipo));
            }
            return;
        }
        guardar(destino, valorPara(valor, tipo));
    }

    private String valorPara(Expresion valor, Tipo esperado) {
        if (valor instanceof LiteralLista lista && esperado.esArreglo()) {
            return arregloDesdeLiteral(lista);
        }
        return evaluar(valor);
    }

    private String arregloDesdeLiteral(LiteralLista lista) {
        Tipo tipo = lista.getTipo();
        List<String> dimensiones = tipo.getDimensiones().stream().map(String::valueOf).toList();
        Arreglo arreglo = reservarArreglo(dimensiones, tamanio(tipo.getTipoElemento()));
        llenarArreglo(arreglo.puntero(), dimensiones.size(), tipo.getTipoElemento(), lista);
        return arreglo.puntero();
    }

    private Arreglo reservarArreglo(List<String> dimensiones, int tamanioElemento) {
        String puntero = temporal();
        emitir(ASIGNAR, "H", null, puntero);
        String total = dimensiones.get(0);
        for (int d = 1; d < dimensiones.size(); d++) {
            total = multiplicar(total, dimensiones.get(d));
        }
        for (int d = 0; d < dimensiones.size(); d++) {
            guardar(new Ubicacion(true, sumar(puntero, d)), dimensiones.get(d));
        }
        String celdas = sumar(multiplicar(total, String.valueOf(tamanioElemento)), dimensiones.size());
        emitir(SUMA, "H", celdas, "H");
        return new Arreglo(puntero, total);
    }

    private void llenarArreglo(String puntero, int rango, Tipo elemento, LiteralLista lista) {
        List<Expresion> valores = aplanar(lista, rango);
        int tamanio = tamanio(elemento);
        for (int k = 0; k < valores.size(); k++) {
            inicializarCon(new Ubicacion(true, sumar(puntero, rango + k * tamanio)), elemento, valores.get(k));
        }
    }

    private static List<Expresion> aplanar(LiteralLista lista, int niveles) {
        if (niveles == 1) {
            return lista.getElementos();
        }
        List<Expresion> planos = new ArrayList<>();
        for (Expresion fila : lista.getElementos()) {
            planos.addAll(aplanar((LiteralLista) fila, niveles - 1));
        }
        return planos;
    }

    private void porDefecto(Ubicacion ubicacion, Tipo tipo) {
        if (tipo.esEstructura()) {
            inicializarEstructura(ubicacion, tabla.buscarTipo(tipo.getNombre()));
        } else {
            guardar(ubicacion, "0");
        }
    }

    private void inicializarEstructura(Ubicacion ubicacion, SimboloEstructura estructura) {
        for (SimboloEstructura.Campo campo : estructura.getCampos().values()) {
            Ubicacion celda = desplazar(ubicacion, campo.getOffset());
            Tipo tipo = campo.getTipo();
            if (tipo.esEstructura()) {
                inicializarEstructura(celda, tabla.buscarTipo(tipo.getNombre()));
            } else if (tipo.esArreglo() && dimensionesConocidas(tipo)) {
                List<String> dimensiones = tipo.getDimensiones().stream().map(String::valueOf).toList();
                Arreglo arreglo = reservarArreglo(dimensiones, tamanio(tipo.getTipoElemento()));
                inicializarElementos(arreglo, dimensiones.size(), tipo.getTipoElemento());
                guardar(celda, arreglo.puntero());
            } else {
                guardar(celda, "0");
            }
        }
    }

    private void inicializarElementos(Arreglo arreglo, int rango, Tipo elemento) {
        if (!elemento.esEstructura()) {
            return;
        }
        SimboloEstructura estructura = tabla.buscarTipo(elemento.getNombre());
        if (!necesitaInicializacion(estructura)) {
            return;
        }
        String i = temporal();
        String inicio = etiqueta();
        String fin = etiqueta();
        emitir(ASIGNAR, "0", null, i);
        emitir(ETIQUETA, null, null, inicio);
        emitir(IF_FALSE, operar(MENOR, i, arreglo.total()), null, fin);
        String desplazamiento = operar(MULTIPLICACION, i, String.valueOf(estructura.getTamanio()));
        inicializarEstructura(new Ubicacion(true, operar(SUMA, sumar(arreglo.puntero(), rango), desplazamiento)),
                estructura);
        emitir(SUMA, i, "1", i);
        emitir(GOTO, null, null, inicio);
        emitir(ETIQUETA, null, null, fin);
    }

    private boolean necesitaInicializacion(SimboloEstructura estructura) {
        for (SimboloEstructura.Campo campo : estructura.getCampos().values()) {
            Tipo tipo = campo.getTipo();
            if ((tipo.esArreglo() && dimensionesConocidas(tipo))
                    || (tipo.esEstructura() && necesitaInicializacion(tabla.buscarTipo(tipo.getNombre())))) {
                return true;
            }
        }
        return false;
    }

    private static boolean dimensionesConocidas(Tipo arreglo) {
        return !arreglo.getDimensiones().isEmpty() && !arreglo.getDimensiones().contains(null);
    }

    private void copiar(Ubicacion origen, Ubicacion destino, int celdas) {
        for (int i = 0; i < celdas; i++) {
            guardar(desplazar(destino, i), cargar(desplazar(origen, i)));
        }
    }

    private void verificarNulo(String puntero, Nodo nodo) {
        String seguir = etiqueta();
        emitir(IF_TRUE, operar(DIFERENTE, puntero, "0"), null, seguir);
        errorEjecucion(nodo, "referencia nula");
        emitir(ETIQUETA, null, null, seguir);
    }

    private void verificarIndice(String indice, String dimension, Nodo nodo) {
        String fuera = etiqueta();
        String seguir = etiqueta();
        emitir(IF_TRUE, operar(MENOR, indice, "0"), null, fuera);
        emitir(IF_TRUE, operar(MENOR, indice, dimension), null, seguir);
        emitir(ETIQUETA, null, null, fuera);
        errorEjecucion(nodo, "indice fuera de rango");
        emitir(ETIQUETA, null, null, seguir);
    }

    private void errorEjecucion(Nodo nodo, String mensaje) {
        emitir(ERROR_EJECUCION, literal("Error de ejecucion en " + nodo.getArchivo() + " linea "
                + nodo.getLinea() + ": " + mensaje), null, null);
    }

    private Ubicacion ubicacionDe(SimboloVariable variable) {
        if (variable.getAlmacenamiento() == Simbolo.Almacenamiento.GLOBAL) {
            return new Ubicacion(false, String.valueOf(variable.getPosicion()));
        }
        String direccion = sumar("P", variable.getPosicion());
        if (variable.getCategoria() == Simbolo.Categoria.PARAMETRO && variable.esPorReferencia()
                && variable.getTipo().esEstructura()) {
            direccion = cargar(new Ubicacion(false, direccion));
        }
        return new Ubicacion(false, direccion);
    }

    private String cargarThis() {
        return cargar(new Ubicacion(false, sumar("P", SimboloFuncion.CELDA_THIS)));
    }

    private String cargar(Ubicacion ubicacion) {
        String temporal = temporal();
        emitir(ubicacion.enHeap() ? LEER_HEAP : LEER_STACK, ubicacion.direccion(), null, temporal);
        return temporal;
    }

    private void guardar(Ubicacion ubicacion, String valor) {
        emitir(ubicacion.enHeap() ? ESCRIBIR_HEAP : ESCRIBIR_STACK, ubicacion.direccion(), valor, null);
    }

    private Ubicacion desplazar(Ubicacion ubicacion, int celdas) {
        return new Ubicacion(ubicacion.enHeap(), sumar(ubicacion.direccion(), celdas));
    }

    private int tamanio(Tipo tipo) {
        return tipo.esEstructura() ? Math.max(1, tabla.buscarTipo(tipo.getNombre()).getTamanio()) : 1;
    }

    private List<SimboloEstructura.Campo> campos(Tipo estructura) {
        return new ArrayList<>(tabla.buscarTipo(estructura.getNombre()).getCampos().values());
    }

    private String evaluar(Expresion expresion) {
        return expresion.aceptar(this);
    }

    private String temporal() {
        return codigo.nuevoTemporal();
    }

    private String etiqueta() {
        return codigo.nuevaEtiqueta();
    }

    private void emitir(Operacion operacion, String arg1, String arg2, String resultado) {
        codigo.agregar(operacion, arg1, arg2, resultado);
    }

    private String operar(Operacion operacion, String a, String b) {
        String resultado = temporal();
        emitir(operacion, a, b, resultado);
        return resultado;
    }

    private String sumar(String base, int celdas) {
        if (celdas == 0) {
            return base;
        }
        if (esConstante(base)) {
            return String.valueOf(Integer.parseInt(base) + celdas);
        }
        return operar(SUMA, base, String.valueOf(celdas));
    }

    private String multiplicar(String a, String b) {
        if (esConstante(a) && esConstante(b)) {
            return String.valueOf(Integer.parseInt(a) * Integer.parseInt(b));
        }
        if (b.equals("1")) {
            return a;
        }
        return operar(MULTIPLICACION, a, b);
    }

    private static boolean esConstante(String operando) {
        return operando.matches("-?\\d+");
    }

    private String literal(String texto) {
        Integer direccion = literales.get(texto);
        if (direccion == null) {
            direccion = siguienteLiteral;
            literales.put(texto, direccion);
            siguienteLiteral += texto.length() + 1;
        }
        return String.valueOf(direccion);
    }

    private static String etiquetaTipo(Tipo tipo, Nodo nodo) {
        return switch (tipo.getBase()) {
            case FLOTANTE -> "flotante";
            case CARACTER -> "caracter";
            case CADENA -> "cadena";
            case BOOLEANO -> {
                Lenguaje lenguaje = Lenguaje.desdeArchivo(nodo.getArchivo());
                yield lenguaje == Lenguaje.ZETARIANO ? "bool_z" : lenguaje == Lenguaje.PIGLATIN ? "bool_pig" : "bool_y";
            }
            default -> "entero";
        };
    }

    @Override
    public String visitarPrograma(Programa nodo) {
        throw new IllegalStateException("El programa se genera con generar(resultado)");
    }

    @Override
    public String visitarImportacion(Importacion nodo) {
        throw new IllegalStateException("Los imports no generan codigo");
    }

    @Override
    public String visitarCampoEstructura(CampoEstructura nodo) {
        throw new IllegalStateException("Los campos no generan codigo");
    }

    @Override
    public String visitarDeclaracionClase(DeclaracionClase nodo) {
        throw new IllegalStateException("Las clases se generan desde generar()");
    }

    @Override
    public String visitarFuncion(Funcion nodo) {
        throw new IllegalStateException("Las funciones se generan desde generar()");
    }

    @Override
    public String visitarDeclaracionMetodo(DeclaracionMetodo nodo) {
        throw new IllegalStateException("Los metodos se generan desde su clase");
    }

    @Override
    public String visitarDeclaracionConstructor(DeclaracionConstructor nodo) {
        throw new IllegalStateException("Los constructores se generan desde su clase");
    }

    @Override
    public String visitarParametro(Parametro nodo) {
        throw new IllegalStateException("Los parametros los escribe quien llama");
    }

    @Override
    public String visitarRama(Rama nodo) {
        throw new IllegalStateException("Las ramas se generan desde su Si");
    }

    @Override
    public String visitarCaso(Caso nodo) {
        throw new IllegalStateException("Los casos se generan desde su Elegir");
    }

    @Override
    public String visitarSufijoAtributo(SufijoAtributo nodo) {
        throw new IllegalStateException("Los sufijos se generan desde su Acceso");
    }

    @Override
    public String visitarSufijoIndice(SufijoIndice nodo) {
        throw new IllegalStateException("Los sufijos se generan desde su Acceso");
    }

    @Override
    public String visitarSufijoMetodo(SufijoMetodo nodo) {
        throw new IllegalStateException("Los sufijos se generan desde su Acceso");
    }
}
