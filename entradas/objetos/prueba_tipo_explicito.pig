## Las tres formas del enunciado: tipo explicito, novus anidado y null ##
import objetos.Persona.z
import objetos.Profesor.z

VARIABILES>
esto miObjeto : novus Persona(12, "Profesor") : Persona ;
esto otroObjeto : novus Profesor(40, miObjeto, novus Persona()) : Persona ;
esto vacio : null : Persona ;
esto prof : novus Profesor(50, novus Persona(1, "Eva"), miObjeto) : Profesor ;

MAIOR>
>> miObjeto.getNombre();
>> otroObjeto.getNombre() >> " tiene " >> otroObjeto.getEdad();
>> prof.getNombre() >> " ensena a " >> prof.getAlumno();
si (vacio == null) {
    >> "el objeto es nulo";
} finis;
vacio = prof;
>> "ahora vacio es " >> vacio.getNombre();
FINIS;
