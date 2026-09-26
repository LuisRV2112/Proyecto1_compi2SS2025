package com.usac.contacto3d.semantico;

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
import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;
import com.usac.contacto3d.simbolos.Simbolo;
import com.usac.contacto3d.simbolos.SimboloEstructura;
import com.usac.contacto3d.simbolos.SimboloFuncion;
import com.usac.contacto3d.simbolos.SimboloVariable;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida el programa completo (el .pig y todo lo que importa) y asigna memoria.
 *
 * Trabaja en pasadas, y el orden importa: asi una funcion puede llamar a otra
 * declarada mas abajo o en otro archivo, y una estructura puede usar otra
 * declarada despues.
 *
 *   1. registrar todas las estructuras y clases (solo el nombre)
 *   2. completar sus campos, con offsets, en orden de dependencia
 *   3. registrar las firmas de funciones, metodos y constructores
 *   4. analizar los cuerpos de Y? y Zetariano
 *   5. analizar PigLatin: globales, funciones de MUNERA> y MAIOR>
 *
 * Como visitante devuelve el tipo de cada expresion (las instrucciones
 * devuelven null). Tras un error la expresion vale Tipo.ERROR, que se acepta
 * en silencio mas arriba: un error se reporta una sola vez, donde nace.
 *
 * Ademas anota el AST (simbolo de cada variable, sobrecarga elegida en cada
 * llamada, offset de cada campo) para que el generador no resuelva nada de nuevo.
 */
public class AnalizadorSemantico implements Visitante<Tipo> {

    private final ListaErrores errores;
    private final TablaSimbolos tabla = new TablaSimbolos();

    /* Estructuras por nombre, para completar sus campos en orden de dependencia. */
    private final Map<String, DeclaracionEstructura> declaracionesEstructura = new HashMap<>();
    private final Set<String> estructurasEnProceso = new HashSet<>();
    private final Set<String> estructurasCompletas = new HashSet<>();

    /* Contexto de lo que se esta analizando. */
    private SimboloEstructura claseActual;
    private SimboloFuncion funcionActual;
    private int ciclosAbiertos;
    private int elegirAbiertos;
    /** Las declaraciones de VARIABILES> de PigLatin van a la zona global, no al stack. */
    private boolean declarandoGlobales;
    private int siguienteGlobal;

    private final Map<String, Integer> usosEtiqueta = new HashMap<>();

    public AnalizadorSemantico(ListaErrores errores) {
        this.errores = errores;
    }

    public ResultadoSemantico analizar(List<Programa> modulos) {
        for (Programa modulo : modulos) {
            validarNombreDeArchivo(modulo);
            modulo.getEstructuras().forEach(this::registrarEstructura);
            modulo.getClases().forEach(this::registrarClase);
        }
        for (Programa modulo : modulos) {
            modulo.getEstructuras().forEach(e -> completarEstructura(e.getNombre()));
        }
        for (Programa modulo : modulos) {
            modulo.getClases().forEach(this::completarClase);
        }
        for (Programa modulo : modulos) {
            modulo.getFunciones().forEach(this::registrarFuncion);
            modulo.getClases().forEach(this::registrarMiembros);
        }
        for (Programa modulo : modulos) {
            if (modulo.getLenguaje() != Lenguaje.PIGLATIN) {
                for (Funcion funcion : modulo.getFunciones()) {
                    analizarFuncion(funcion.getSimbolo(), funcion.getParametros(), funcion.getCuerpo(),
                            false, funcion);
                }
                modulo.getClases().forEach(this::analizarClase);
            }
        }
        SimboloFuncion principal = null;
        for (Programa modulo : modulos) {
            if (modulo.getLenguaje() == Lenguaje.PIGLATIN) {
                principal = analizarPigLatin(modulo);
            }
        }
        return new ResultadoSemantico(modulos, tabla, principal, siguienteGlobal);
    }

    /* =================== 1-2. Estructuras y clases =================== */

    private void validarNombreDeArchivo(Programa modulo) {
        for (DeclaracionClase clase : modulo.getClases()) {
            String esperado = clase.getNombre() + Lenguaje.ZETARIANO.getExtension();
            if (!esperado.equals(modulo.getArchivo())) {
                error(clase, clase.getNombre(), "La clase '" + clase.getNombre() + "' esta en '"
                        + modulo.getArchivo() + "'. Se esperaba que el archivo se llamara " + esperado);
            }
        }
    }

    private void registrarEstructura(DeclaracionEstructura declaracion) {
        SimboloEstructura simbolo = new SimboloEstructura(declaracion.getNombre(), false,
                declaracion.getArchivo(), declaracion.getLinea(), declaracion.getColumna());
        if (declararTipo(simbolo, declaracion)) {
            declaracion.setSimbolo(simbolo);
            declaracionesEstructura.put(declaracion.getNombre(), declaracion);
        }
    }

    private void registrarClase(DeclaracionClase declaracion) {
        SimboloEstructura simbolo = new SimboloEstructura(declaracion.getNombre(), true,
                declaracion.getArchivo(), declaracion.getLinea(), declaracion.getColumna());
        if (declararTipo(simbolo, declaracion)) {
            declaracion.setSimbolo(simbolo);
        }
    }

    private boolean declararTipo(SimboloEstructura simbolo, Nodo declaracion) {
        SimboloEstructura previo = tabla.buscarTipo(simbolo.getNombre());
        if (previo != null) {
            error(declaracion, simbolo.getNombre(), "El tipo '" + simbolo.getNombre() + "' ya fue declarado en "
                    + previo.getArchivo() + " linea " + previo.getLinea() + ". Se esperaba un nombre distinto");
            return false;
        }
        tabla.declararTipo(simbolo);
        return true;
    }

    /**
     * Los campos se aplanan: una estructura anidada ocupa sus celdas dentro de
     * la que la contiene, asi que antes hay que conocer su tamanio. Por eso se
     * completa primero la anidada, y una estructura que se contiene a si misma
     * (directa o indirectamente) no tendria tamanio finito.
     */
    private void completarEstructura(String nombre) {
        DeclaracionEstructura declaracion = declaracionesEstructura.get(nombre);
        if (declaracion == null || estructurasCompletas.contains(nombre)) {
            return;
        }
        estructurasEnProceso.add(nombre);
        SimboloEstructura estructura = declaracion.getSimbolo();

        tabla.abrirAmbito("estructura " + nombre);
        for (CampoEstructura campo : declaracion.getCampos()) {
            Tipo tipo = resolverTipo(campo.getTipo(), campo);
            if (tipo.esEstructura()) {
                if (estructurasEnProceso.contains(tipo.getNombre())) {
                    error(campo, campo.getNombre(), "La estructura '" + nombre + "' se contiene a si misma a traves de '"
                            + campo.getNombre() + "'. Se esperaba que las estructuras anidadas no formaran un ciclo");
                    tipo = Tipo.ERROR;
                } else {
                    completarEstructura(tipo.getNombre());
                }
            }
            campo.setTipo(tipo);
            agregarCampo(estructura, campo.getNombre(), tipo, tamanio(tipo), campo, Simbolo.Almacenamiento.STACK);
        }
        tabla.cerrarAmbito();

        estructurasEnProceso.remove(nombre);
        estructurasCompletas.add(nombre);
    }

    /** Los atributos se ubican por offset dentro del objeto, que vive en el heap. */
    private void completarClase(DeclaracionClase declaracion) {
        SimboloEstructura clase = declaracion.getSimbolo();
        if (clase == null) {
            return;
        }
        tabla.abrirAmbito("clase " + clase.getNombre());
        for (DeclaracionVariable atributo : declaracion.getAtributos()) {
            Tipo tipo = resolverTipo(atributo.getTipo(), atributo);
            atributo.setTipo(tipo);
            atributo.setSimbolo(agregarCampo(clase, atributo.getNombre(), tipo, tamanio(tipo), atributo,
                    Simbolo.Almacenamiento.HEAP));
        }
        tabla.cerrarAmbito();
    }

