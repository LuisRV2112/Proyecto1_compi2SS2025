import PruebaY.y
import Figura.z
import Circulo.z
import Cuadrado.z
import Pruebas.z

VARIABILES>
esto n : numerus 3;
esto d : decimalis 1.5;
esto t : textum "pig";
esto l : littera 'p';
esto b : bool verum;
esto rapida : falsus;
esto leido : textum;
esto num : numerus;
series v[4] : numerus {1, 2, 3, 4};
series m[2][2] : numerus { {1, 2}, {3, 4} };
series nombres[2] : textum;
esto pt : Punto {7, 8};
esto pr : novus Pruebas();
esto fig : Figura;
esto c : novus Circulo(5, 1.0) : Figura;

MUNERA>
ratio numerus cuadrado(esto x : numerus) {
    reddere x * x;
} finis;

actio repetir(esto s : textum, esto veces : numerus) {
    per (esto i : numerus 0; i < veces; i++) {
        >> s >> i;
    } finis;
} finis;

MAIOR>
>> "== pig variables";
n = n + 2;
d = d * 2;
t = t + "latin";
b = non b;
>> n >> d >> t >> l >> b >> rapida;

>> "== pig lectura";
leido <<
num <<
>> "pig leyo: " >> leido >> num + 1;

>> "== pig condicionales";
si (n > 10) {
    >> "mayor";
} aliter (n == 5) {
    >> "cinco";
} aliter {
    >> "menor";
} finis;

>> "== pig ciclos";
esto suma : numerus 0;
per (esto i : numerus 0; i < 10; i++) {
    si (i == 3) { perge; } finis;
    si (i == 7) { interrumpe; } finis;
    suma = suma + i;
} finis;
>> "per: " >> suma;
dum (suma > 10) {
    suma = suma - 4;
} finis;
>> "dum: " >> suma;
facere {
    suma = suma + 5;
} dum (suma < 20);
>> "facere: " >> suma;

>> "== pig arreglos";
v[2] = v[0] + v[3];
m[1][0] = m[0][1] * 10;
nombres[0] = "a";
nombres[1] = nombres[0] + "b";
>> v[2] >> m[1][0] >> nombres[1];

>> "== pig funciones e importaciones";
>> cuadrado(6);
repetir("r", 2);
>> pt.x + pt.y;
pt.y = 80;
>> pt.y;
>> yCondicionales(12) >> yCondicionales(10) >> yCondicionales(1);
>> factorial(6);

>> "== y";
yVariables();
yLectura();
yElegir(1);
yElegir(2);
yElegir(9);
yCiclos();
yArreglos();
yEstructurasYFunciones();

>> "== z";
pr.zVariables();
pr.zLectura();
pr.zCondicionales(10);
pr.zCondicionales(2);
pr.zCiclos();
pr.zArreglos();
pr.zObjetosYNull();
pr.zMetodos();
pr.zHerenciaYPolimorfismo();

>> "== pig objetos y null";
>> c.area();
>> c.describir();
fig = null;
si (fig == null) {
    >> "fig es null";
} finis;
fig = novus Cuadrado(3, 4.0);
>> fig.describir();
>> "fin";

FINIS;
