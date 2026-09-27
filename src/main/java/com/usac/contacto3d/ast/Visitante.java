package com.usac.contacto3d.ast;

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
import com.usac.contacto3d.ast.expresiones.IncrementoDecremento;
import com.usac.contacto3d.ast.expresiones.Leer;
import com.usac.contacto3d.ast.expresiones.Literal;
import com.usac.contacto3d.ast.expresiones.LiteralLista;
import com.usac.contacto3d.ast.expresiones.LlamadaFuncion;
import com.usac.contacto3d.ast.expresiones.NuevoArreglo;
import com.usac.contacto3d.ast.expresiones.NuevoObjeto;
import com.usac.contacto3d.ast.expresiones.OperacionBinaria;
import com.usac.contacto3d.ast.expresiones.OperacionUnaria;
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
import com.usac.contacto3d.ast.instrucciones.Mientras;
import com.usac.contacto3d.ast.instrucciones.Para;
import com.usac.contacto3d.ast.instrucciones.Rama;
import com.usac.contacto3d.ast.instrucciones.Retorno;
import com.usac.contacto3d.ast.instrucciones.Si;

public interface Visitante<T> {

    T visitarPrograma(Programa nodo);

    T visitarImportacion(Importacion nodo);
    T visitarDeclaracionVariable(DeclaracionVariable nodo);
    T visitarDeclaracionArreglo(DeclaracionArreglo nodo);
    T visitarDeclaracionEstructura(DeclaracionEstructura nodo);
    T visitarCampoEstructura(CampoEstructura nodo);
    T visitarDeclaracionClase(DeclaracionClase nodo);
    T visitarFuncion(Funcion nodo);
    T visitarDeclaracionMetodo(DeclaracionMetodo nodo);
    T visitarDeclaracionConstructor(DeclaracionConstructor nodo);
    T visitarParametro(Parametro nodo);

    T visitarBloque(Bloque nodo);
    T visitarAsignacion(Asignacion nodo);
    T visitarSi(Si nodo);
    T visitarRama(Rama nodo);
    T visitarElegir(Elegir nodo);
    T visitarCaso(Caso nodo);
    T visitarMientras(Mientras nodo);
    T visitarHacer(Hacer nodo);
    T visitarPara(Para nodo);
    T visitarControlCiclo(ControlCiclo nodo);
    T visitarRetorno(Retorno nodo);
    T visitarImprimir(Imprimir nodo);

    T visitarLiteral(Literal nodo);
    T visitarOperacionBinaria(OperacionBinaria nodo);
    T visitarOperacionUnaria(OperacionUnaria nodo);
    T visitarAcceso(Acceso nodo);
    T visitarSufijoAtributo(SufijoAtributo nodo);
    T visitarSufijoIndice(SufijoIndice nodo);
    T visitarSufijoMetodo(SufijoMetodo nodo);
    T visitarLlamadaFuncion(LlamadaFuncion nodo);
    T visitarIncrementoDecremento(IncrementoDecremento nodo);
    T visitarTernario(Ternario nodo);
    T visitarNuevoObjeto(NuevoObjeto nodo);
    T visitarNuevoArreglo(NuevoArreglo nodo);
    T visitarLiteralLista(LiteralLista nodo);
    T visitarLeer(Leer nodo);
}