    /**
     * El campo tambien se declara como simbolo ATRIBUTO en el ambito de su tipo,
     * para que salga en la tabla. Su posicion es el offset dentro del tipo.
     */
    private SimboloVariable agregarCampo(SimboloEstructura tipo, String nombre, Tipo tipoCampo, int celdas,
                                         Nodo nodo, Simbolo.Almacenamiento zona) {
        if (!tipo.agregarCampo(nombre, tipoCampo, celdas)) {
            error(nodo, nombre, "El campo '" + nombre + "' esta repetido en '" + tipo.getNombre()
                    + "'. Se esperaba un nombre distinto para cada campo");
            return null;
        }
        SimboloVariable simbolo = new SimboloVariable(nombre, tipoCampo, Simbolo.Categoria.ATRIBUTO, true,
                nodo.getArchivo(), nodo.getLinea(), nodo.getColumna());
        simbolo.setTamanio(celdas);
        simbolo.setPorReferencia(tipoCampo.esArreglo() || tipoCampo.esObjeto());
        simbolo.ubicarEnMemoria(zona, tipo.getCampo(nombre).getOffset());
        tabla.declarar(simbolo);
        return simbolo;
    }

    /* ======================== 3. Firmas ======================== */

    private void registrarFuncion(Funcion funcion) {
        Tipo retorno = retornoValido(resolverTipo(funcion.getTipoRetorno(), funcion), funcion);
        funcion.setTipoRetorno(retorno);
        SimboloFuncion simbolo = new SimboloFuncion(funcion.getNombre(), retorno, parametros(funcion.getParametros()),
                false, null, funcion.getArchivo(), funcion.getLinea(), funcion.getColumna());
        simbolo.setEtiqueta(etiqueta(funcion.getNombre()));
        funcion.setSimbolo(simbolo);
        if (!tabla.declararFuncion(simbolo)) {
            error(funcion, funcion.getNombre(), "La funcion '" + simbolo.getFirma()
                    + "' ya fue declarada. Se esperaba otro nombre o parametros de otros tipos");
        }
    }

    private void registrarMiembros(DeclaracionClase declaracion) {
        SimboloEstructura clase = declaracion.getSimbolo();
        if (clase == null) {
            return;
        }
        for (DeclaracionConstructor constructor : declaracion.getConstructores()) {
            SimboloFuncion simbolo = new SimboloFuncion(clase.getNombre(), Tipo.VACIO,
                    parametros(constructor.getParametros()), true, clase.getNombre(),
                    constructor.getArchivo(), constructor.getLinea(), constructor.getColumna());
            simbolo.setEtiqueta(etiqueta(clase.getNombre() + "_constructor"));
            constructor.setSimbolo(simbolo);
            if (constructor.getNombre().equals(clase.getNombre())) {
                agregarMiembro(clase.getConstructores(), simbolo, constructor, "El constructor");
            } else {
                error(constructor, constructor.getNombre(), "'" + constructor.getNombre()
                        + "' no tiene tipo de retorno y no se llama como la clase. Se esperaba un constructor '"
                        + clase.getNombre() + "' o un metodo con tipo de retorno");
            }
        }
        for (DeclaracionMetodo metodo : declaracion.getMetodos()) {
            Tipo retorno = retornoValido(resolverTipo(metodo.getTipoRetorno(), metodo), metodo);
            metodo.setTipoRetorno(retorno);
            SimboloFuncion simbolo = new SimboloFuncion(metodo.getNombre(), retorno,
                    parametros(metodo.getParametros()), true, clase.getNombre(),
                    metodo.getArchivo(), metodo.getLinea(), metodo.getColumna());
            simbolo.setEtiqueta(etiqueta(clase.getNombre() + "_" + metodo.getNombre()));
            metodo.setSimbolo(simbolo);
            agregarMiembro(clase.getMetodos(), simbolo, metodo, "El metodo");
        }
    }

    /**
     * El valor de retorno ocupa una sola celda del marco; una estructura
     * aplanada no cabe. Los objetos y arreglos si, porque son un puntero.
     */
    private Tipo retornoValido(Tipo retorno, Funcion funcion) {
        if (retorno.esEstructura()) {
            error(funcion, funcion.getNombre(), "'" + funcion.getNombre() + "' no puede retornar la estructura "
                    + retorno + ". Se esperaba un tipo primitivo, un arreglo o un objeto; una estructura "
                    + "se devuelve recibiendola por referencia ({} " + retorno + ")");
            return Tipo.ERROR;
        }
        return retorno;
    }

    private void agregarMiembro(List<SimboloFuncion> existentes, SimboloFuncion nuevo, Nodo nodo, String que) {
        boolean repetido = existentes.stream().anyMatch(f -> f.getFirma().equals(nuevo.getFirma()));
        if (repetido) {
            error(nodo, nuevo.getNombre(), que + " '" + nuevo.getFirma() + "' ya fue declarado en '"
                    + nuevo.getClaseDuenia() + "'. Se esperaba una sobrecarga con parametros de otros tipos");
            return;
        }
        existentes.add(nuevo);
        tabla.registrarEnHistorial(nuevo);
    }

    private List<SimboloVariable> parametros(List<Parametro> parametros) {
        List<SimboloVariable> simbolos = new ArrayList<>();
        for (Parametro parametro : parametros) {
            Tipo tipo = resolverTipo(parametro.getTipo(), parametro);
            parametro.setTipo(tipo);
            SimboloVariable simbolo = SimboloVariable.parametro(parametro.getNombre(), tipo,
                    parametro.getArchivo(), parametro.getLinea(), parametro.getColumna());
            // Por referencia la celda guarda la direccion, no el valor: ocupa una sola
            boolean referencia = parametro.esPorReferencia() || tipo.esArreglo() || tipo.esObjeto();
            simbolo.setPorReferencia(referencia);
            simbolo.setTamanio(referencia ? 1 : tamanio(tipo));
            parametro.setSimbolo(simbolo);
            simbolos.add(simbolo);
        }
        return simbolos;
    }

    /** Nombre unico para el C3D: las sobrecargas reciben un sufijo numerico. */
    private String etiqueta(String base) {
        int uso = usosEtiqueta.merge(base, 1, Integer::sum);
        return uso == 1 ? base : base + "_" + uso;
    }

    /* ======================== 4-5. Cuerpos ======================== */

    private void analizarClase(DeclaracionClase declaracion) {
        SimboloEstructura clase = declaracion.getSimbolo();
        if (clase == null) {
            return;
        }
        claseActual = clase;

        // Los valores iniciales de los atributos se ejecutan dentro de cada
        // constructor; aqui solo se validan, con this disponible
        tabla.abrirMarcoFuncion("atributos de " + clase.getNombre());
        for (DeclaracionVariable atributo : declaracion.getAtributos()) {
            if (atributo.tieneValor()) {
                verificarValor(atributo.getValor(), atributo.getTipo(), atributo.getNombre());
            }
        }
        tabla.cerrarAmbito();

        for (DeclaracionConstructor constructor : declaracion.getConstructores()) {
            analizarFuncion(constructor.getSimbolo(), constructor.getParametros(), constructor.getCuerpo(),
                    true, constructor);
        }
        for (DeclaracionMetodo metodo : declaracion.getMetodos()) {
            analizarFuncion(metodo.getSimbolo(), metodo.getParametros(), metodo.getCuerpo(), true, metodo);
        }
        claseActual = null;
    }

