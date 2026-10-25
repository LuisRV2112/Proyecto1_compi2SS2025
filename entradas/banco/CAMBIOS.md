# Cambios respecto al banco de pruebas entregado

Un solo cambio, autorizado por el ingeniero:

| Archivo | Antes | Después | Por qué |
|---|---|---|---|
| `banco/Cuenta.z` línea 6 | `int movimientos;` | `protected int movimientos;` | Sin modificador es `private`, y `CuentaCorriente` (subclase) lo usa en las líneas 18 y 36 |

Con el archivo original el compilador reporta exactamente esos dos usos como error semántico.
