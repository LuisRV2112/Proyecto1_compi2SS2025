## null en PigLatin: asignar, comparar y devolver ##
import objetos.Persona.z

VARIABILES>
esto p : novus Persona(12, "Ana");
esto vacio : novus Persona();

MAIOR>
>> "vacio == null: " >> (vacio == null);
vacio = null;
>> "vacio == null: " >> (vacio == null);
>> "p != null: " >> (p != null);
>> "companero nulo: " >> (p.companero == null);
p.companero = novus Persona(30, "Beto");
si (p.companero != null) {
    >> "companero: " >> p.companero.getNombre();
} finis;
p.companero = null;
si (p.companero == null) {
    >> "sin companero";
} finis;
FINIS;