    /**
     * Las globales de VARIABILES> las ven las funciones de MUNERA> y MAIOR>,
     * pero no las de Y? ni Zetariano (Y? prohibe variables globales). Por eso
     * viven en un ambito que se abre solo mientras se analiza el .pig.
     */
    private SimboloFuncion analizarPigLatin(Programa modulo) {
        tabla.abrirAmbito("VARIABILES");
        declarandoGlobales = true;
        instrucciones(modulo.getGlobales());
        declarandoGlobales = false;

        for (Funcion funcion : modulo.getFunciones()) {
            analizarFuncion(funcion.getSimbolo(), funcion.getParametros(), funcion.getCuerpo(), false, funcion);
        }

        // MAIOR> se trata como una funcion sin parametros ni retorno: necesita su propio marco
        SimboloFuncion principal = new SimboloFuncion("principal", Tipo.VACIO, new ArrayList<>(), false, null,
                modulo.getArchivo(), modulo.getLinea(), modulo.getColumna());
        principal.setEtiqueta(etiqueta("principal"));
        tabla.registrarEnHistorial(principal);
        analizarFuncion(principal, List.of(), modulo.getPrincipal(), false, modulo);

        tabla.cerrarAmbito();
        return principal;
    }

    private void analizarFuncion(SimboloFuncion funcion, List<Parametro> nodosParametros,
                                 List<Instruccion> cuerpo, boolean conThis, Nodo nodo) {
        SimboloFuncion funcionAnterior = funcionActual;
        int ciclosAnteriores = ciclosAbiertos;
        int elegirAnteriores = elegirAbiertos;
        funcionActual = funcion;
        ciclosAbiertos = 0;
        elegirAbiertos = 0;

        String duenio = funcion.getClaseDuenia() == null ? "" : funcion.getClaseDuenia() + ".";
        tabla.abrirMarcoFuncion(duenio + funcion.getNombre());
        tabla.reservarStack(1);                 // SimboloFuncion.CELDA_RETORNO
        if (conThis) {
            tabla.reservarStack(1);             // SimboloFuncion.CELDA_THIS
        }
        List<SimboloVariable> parametros = funcion.getParametros();
        for (int i = 0; i < parametros.size(); i++) {
            if (!tabla.declararConMemoria(parametros.get(i))) {
                error(nodosParametros.get(i), parametros.get(i).getNombre(), "El parametro '"
                        + parametros.get(i).getNombre() + "' esta repetido. Se esperaba un nombre distinto");
            }
        }

        instrucciones(cuerpo);

        if (!funcion.sinRetorno() && !siempreRetorna(cuerpo)) {
            error(nodo, funcion.getNombre(), "'" + funcion.getNombre() + "' debe retornar un valor de tipo "
                    + funcion.getTipoRetorno() + " en todos los caminos. Falta un retorno al final o en alguna rama");
        }
        tabla.cerrarAmbito();
        funcion.setTamanioMarco(tabla.getMaximoMarco());

        funcionActual = funcionAnterior;
        ciclosAbiertos = ciclosAnteriores;
        elegirAbiertos = elegirAnteriores;
    }

    /** Analiza un cuerpo y reporta lo que queda despues de un retorno, romper o continuar. */
    private void instrucciones(List<Instruccion> lista) {
        Instruccion corte = null;
        boolean inalcanzableReportado = false;
        for (Instruccion instruccion : lista) {
            if (corte != null && !inalcanzableReportado) {
                error((Nodo) instruccion, "Codigo inalcanzable: la instruccion anterior ("
                        + ((Nodo) corte).getEtiqueta().toLowerCase() + ") termina el bloque. Se esperaba que fuera la ultima");
                inalcanzableReportado = true;
            }
            instruccion.aceptar(this);
            if (instruccion instanceof Acceso acceso && !terminaEnLlamada(acceso)) {
                error(acceso, "Esto no es una instruccion: un acceso suelto no hace nada. Se esperaba una llamada a un metodo");
            }
            if (cortaElFlujo(instruccion)) {
                corte = instruccion;
            }
        }
    }

    /**
     * Un romper/continuar fuera de lugar ya se reporto como tal; tratarlo
     * ademas como corte agregaria un "codigo inalcanzable" por la misma causa.
     */
    private boolean cortaElFlujo(Instruccion instruccion) {
        return switch (instruccion) {
            case Retorno r -> true;
            case ControlCiclo c -> c.esRomper() ? ciclosAbiertos > 0 || elegirAbiertos > 0 : ciclosAbiertos > 0;
            default -> false;
        };
    }

    /**
     * Los ciclos no cuentan: su condicion puede ser falsa desde el inicio y el
     * cuerpo no ejecutarse nunca. Un si cuenta solo si tiene rama por defecto y
     * todas sus ramas retornan.
     */
    private boolean siempreRetorna(List<Instruccion> cuerpo) {
        for (Instruccion instruccion : cuerpo) {
            boolean retorna = switch (instruccion) {
                case Retorno r -> true;
                case Bloque b -> siempreRetorna(b.getInstrucciones());
                case Si si -> si.tieneRamaPorDefecto()
                        && si.getRamas().stream().allMatch(rama -> siempreRetorna(rama.getCuerpo()));
                default -> false;
            };
            if (retorna) {
                return true;
            }
        }
        return false;
    }

    /* ======================== Declaraciones ======================== */

    @Override
    public Tipo visitarDeclaracionVariable(DeclaracionVariable nodo) {
        Tipo tipo = resolverTipo(nodo.getTipo(), nodo);
        nodo.setTipo(tipo);
        // El valor se analiza antes de declarar: "entero x = x" debe fallar
        if (nodo.tieneValor()) {
            verificarValor(nodo.getValor(), tipo, nodo.getNombre());
        }
        SimboloVariable simbolo = SimboloVariable.variable(nodo.getNombre(), tipo, nodo.tieneValor(),
                nodo.getArchivo(), nodo.getLinea(), nodo.getColumna());
        simbolo.setTamanio(tamanio(tipo));
        simbolo.setPorReferencia(tipo.esArreglo() || tipo.esObjeto());
        declararVariable(simbolo, nodo);
        nodo.setSimbolo(simbolo);
        return null;
    }

    @Override
    public Tipo visitarDeclaracionArreglo(DeclaracionArreglo nodo) {
        Tipo elemento = resolverTipo(nodo.getTipoElemento(), nodo);
        nodo.setTipoElemento(elemento);
        List<Integer> dimensiones = dimensiones(nodo.getDimensiones());
        Tipo tipo = elemento.esError() ? Tipo.ERROR : Tipo.arreglo(elemento, dimensiones);

        if (nodo.tieneValor()) {
            verificarValor(nodo.getValor(), tipo, nodo.getNombre());
        }
        // En el stack solo va el puntero; el contenido aplanado va en el heap
        SimboloVariable simbolo = SimboloVariable.arreglo(nodo.getNombre(), tipo, nodo.tieneValor(),
                nodo.getArchivo(), nodo.getLinea(), nodo.getColumna());
        simbolo.setPorReferencia(true);
        declararVariable(simbolo, nodo);
        nodo.setSimbolo(simbolo);
        return null;
    }

    /** Evalua las dimensiones que se puedan: null donde no es constante. */
    private List<Integer> dimensiones(List<Expresion> expresiones) {
        List<Integer> dimensiones = new ArrayList<>();
        for (Expresion expresion : expresiones) {
            Tipo tipo = expresion.aceptar(this);
            if (!tipo.esError() && !Compatibilidad.esEntero(tipo)) {
                error(expresion, "La dimension de un arreglo debe ser entera; se obtuvo " + tipo);
            }
            Integer valor = EvaluadorConstantes.entero(expresion);
            if (valor != null && valor <= 0) {
                error(expresion, "La dimension de un arreglo debe ser mayor que 0; se obtuvo " + valor);
                valor = null;
            }
            dimensiones.add(valor);
        }
        return dimensiones;
    }

