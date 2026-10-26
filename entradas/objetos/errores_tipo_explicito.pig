## Cada declaracion o instruccion debe dar error semantico ##
import objetos.Persona.z
import objetos.Profesor.z

VARIABILES>
esto malo : null : numerus ;
esto peor : novus Persona() : Profesor ;
esto raro : novus Persona() : Inexistente ;
esto texto : 5 : textum ;
esto otro : novus Profesor(1, novus Persona(), novus Persona()) : Persona ;

MAIOR>
>> otro.getAlumno();
FINIS;
