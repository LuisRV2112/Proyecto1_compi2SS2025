package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.ast.declaraciones.Importacion;
import com.usac.contacto3d.errores.ErrorCompilacion;
import com.usac.contacto3d.errores.ListaErrores;
import com.usac.contacto3d.errores.TipoError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Carga los archivos que importa un .pig.
 *
 * Solo el .pig importa: los .y y .z no tienen import, asi que no puede haber
 * ciclos y basta con no cargar dos veces el mismo archivo.
 *
 * Donde se busca "import carpeta.Persona.z" (el auxiliar aclaro que no importa
 * en que carpeta este, mientras el principal lo importe):
 *   1. carpeta/Persona.z junto al .pig
 *   2. carpeta/Persona.z junto a la carpeta del .pig (el .pig esta dentro de
 *      una carpeta del proyecto e importa desde la raiz)
 *   3. Persona.z junto al .pig
 *   4. cualquier Persona.z dentro de la carpeta del .pig, a cualquier profundidad
 */
public class ResolvedorImports {

    /** Parsea un archivo y construye su AST; null si tuvo errores lexicos o sintacticos. */
    @FunctionalInterface
    public interface Cargador {
        Programa cargar(Path archivo) throws IOException;
    }

    private static final int PROFUNDIDAD_BUSQUEDA = 8;

    private final ListaErrores errores;
    private final Cargador cargador;

    public ResolvedorImports(ListaErrores errores, Cargador cargador) {
        this.errores = errores;
        this.cargador = cargador;
    }

    /**
     * @return el principal seguido de cada archivo importado que se pudo cargar.
     *         Los que tuvieron errores de parseo ya los reporto el cargador.
     */
    public List<Programa> resolver(Path rutaPrincipal, Programa principal) throws IOException {
        List<Programa> modulos = new ArrayList<>();
        modulos.add(principal);

        Path base = rutaPrincipal.toAbsolutePath().getParent();
        Set<Path> cargados = new HashSet<>();
        cargados.add(rutaPrincipal.toRealPath());

        for (Importacion importacion : principal.getImportaciones()) {
            String extension = importacion.getExtension();
            if (!extension.equals("y") && !extension.equals("z")) {
                error(importacion, "Solo se pueden importar archivos .y o .z; se importo un ." + extension);
                continue;
            }
            Path ruta = buscar(base, importacion);
            if (ruta == null) {
                error(importacion, "No se encontro el archivo importado '" + importacion.getRutaRelativa()
                        + "'. Se esperaba en la carpeta de " + principal.getArchivo() + " o debajo de ella");
                continue;
            }
            if (!cargados.add(ruta.toRealPath())) {
                continue;   // importado dos veces: se carga una sola
            }
            Programa modulo = cargador.cargar(ruta);
            if (modulo != null) {
                modulos.add(modulo);
            }
        }
        return modulos;
    }

    private Path buscar(Path base, Importacion importacion) throws IOException {
        Path relativa = Path.of(importacion.getRutaRelativa());
        List<Path> candidatos = new ArrayList<>();
        candidatos.add(base.resolve(relativa));
        if (base.getParent() != null) {
            candidatos.add(base.getParent().resolve(relativa));
        }
        candidatos.add(base.resolve(importacion.getNombreArchivo()));

        for (Path candidato : candidatos) {
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
        }

        List<Path> encontrados;
        try (Stream<Path> recorrido = Files.walk(base, PROFUNDIDAD_BUSQUEDA)) {
            encontrados = recorrido
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(importacion.getNombreArchivo()))
                    .sorted()
                    .toList();
        }
        // Si hay varios con el mismo nombre, se prefiere el que coincide con la carpeta del import
        return encontrados.stream().filter(p -> p.endsWith(relativa)).findFirst()
                .orElse(encontrados.isEmpty() ? null : encontrados.get(0));
    }

    private void error(Importacion importacion, String descripcion) {
        errores.agregar(new ErrorCompilacion(TipoError.SEMANTICO, importacion.getArchivo(),
                String.join(".", importacion.getPartes()), descripcion,
                importacion.getLinea(), importacion.getColumna()));
    }
}