    /** Estructura declarada dentro de una funcion de Y?. */
    @Override
    public Tipo visitarDeclaracionEstructura(DeclaracionEstructura nodo) {
        registrarEstructura(nodo);
        if (nodo.getSimbolo() != null) {
            completarEstructura(nodo.getNombre());
        }
        return null;
    }

    private void declararVariable(SimboloVariable simbolo, Nodo nodo) {
        Simbolo previo = tabla.buscarLocal(simbolo.getNombre());
        if (previo != null) {
            error(nodo, simbolo.getNombre(), "'" + simbolo.getNombre() + "' ya fue declarada en este ambito (linea "
                    + previo.getLinea() + "). Se esperaba un nombre distinto");
            return;
        }
        if (declarandoGlobales) {
            tabla.declarar(simbolo);
            simbolo.ubicarEnMemoria(Simbolo.Almacenamiento.GLOBAL, siguienteGlobal);
            siguienteGlobal += simbolo.getTamanio();
        } else {
            tabla.declararConMemoria(simbolo);
        }
    }

    /**
     * Valida que un valor pueda guardarse en algo del tipo esperado. Las
     * listas {..} no tienen tipo propio: se validan contra el esperado.
     *
     * @return el tipo del valor (para una lista, con las dimensiones que trae)
     */
    private Tipo verificarValor(Expresion valor, Tipo esperado, String destino) {
        if (valor instanceof LiteralLista lista) {
            return validarLista(lista, esperado, destino);
        }
        Tipo tipo = valor.aceptar(this);
        if (!Compatibilidad.asignable(esperado, tipo)) {
            if (Compatibilidad.pierdeInformacion(esperado, tipo)) {
                error(valor, "No se puede guardar un valor " + tipo + " en '" + destino + "', de tipo " + esperado
                        + ": se perderia informacion. Se esperaba un valor " + esperado + " o de menor jerarquia");
            } else {
                error(valor, "No se puede guardar un valor de tipo " + tipo + " en '" + destino
                        + "'. Se esperaba un valor de tipo " + esperado);
            }
        }
        return tipo;
    }

    private Tipo validarLista(LiteralLista lista, Tipo esperado, String destino) {
        Tipo resultado = switch (esperado.getBase()) {
            case ERROR -> Tipo.ERROR;
            case ARREGLO -> validarListaArreglo(lista, esperado, destino);
            case ESTRUCTURA -> validarListaEstructura(lista, esperado, destino);
            case OBJETO -> {
                error(lista, "Un objeto de " + esperado + " no se crea con una lista {...}. Se esperaba new/novus "
                        + esperado + "(...)");
                yield Tipo.ERROR;
            }
            default -> {
                error(lista, "'" + destino + "' es de tipo " + esperado
                        + " y no se puede inicializar con una lista {...}. Se esperaba un solo valor");
                yield Tipo.ERROR;
            }
        };
        lista.setTipo(resultado);
        return resultado;
    }

    /**
     * Cada nivel de llaves es una dimension. Donde la dimension no se declaro
     * (Zetariano: int[][] m = {...}) se toma del literal, y todas las filas
     * deben medir lo mismo, porque el arreglo se aplana.
     */
    private Tipo validarListaArreglo(LiteralLista lista, Tipo esperado, String destino) {
        List<Integer> dimensiones = esperado.getDimensiones();
        int cantidad = lista.getElementos().size();
        Integer declarada = dimensiones.isEmpty() ? null : dimensiones.get(0);
        if (declarada != null && declarada != cantidad) {
            error(lista, "'" + destino + "' tiene " + declarada + " posicion(es) en esa dimension y la lista trae "
                    + cantidad + " valor(es). Se esperaba la misma cantidad");
        }

        boolean esMatriz = dimensiones.size() > 1;
        Tipo subTipo = esMatriz
                ? Tipo.arreglo(esperado.getTipoElemento(), dimensiones.subList(1, dimensiones.size()))
                : esperado.getTipoElemento();

        List<Integer> dimensionesFila = null;
        boolean irregularReportado = false;
        for (Expresion elemento : lista.getElementos()) {
            Tipo tipo = verificarValor(elemento, subTipo, destino);
            if (esMatriz && elemento instanceof LiteralLista && tipo.esArreglo()) {
                if (dimensionesFila == null) {
                    dimensionesFila = tipo.getDimensiones();
                } else if (!dimensionesFila.equals(tipo.getDimensiones()) && !irregularReportado) {
                    error(elemento, "Matriz irregular en '" + destino
                            + "': las filas tienen tamanios distintos. Se esperaba que todas midieran lo mismo");
                    irregularReportado = true;
                }
            }
        }

        List<Integer> inferidas = new ArrayList<>();
        inferidas.add(cantidad);
        if (esMatriz) {
            inferidas.addAll(dimensionesFila != null ? dimensionesFila : dimensiones.subList(1, dimensiones.size()));
        }
        return Tipo.arreglo(esperado.getTipoElemento(), inferidas);
    }

    /** Los literales de estructura son posicionales: un valor por campo, en orden. */
    private Tipo validarListaEstructura(LiteralLista lista, Tipo esperado, String destino) {
        SimboloEstructura estructura = tabla.buscarTipo(esperado.getNombre());
        List<SimboloEstructura.Campo> campos = new ArrayList<>(estructura.getCampos().values());
        List<Expresion> valores = lista.getElementos();
        if (campos.size() != valores.size()) {
            // Con la cantidad equivocada, comparar campo por campo solo daria errores en cascada
            String nombres = campos.stream().map(SimboloEstructura.Campo::getNombre).collect(Collectors.joining(", "));
            error(lista, "La estructura " + esperado + " tiene " + campos.size() + " campo(s) y la lista trae "
                    + valores.size() + " valor(es). Se esperaba un valor por campo, en orden: " + nombres);
            valores.forEach(valor -> {
                if (!(valor instanceof LiteralLista)) {
                    valor.aceptar(this);   // igual se analizan, por si tienen errores propios
                }
            });
            return esperado;
        }
        for (int i = 0; i < campos.size(); i++) {
            SimboloEstructura.Campo campo = campos.get(i);
            verificarValor(valores.get(i), campo.getTipo(), destino + "." + campo.getNombre());
        }
        return esperado;
    }

    /* ======================== Instrucciones ======================== */

    @Override
    public Tipo visitarBloque(Bloque nodo) {
        tabla.abrirAmbito("bloque");
        instrucciones(nodo.getInstrucciones());
        tabla.cerrarAmbito();
        return null;
    }

    @Override
    public Tipo visitarAsignacion(Asignacion nodo) {
        Acceso destino = nodo.getDestino();
        verificarAsignable(destino);
        Tipo tipoDestino = destino.aceptar(this);
        String nombre = descripcion(destino);

        if (nodo.esCompuesta()) {
            Tipo tipoValor = nodo.getValor().aceptar(this);
            if (!tipoDestino.esError() && !tipoValor.esError()) {
                Tipo resultado = Compatibilidad.binaria(nodo.getOperador(), tipoDestino, tipoValor);
                if (resultado == null) {
                    error(nodo, nombre, "No se puede aplicar '" + nodo.getEtiqueta() + "' entre " + tipoDestino
                            + " y " + tipoValor);
                } else if (!Compatibilidad.asignable(tipoDestino, resultado)) {
                    error(nodo, nombre, "El resultado de '" + nodo.getEtiqueta() + "' es " + resultado
                            + " y no cabe en '" + nombre + "', de tipo " + tipoDestino);
                }
            }
        } else {
            verificarValor(nodo.getValor(), tipoDestino, nombre);
        }
        if (destino.getSimbolo() instanceof SimboloVariable variable && destino.getSufijos().isEmpty()) {
            variable.marcarInicializada();
        }
        return null;
    }

