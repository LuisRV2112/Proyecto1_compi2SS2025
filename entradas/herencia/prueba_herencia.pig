## Herencia, encapsulamiento, @Override y super ##
import herencia.Animal.z
import herencia.Perro.z
import herencia.Gato.z
import herencia.Cachorro.z

VARIABILES>
esto a : novus Animal("Toby");
esto p : novus Perro("Firulais");
esto g : novus Gato("Misifu");
esto c : novus Cachorro("Bolita", 3);

MAIOR>
a.comer();
p.comer();
p.ladrar();
g.comer();
>> g.ficha();
c.comer();
>> c.ficha();
FINIS;