    /** Lo que va a la izquierda de =, ++ o << debe ser un lugar donde guardar, no una llamada. */
    private void verificarAsignable(Acceso destino) {
        List<Sufijo> sufijos = destino.getSufijos();
        boolean esLlamada = sufijos.isEmpty() ? destino.getLlamada() != null
                : sufijos.get(sufijos.size() - 1) instanceof SufijoMetodo;
        if (esLlamada) {
            error(destino, "No se puede guardar un valor en el resultado de una llamada. "
                    + "Se esperaba una variable, un campo o una posicion de arreglo");
        } else if (destino.esThis() && sufijos.isEmpty()) {
            error(destino, "this", "No se puede asignar a this. Se esperaba un atributo: this.nombre");
        }
    }

    @Override
    public Tipo visitarSi(Si nodo) {
        for (Rama rama : nodo.getRamas()) {
            if (rama.getCondicion() != null) {
                condicion(rama.getCondicion());
            }
            tabla.abrirAmbito("si");
            instrucciones(rama.getCuerpo());
            tabla.cerrarAmbito();
        }
        return null;
    }

    @Override
    public Tipo visitarElegir(Elegir nodo) {
        Tipo tipo = nodo.getValor().aceptar(this);
        tabla.abrirAmbito("elegir");
        elegirAbiertos++;

        int porDefecto = 0;
        Set<Object> valoresVistos = new HashSet<>();
        for (Caso caso : nodo.getCasos()) {
            if (caso.esPorDefecto()) {
                if (++porDefecto == 2) {
                    error(caso, palabra(caso, "siempre", "default", "default"),
                            "Solo puede haber un caso por defecto. Se esperaba quitar este");
                }
            } else {
                Expresion valor = caso.getValor();
                Tipo tipoCaso = valor.aceptar(this);
                if (!tipo.esError() && !tipoCaso.esError() && !Compatibilidad.comparables(tipo, tipoCaso)) {
                    error(valor, "El caso es de tipo " + tipoCaso + " y no se puede comparar con el valor evaluado, de tipo "
                            + tipo + ". Se esperaba un caso de tipo " + tipo);
                }
                Object constante = valor instanceof Literal l && l.getValor() instanceof String s
                        ? s : EvaluadorConstantes.entero(valor);
                if (constante != null && !valoresVistos.add(constante)) {
                    error(valor, "El caso " + constante + " esta repetido. Se esperaba un valor distinto en cada caso");
                }
            }
            instrucciones(caso.getCuerpo());
        }

        elegirAbiertos--;
        tabla.cerrarAmbito();
        return null;
    }

    @Override
    public Tipo visitarMientras(Mientras nodo) {
        condicion(nodo.getCondicion());
        cuerpoDeCiclo(nodo.getCuerpo());
        return null;
    }

    @Override
    public Tipo visitarHacer(Hacer nodo) {
        cuerpoDeCiclo(nodo.getCuerpo());
        // Despues de cerrar el cuerpo: lo declarado adentro no se ve en la condicion
        condicion(nodo.getCondicion());
        return null;
    }

    @Override
    public Tipo visitarPara(Para nodo) {
        tabla.abrirAmbito("para");
        nodo.getInicio().forEach(instruccion -> instruccion.aceptar(this));
        if (nodo.getCondicion() != null) {
            condicion(nodo.getCondicion());
        }
        cuerpoDeCiclo(nodo.getCuerpo());
        ciclosAbiertos++;
        nodo.getActualizacion().forEach(instruccion -> instruccion.aceptar(this));
        ciclosAbiertos--;
        tabla.cerrarAmbito();
        return null;
    }

    private void cuerpoDeCiclo(List<Instruccion> cuerpo) {
        tabla.abrirAmbito("ciclo");
        ciclosAbiertos++;
        instrucciones(cuerpo);
        ciclosAbiertos--;
        tabla.cerrarAmbito();
    }

    private void condicion(Expresion condicion) {
        Tipo tipo = condicion.aceptar(this);
        if (!tipo.esError() && !Compatibilidad.esBool(tipo)) {
            error(condicion, "La condicion debe ser de tipo bool; se obtuvo " + tipo);
        }
    }

    @Override
    public Tipo visitarControlCiclo(ControlCiclo nodo) {
        if (nodo.esRomper()) {
            if (ciclosAbiertos == 0 && elegirAbiertos == 0) {
                String palabra = palabra(nodo, "romper", "break", "interrumpe");
                error(nodo, palabra, "'" + palabra + "' esta fuera de un ciclo. Se esperaba dentro de un ciclo o de un "
                        + palabra(nodo, "elegir", "switch", "ciclo"));
            }
        } else if (ciclosAbiertos == 0) {
            String palabra = palabra(nodo, "continuar", "continue", "perge");
            error(nodo, palabra, "'" + palabra + "' esta fuera de un ciclo. Se esperaba dentro de un ciclo");
        }
        return null;
    }

    @Override
    public Tipo visitarRetorno(Retorno nodo) {
        String palabra = palabra(nodo, "retornar", "return", "reddere");
        if (funcionActual == null) {
            error(nodo, palabra, "'" + palabra + "' esta fuera de una funcion");
            return null;
        }
        String funcion = funcionActual.getNombre();
        if (nodo.getValor() == null) {
            if (!funcionActual.sinRetorno()) {
                error(nodo, palabra, "'" + funcion + "' debe retornar un valor de tipo " + funcionActual.getTipoRetorno());
            }
        } else if (funcionActual.sinRetorno()) {
            nodo.getValor().aceptar(this);
            error(nodo, palabra, "'" + funcion + "' no retorna ningun valor. Se esperaba '" + palabra + "' sin expresion");
        } else if (nodo.getValor() instanceof LiteralLista lista) {
            validarLista(lista, funcionActual.getTipoRetorno(), "el retorno de " + funcion);
        } else {
            Tipo esperado = funcionActual.getTipoRetorno();
            Tipo tipo = nodo.getValor().aceptar(this);
            if (!Compatibilidad.asignable(esperado, tipo)) {
                String perdida = Compatibilidad.pierdeInformacion(esperado, tipo) ? " (se perderia informacion)" : "";
                error(nodo.getValor(), "'" + funcion + "' retorna " + esperado + " y se intento retornar un valor "
                        + tipo + perdida + ". Se esperaba un valor de tipo " + esperado);
            }
        }
        return null;
    }

    @Override
    public Tipo visitarImprimir(Imprimir nodo) {
        for (Expresion valor : nodo.getValores()) {
            Tipo tipo = valor.aceptar(this);
            if (!tipo.esError() && !tipo.esPrimitivo()) {
                error(valor, "No se puede imprimir un valor de tipo " + tipo + ". Se esperaba un valor primitivo "
                        + "(entero, flotante, cadena, caracter o bool)");
            }
        }
        return null;
    }

    /* ======================== Expresiones ======================== */

    @Override
    public Tipo visitarLiteral(Literal nodo) {
        if (nodo.getValor() instanceof Long valor) {
            error(nodo, "El entero " + valor + " excede el rango de un entero (hasta " + Integer.MAX_VALUE + ")");
            nodo.setTipo(Tipo.ERROR);
        }
        return nodo.getTipo();
    }

    @Override
    public Tipo visitarOperacionBinaria(OperacionBinaria nodo) {
        Tipo a = nodo.getIzquierda().aceptar(this);
        Tipo b = nodo.getDerecha().aceptar(this);
        Tipo resultado = Tipo.ERROR;
        if (!a.esError() && !b.esError()) {
            resultado = Compatibilidad.binaria(nodo.getOperador(), a, b);
            if (resultado == null) {
                error(nodo, nodo.getOperador().getSimbolo(), mensajeOperacion(nodo.getOperador(), a, b));
                resultado = Tipo.ERROR;
            }
        }
        nodo.setTipo(resultado);
        return resultado;
    }

    private String mensajeOperacion(Operador operador, Tipo a, Tipo b) {
        String simbolo = "'" + operador.getSimbolo() + "'";
        boolean hayCadena = a.getBase() == Tipo.Base.CADENA || b.getBase() == Tipo.Base.CADENA;
        if (hayCadena && operador.esAritmetico()) {
            return "No se puede aplicar " + simbolo + " a una cadena: la cadena solo admite concatenacion con '+'";
        }
        return switch (operador) {
            case MODULO -> simbolo + " exige operandos enteros; se obtuvo " + a + " y " + b;
            case AND, OR -> simbolo + " exige operandos bool; se obtuvo " + a + " y " + b;
            case IGUAL, DIFERENTE -> "No se puede comparar " + a + " con " + b;
            default -> "No se puede aplicar " + simbolo + " entre " + a + " y " + b;
        };
    }

    @Override
    public Tipo visitarOperacionUnaria(OperacionUnaria nodo) {
        Tipo operando = nodo.getOperando().aceptar(this);
        Tipo resultado = Tipo.ERROR;
        if (!operando.esError()) {
            resultado = Compatibilidad.unaria(nodo.getOperador(), operando);
            if (resultado == null) {
                String esperado = nodo.getOperador() == Operador.NOT ? "bool" : "numerico";
                error(nodo, "El operador '" + nodo.getEtiqueta() + "' exige un valor " + esperado + "; se obtuvo " + operando);
                resultado = Tipo.ERROR;
            }
        }
        nodo.setTipo(resultado);
        return resultado;
    }

    @Override
    public Tipo visitarAcceso(Acceso nodo) {
        Tipo tipo;
        if (nodo.esThis()) {
            if (claseActual == null) {
                error(nodo, "this", "'this' solo se puede usar dentro de una clase");
                tipo = Tipo.ERROR;
            } else {
                tipo = claseActual.getTipo();
            }
        } else if (nodo.getLlamada() != null) {
            tipo = nodo.getLlamada().aceptar(this);
        } else {
            tipo = raiz(nodo);
        }
        tipo = aplicarSufijos(nodo.getSufijos(), tipo);
        nodo.setTipo(tipo);
        return tipo;
    }

    /** Un nombre suelto: variable visible, o dentro de una clase, un atributo del propio objeto. */
    private Tipo raiz(Acceso nodo) {
        String nombre = nodo.getNombre();
        if (tabla.buscar(nombre) instanceof SimboloVariable variable) {
            nodo.setSimbolo(variable);
            return variable.getTipo();
        }
        if (claseActual != null && claseActual.tieneCampo(nombre)) {
            SimboloEstructura.Campo campo = claseActual.getCampo(nombre);
            nodo.setCampoImplicito(campo);
            return campo.getTipo();
        }
        error(nodo, nombre, "'" + nombre + "' no esta declarada. Se esperaba una variable declarada antes de usarla");
        return Tipo.ERROR;
    }

    private Tipo aplicarSufijos(List<Sufijo> sufijos, Tipo tipo) {
        int i = 0;
        while (i < sufijos.size()) {
            Sufijo sufijo = sufijos.get(i);
            if (tipo.esError()) {
                sufijo.setTipo(Tipo.ERROR);
                i++;
                continue;
            }
            switch (sufijo) {
                case SufijoAtributo atributo -> {
                    tipo = campo(atributo, tipo);
                    atributo.setTipo(tipo);
                    i++;
                }
                case SufijoMetodo metodo -> {
                    tipo = metodo(metodo, tipo);
                    metodo.setTipo(tipo);
                    i++;
                }
                case SufijoIndice indice -> {
                    // Los indices seguidos se consumen juntos: m[i][j] es un solo acceso aplanado
                    int consumidos = 0;
                    while (i + consumidos < sufijos.size() && sufijos.get(i + consumidos) instanceof SufijoIndice) {
                        consumidos++;
                    }
                    tipo = indices(sufijos.subList(i, i + consumidos), tipo);
                    i += consumidos;
                }
                default -> throw new IllegalStateException("Sufijo no contemplado: " + sufijo.getEtiqueta());
            }
        }
        return tipo;
    }

    private Tipo campo(SufijoAtributo atributo, Tipo tipo) {
        String nombre = atributo.getNombre();
        if (!tipo.esEstructura() && !tipo.esObjeto()) {
            error(atributo, nombre, "No se puede acceder a '." + nombre + "': el valor es de tipo " + tipo
                    + ". Se esperaba una estructura o un objeto");
            return Tipo.ERROR;
        }
        SimboloEstructura estructura = tabla.buscarTipo(tipo.getNombre());
        SimboloEstructura.Campo campo = estructura.getCampo(nombre);
        if (campo == null) {
            error(atributo, nombre, tipo + " no tiene un campo '" + nombre + "'. Se esperaba uno de: "
                    + String.join(", ", estructura.getCampos().keySet()));
            return Tipo.ERROR;
        }
        atributo.setCampo(campo);
        return campo.getTipo();
    }

    private Tipo metodo(SufijoMetodo sufijo, Tipo tipo) {
        List<Tipo> argumentos = tipos(sufijo.getArgumentos());
        if (!tipo.esObjeto()) {
            String razon = tipo.esEstructura() ? "las estructuras no tienen metodos" : "el valor es de tipo " + tipo;
            error(sufijo, sufijo.getNombre(), "No se puede llamar a '." + sufijo.getNombre() + "()': " + razon
                    + ". Se esperaba un objeto");
            return Tipo.ERROR;
        }
        SimboloEstructura clase = tabla.buscarTipo(tipo.getNombre());
        SimboloFuncion metodo = resolver(conNombre(clase.getMetodos(), sufijo.getNombre()), argumentos, sufijo,
                "el metodo '" + sufijo.getNombre() + "' de " + clase.getNombre());
        sufijo.setMetodo(metodo);
        return metodo == null ? Tipo.ERROR : metodo.getTipoRetorno();
    }

    /**
     * Un arreglo de N dimensiones se indexa con N indices; con menos quedaria
     * una fila suelta del arreglo aplanado, que no se puede representar. Cada
     * indice constante se valida contra su dimension, si esta se conoce.
     */
    private Tipo indices(List<Sufijo> indices, Tipo tipo) {
        for (Sufijo sufijo : indices) {
            Tipo tipoIndice = ((SufijoIndice) sufijo).getIndice().aceptar(this);
            if (!tipoIndice.esError() && !Compatibilidad.esEntero(tipoIndice)) {
                error(sufijo, "El indice debe ser entero; se obtuvo " + tipoIndice);
            }
        }
        Tipo resultado;
        if (!tipo.esArreglo()) {
            error(indices.get(0), "No se puede indexar un valor de tipo " + tipo + ". Se esperaba un arreglo");
            resultado = Tipo.ERROR;
        } else {
            List<Integer> dimensiones = tipo.getDimensiones();
            if (indices.size() != dimensiones.size()) {
                error(indices.get(0), "El arreglo tiene " + dimensiones.size() + " dimension(es) y se usaron "
                        + indices.size() + " indice(s). Se esperaban " + dimensiones.size());
                resultado = Tipo.ERROR;
            } else {
                for (int d = 0; d < indices.size(); d++) {
                    Integer valor = EvaluadorConstantes.entero(((SufijoIndice) indices.get(d)).getIndice());
                    Integer limite = dimensiones.get(d);
                    if (valor != null && (valor < 0 || (limite != null && valor >= limite))) {
                        String rango = limite == null ? "mayor o igual que 0" : "entre 0 y " + (limite - 1);
                        error(indices.get(d), "Indice fuera de rango: " + valor + ". Se esperaba un indice " + rango);
                    }
                }
                resultado = tipo.getTipoElemento();
            }
        }
        for (Sufijo sufijo : indices) {
            sufijo.setTipo(resultado);
        }
        return resultado;
    }

    /**
     * Dentro de una clase, f(x) es un metodo del propio objeto; fuera, una
     * funcion libre de un .y o de MUNERA>.
     */
    @Override
    public Tipo visitarLlamadaFuncion(LlamadaFuncion nodo) {
        List<Tipo> argumentos = tipos(nodo.getArgumentos());
        SimboloFuncion funcion;
        if (claseActual != null) {
            funcion = resolver(conNombre(claseActual.getMetodos(), nodo.getNombre()), argumentos, nodo,
                    "el metodo '" + nodo.getNombre() + "' de " + claseActual.getNombre());
        } else {
            funcion = resolver(conNombre(tabla.getFunciones().values(), nodo.getNombre()), argumentos, nodo,
                    "la funcion '" + nodo.getNombre() + "'");
        }
        if (funcion != null) {
            verificarReferencias(funcion, nodo.getArgumentos());
        }
        nodo.setFuncion(funcion);
        Tipo tipo = funcion == null ? Tipo.ERROR : funcion.getTipoRetorno();
        nodo.setTipo(tipo);
        return tipo;
    }

    @Override
    public Tipo visitarNuevoObjeto(NuevoObjeto nodo) {
        List<Tipo> argumentos = tipos(nodo.getArgumentos());
        SimboloEstructura clase = tabla.buscarTipo(nodo.getClase());
        Tipo tipo = Tipo.ERROR;
        if (clase == null) {
            error(nodo, nodo.getClase(), "La clase '" + nodo.getClase() + "' no existe. Se esperaba una clase de un .z importado");
        } else if (!clase.esClase()) {
            error(nodo, nodo.getClase(), "'" + nodo.getClase() + "' es una estructura, no una clase. "
                    + "Se esperaba inicializarla con una lista {...}");
        } else {
            nodo.setSimboloClase(clase);
            tipo = clase.getTipo();
            if (clase.getConstructores().isEmpty()) {
                // Sin constructores declarados solo existe el implicito, sin parametros
                if (!argumentos.isEmpty()) {
                    error(nodo, nodo.getClase(), "La clase '" + clase.getNombre() + "' no declara constructores. "
                            + "Se esperaba crearla sin argumentos: " + clase.getNombre() + "()");
                }
            } else {
                SimboloFuncion constructor = resolver(clase.getConstructores(), argumentos, nodo,
                        "el constructor de " + clase.getNombre());
                nodo.setConstructor(constructor);
            }
        }
        nodo.setTipo(tipo);
        return tipo;
    }

    @Override
    public Tipo visitarNuevoArreglo(NuevoArreglo nodo) {
        Tipo elemento = resolverTipo(nodo.getTipoElemento(), nodo);
        nodo.setTipoElemento(elemento);
        List<Integer> dimensiones = dimensiones(nodo.getDimensiones());
        Tipo tipo = elemento.esError() ? Tipo.ERROR : Tipo.arreglo(elemento, dimensiones);
        nodo.setTipo(tipo);
        return tipo;
    }

    @Override
    public Tipo visitarLiteralLista(LiteralLista nodo) {
        error(nodo, "Una lista {...} solo puede inicializar o asignar un arreglo o una estructura. "
                + "Se esperaba un valor");
        nodo.setTipo(Tipo.ERROR);
        return Tipo.ERROR;
    }

    @Override
    public Tipo visitarIncrementoDecremento(IncrementoDecremento nodo) {
        verificarAsignable(nodo.getDestino());
        Tipo tipo = nodo.getDestino().aceptar(this);
        boolean numerico = tipo.getBase() == Tipo.Base.ENTERO || tipo.getBase() == Tipo.Base.FLOTANTE
                || tipo.getBase() == Tipo.Base.CARACTER;
        if (!tipo.esError() && !numerico) {
            error(nodo, descripcion(nodo.getDestino()), "'" + (nodo.esIncremento() ? "++" : "--")
                    + "' exige una variable numerica; '" + descripcion(nodo.getDestino()) + "' es de tipo " + tipo);
            tipo = Tipo.ERROR;
        }
        nodo.setTipo(tipo);
        return tipo;
    }

    @Override
    public Tipo visitarTernario(Ternario nodo) {
        condicion(nodo.getCondicion());
        Tipo a = nodo.getSiVerdadero().aceptar(this);
        Tipo b = nodo.getSiFalso().aceptar(this);
        Tipo tipo;
        if (a.esError() || b.esError()) {
            tipo = Tipo.ERROR;
        } else if (a.esNumerico() && b.esNumerico()) {
            tipo = Tipo.dominante(a, b);
        } else if (Compatibilidad.asignable(a, b)) {
            tipo = a;
        } else if (Compatibilidad.asignable(b, a)) {
            tipo = b;
        } else {
            error(nodo, "Las dos opciones del ternario deben tener tipos compatibles; se obtuvo " + a + " y " + b);
            tipo = Tipo.ERROR;
        }
        nodo.setTipo(tipo);
        return tipo;
    }

    /** El texto leido es una cadena; "x <<" de PigLatin lo convierte al tipo de x al ejecutarse. */
    @Override
    public Tipo visitarLeer(Leer nodo) {
        if (nodo.tieneDestino()) {
            verificarAsignable(nodo.getDestino());
            Tipo tipo = nodo.getDestino().aceptar(this);
            if (!tipo.esError() && !tipo.esPrimitivo()) {
                error(nodo.getDestino(), "Solo se puede leer en una variable de tipo primitivo; '"
                        + descripcion(nodo.getDestino()) + "' es de tipo " + tipo);
            }
        }
        nodo.setTipo(Tipo.CADENA);
        return Tipo.CADENA;
    }

    /* ==================== Resolucion de llamadas ==================== */

    private List<Tipo> tipos(List<Expresion> expresiones) {
        return expresiones.stream().map(e -> e.aceptar(this)).toList();
    }

    private static List<SimboloFuncion> conNombre(Iterable<SimboloFuncion> funciones, String nombre) {
        List<SimboloFuncion> lista = new ArrayList<>();
        funciones.forEach(f -> {
            if (f.getNombre().equals(nombre)) {
                lista.add(f);
            }
        });
        return lista;
    }

    /**
     * Elige la sobrecarga: primero por cantidad de argumentos, despues la que
     * calce exacto en tipos, y si ninguna calza exacto, la primera compatible.
     */
    private SimboloFuncion resolver(List<SimboloFuncion> candidatas, List<Tipo> argumentos, Nodo nodo, String que) {
        String lexema = nodo instanceof LlamadaFuncion l ? l.getNombre()
                : nodo instanceof SufijoMetodo m ? m.getNombre() : lexema(nodo);
        if (candidatas.isEmpty()) {
            error(nodo, lexema, "No existe " + que + ". Se esperaba una funcion o metodo declarado");
            return null;
        }
        if (argumentos.stream().anyMatch(Tipo::esError)) {
            return null;   // un argumento ya fallo: no se sabe que sobrecarga quiso
        }
        List<SimboloFuncion> porCantidad = candidatas.stream()
                .filter(f -> f.getCantidadParametros() == argumentos.size()).toList();
        if (porCantidad.isEmpty()) {
            List<String> cantidades = candidatas.stream().map(SimboloFuncion::getCantidadParametros)
                    .distinct().sorted().map(String::valueOf).toList();
            String esperadas = cantidades.size() == 1 ? cantidades.get(0)
                    : String.join(", ", cantidades.subList(0, cantidades.size() - 1)) + " o " + cantidades.getLast();
            error(nodo, lexema, mayuscula(que) + " recibe " + esperadas + " argumento(s) y se pasaron "
                    + argumentos.size());
            return null;
        }
        for (SimboloFuncion candidata : porCantidad) {
            if (calza(candidata, argumentos, true)) {
                return candidata;
            }
        }
        for (SimboloFuncion candidata : porCantidad) {
            if (calza(candidata, argumentos, false)) {
                return candidata;
            }
        }
        String pasados = argumentos.stream().map(Tipo::toString).collect(Collectors.joining(", "));
        String esperados = porCantidad.stream().map(SimboloFuncion::getFirma).collect(Collectors.joining(" o "));
        error(nodo, lexema, "Los argumentos (" + pasados + ") no coinciden con " + que + ". Se esperaba " + esperados);
        return null;
    }

    /**
     * Un parametro {} Estructura recibe la direccion de la estructura en el
     * stack, asi que el argumento tiene que ser una estructura guardada en una
     * variable (o en un campo de ella), no un elemento de un arreglo del heap.
     */
    private void verificarReferencias(SimboloFuncion funcion, List<Expresion> argumentos) {
        for (int i = 0; i < argumentos.size(); i++) {
            SimboloVariable parametro = funcion.getParametros().get(i);
            if (!parametro.esPorReferencia() || !parametro.getTipo().esEstructura()) {
                continue;
            }
            boolean enVariable = argumentos.get(i) instanceof Acceso acceso
                    && acceso.getSimbolo() != null
                    && acceso.getSufijos().stream().allMatch(sufijo -> sufijo instanceof SufijoAtributo);
            if (!enVariable) {
                error(argumentos.get(i), "El parametro '" + parametro.getNombre() + "' recibe la estructura por "
                        + "referencia. Se esperaba una variable de tipo " + parametro.getTipo() + " o un campo de ella");
            }
        }
    }

    private static boolean calza(SimboloFuncion funcion, List<Tipo> argumentos, boolean exacto) {
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo parametro = funcion.getParametros().get(i).getTipo();
            boolean ok = exacto ? parametro.equals(argumentos.get(i))
                    : Compatibilidad.asignable(parametro, argumentos.get(i));
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /* ========================= Utilidades ========================= */

    /**
     * Pone el tipo definitivo a un nombre de tipo. PigLatin no sabe si
     * "Persona" es estructura o clase hasta que se registran todos los
     * archivos: aqui se decide.
     */
    private Tipo resolverTipo(Tipo tipo, Nodo nodo) {
        return switch (tipo.getBase()) {
            case ESTRUCTURA, OBJETO -> {
                SimboloEstructura declarado = tabla.buscarTipo(tipo.getNombre());
                if (declarado == null) {
                    error(nodo, tipo.getNombre(), "El tipo '" + tipo.getNombre() + "' no esta declarado. "
                            + "Se esperaba una estructura de un .y o una clase de un .z");
                    yield Tipo.ERROR;
                }
                yield declarado.getTipo();
            }
            case ARREGLO -> {
                Tipo elemento = resolverTipo(tipo.getTipoElemento(), nodo);
                yield elemento.esError() ? Tipo.ERROR : Tipo.arreglo(elemento, tipo.getDimensiones());
            }
            default -> tipo;
        };
    }

    /** Celdas que ocupa un valor: una estructura se aplana; lo demas es un valor o un puntero. */
    private int tamanio(Tipo tipo) {
        if (tipo.esEstructura()) {
            SimboloEstructura estructura = tabla.buscarTipo(tipo.getNombre());
            return estructura == null ? 1 : Math.max(1, estructura.getTamanio());
        }
        return 1;
    }

    private static boolean terminaEnLlamada(Acceso acceso) {
        List<Sufijo> sufijos = acceso.getSufijos();
        return sufijos.isEmpty() ? acceso.getLlamada() != null : sufijos.get(sufijos.size() - 1) instanceof SufijoMetodo;
    }

    /** Texto legible de un acceso para los mensajes: p.direccion.calle, m[..], this.x */
    private static String descripcion(Acceso acceso) {
        StringBuilder sb = new StringBuilder(acceso.esThis() ? "this"
                : acceso.getNombre() != null ? acceso.getNombre() : acceso.getLlamada().getNombre() + "()");
        for (Sufijo sufijo : acceso.getSufijos()) {
            sb.append(switch (sufijo) {
                case SufijoAtributo a -> "." + a.getNombre();
                case SufijoMetodo m -> "." + m.getNombre() + "()";
                default -> "[..]";
            });
        }
        return sb.toString();
    }

    /** La palabra reservada que el usuario escribio, segun el lenguaje del archivo. */
    private static String palabra(Nodo nodo, String y, String zetariano, String pig) {
        Lenguaje lenguaje = Lenguaje.desdeArchivo(nodo.getArchivo());
        if (lenguaje == null) {
            return y;
        }
        return switch (lenguaje) {
            case Y -> y;
            case ZETARIANO -> zetariano;
            case PIGLATIN -> pig;
        };
    }

    private static String mayuscula(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String lexema(Nodo nodo) {
        return switch (nodo) {
            case LiteralLista l -> "{...}";
            case Acceso a -> descripcion(a);
            case Literal l -> String.valueOf(l.getValor());
            case LlamadaFuncion l -> l.getNombre();
            case NuevoObjeto n -> n.getClase();
            case OperacionBinaria o -> o.getOperador().getSimbolo();
            default -> nodo.getEtiqueta();
        };
    }

    private void error(Nodo nodo, String descripcion) {
        error(nodo, lexema(nodo), descripcion);
    }

    private void error(Nodo nodo, String lexema, String descripcion) {
        errores.agregar(new ErrorCompilacion(TipoError.SEMANTICO, nodo.getArchivo(), lexema, descripcion,
                nodo.getLinea(), nodo.getColumna()));
    }

    /* ==== Nodos que se analizan desde las pasadas, no visitandolos sueltos ==== */

    @Override
    public Tipo visitarPrograma(Programa nodo) {
        throw new IllegalStateException("El programa se analiza con analizar(modulos)");
    }

    @Override
    public Tipo visitarImportacion(Importacion nodo) {
        throw new IllegalStateException("Los imports los resuelve ResolvedorImports");
    }

    @Override
    public Tipo visitarCampoEstructura(CampoEstructura nodo) {
        throw new IllegalStateException("Los campos se analizan al completar su estructura");
    }

    @Override
    public Tipo visitarDeclaracionClase(DeclaracionClase nodo) {
        throw new IllegalStateException("Las clases se analizan por pasadas");
    }

    @Override
    public Tipo visitarFuncion(Funcion nodo) {
        throw new IllegalStateException("Las funciones se analizan por pasadas");
    }

    @Override
    public Tipo visitarDeclaracionMetodo(DeclaracionMetodo nodo) {
        throw new IllegalStateException("Los metodos se analizan por pasadas");
    }

    @Override
    public Tipo visitarDeclaracionConstructor(DeclaracionConstructor nodo) {
        throw new IllegalStateException("Los constructores se analizan por pasadas");
    }

    @Override
    public Tipo visitarParametro(Parametro nodo) {
        throw new IllegalStateException("Los parametros se analizan con su funcion");
    }

    @Override
    public Tipo visitarRama(Rama nodo) {
        throw new IllegalStateException("Las ramas se analizan desde su Si");
    }

    @Override
    public Tipo visitarCaso(Caso nodo) {
        throw new IllegalStateException("Los casos se analizan desde su Elegir");
    }

    @Override
    public Tipo visitarSufijoAtributo(SufijoAtributo nodo) {
        throw new IllegalStateException("Los sufijos se analizan desde su Acceso");
    }

    @Override
    public Tipo visitarSufijoIndice(SufijoIndice nodo) {
        throw new IllegalStateException("Los sufijos se analizan desde su Acceso");
    }

    @Override
    public Tipo visitarSufijoMetodo(SufijoMetodo nodo) {
        throw new IllegalStateException("Los sufijos se analizan desde su Acceso");
    }
}
