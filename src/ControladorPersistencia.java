package src;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador responsable de guardar y cargar los horarios desde archivos.
 * Serializa el contenido en un JSON sencillo para facilitar el intercambio de datos.
 */
public class ControladorPersistencia {

    private static final Path ARCHIVO_POR_DEFECTO =
            Paths.get(System.getProperty("user.dir"), "horario_guardado.json");

    /**
     * Guarda el horario en un archivo JSON dentro del directorio del usuario.
     */
    public void guardar(HorarioSemana horario) {
        guardar(horario, ARCHIVO_POR_DEFECTO);
    }

    /**
     * Guarda el horario en el archivo indicado.
     */
    public void guardar(HorarioSemana horario, Path destino) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario a guardar no puede ser nulo");
        }
        if (destino == null) {
            destino = ARCHIVO_POR_DEFECTO;
        }

        try {
            Files.createDirectories(destino.getParent());
            String json = serializarHorario(horario);
            Files.writeString(destino, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el horario en " + destino, e);
        }
    }

    /**
     * Carga un horario desde la ruta indicada.
     */
    public HorarioSemana cargarHorario(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar una ruta válida");
        }
        Path origen = Paths.get(ruta);
        if (!Files.exists(origen)) {
            throw new IllegalArgumentException("No se encontró el archivo: " + ruta);
        }
        try {
            String data = Files.readString(origen, StandardCharsets.UTF_8);
            return deserializarHorario(data);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo " + ruta, e);
        }
    }

    public void guardarProyecto(ProyectoDatos datos, String ruta) throws IOException {
        if (datos == null) {
            throw new IllegalArgumentException("No hay datos de proyecto para guardar");
        }
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar una ruta válida");
        }
        Path destino = Paths.get(ruta);
        Path parent = destino.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(destino, datos.toJson(), StandardCharsets.UTF_8);
    }

    public ProyectoDatos cargarProyecto(String ruta) throws IOException {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar una ruta válida");
        }
        Path origen = Paths.get(ruta);
        if (!Files.exists(origen)) {
            throw new IllegalArgumentException("No se encontró el archivo: " + ruta);
        }
        String contenido = Files.readString(origen, StandardCharsets.UTF_8);
        return ProyectoDatos.fromJson(contenido);
    }

    private String serializarHorario(HorarioSemana horario) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"bloques\":[");
        List<BloqueHorario> bloques = horario.getBloques();
        for (int i = 0; i < bloques.size(); i++) {
            BloqueHorario bloque = bloques.get(i);
            sb.append("{")
                    .append("\"id\":\"").append(escape(bloque.getId())).append("\",")
                    .append("\"materia\":\"").append(escape(nullToEmpty(bloque.getMateria()))).append("\",")
                    .append("\"dia\":\"").append(escape(nullToEmpty(bloque.getDia()))).append("\",")
                    .append("\"horaInicio\":\"").append(formatHora(bloque.getHoraInicio())).append("\",")
                    .append("\"horaFin\":\"").append(formatHora(bloque.getHoraFin())).append("\",")
                    .append("\"profesorId\":\"").append(escape(nullToEmpty(bloque.getProfesorId()))).append("\",")
                    .append("\"salonId\":\"").append(escape(nullToEmpty(bloque.getSalonId()))).append("\",")
                    .append("\"grupoId\":\"").append(escape(nullToEmpty(bloque.getGrupoId()))).append("\",")
                    .append("\"profesor\":\"").append(escape(nullToEmpty(bloque.getProfesor()))).append("\",")
                    .append("\"salon\":\"").append(escape(nullToEmpty(bloque.getSalon()))).append("\",")
                    .append("\"grupo\":\"").append(escape(nullToEmpty(bloque.getGrupo()))).append("\"")
                    .append("}");
            if (i < bloques.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private HorarioSemana deserializarHorario(String json) {
        HorarioSemana horario = new HorarioSemana();
        Map<String, HorarioDia> diasCreados = new HashMap<>();
        for (Map<String, String> bloqueData : extraerObjetos(json)) {
            LocalTime inicio = parseHora(bloqueData.get("horaInicio"));
            LocalTime fin = parseHora(bloqueData.get("horaFin"));
            String materia = bloqueData.getOrDefault("materia", "");
            String profesorId = emptyToNull(bloqueData.get("profesorId"));
            String salonId = emptyToNull(bloqueData.get("salonId"));
            String grupoId = emptyToNull(bloqueData.get("grupoId"));

            BloqueHorario bloque = new BloqueHorario(inicio, fin, materia,
                    profesorId, salonId, grupoId, true);
            bloque.setProfesor(emptyToNull(bloqueData.get("profesor")));
            bloque.setSalon(emptyToNull(bloqueData.get("salon")));
            bloque.setGrupo(emptyToNull(bloqueData.get("grupo")));

            String diaAsignado = emptyToNull(bloqueData.get("dia"));
            if (diaAsignado == null) {
                horario.agregarBloqueSinAsignar(bloque);
            } else {
                HorarioDia dia = diasCreados.computeIfAbsent(diaAsignado, nombre -> {
                    HorarioDia nuevo = new HorarioDia(nombre);
                    horario.agregarDia(nuevo);
                    return nuevo;
                });
                dia.agregar(bloque);
            }
        }
        return horario;
    }

    private List<Map<String, String>> extraerObjetos(String json) {
        List<Map<String, String>> objetos = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return objetos;
        }
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start == -1 || end == -1 || start >= end) {
            return objetos;
        }
        String contenido = json.substring(start + 1, end);
        int nivel = 0;
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            if (c == '{') {
                nivel++;
                if (nivel == 1) {
                    actual.setLength(0);
                    continue;
                }
            }
            if (c == '}') {
                nivel--;
                if (nivel == 0) {
                    objetos.add(parseObjeto(actual.toString()));
                    continue;
                }
            }
            if (nivel >= 1) {
                actual.append(c);
            }
        }
        return objetos;
    }

    private Map<String, String> parseObjeto(String contenido) {
        Map<String, String> valores = new HashMap<>();
        int i = 0;
        while (i < contenido.length()) {
            while (i < contenido.length() && (Character.isWhitespace(contenido.charAt(i)) || contenido.charAt(i) == ',')) {
                i++;
            }
            if (i >= contenido.length() || contenido.charAt(i) != '"') {
                break;
            }
            int finClave = contenido.indexOf('"', i + 1);
            if (finClave == -1) {
                break;
            }
            String clave = unescape(contenido.substring(i + 1, finClave));
            i = finClave + 1;
            int idxDosPuntos = contenido.indexOf(':', i);
            if (idxDosPuntos == -1) {
                break;
            }
            i = idxDosPuntos + 1;
            while (i < contenido.length() && Character.isWhitespace(contenido.charAt(i))) {
                i++;
            }
            String valor;
            if (i < contenido.length() && contenido.charAt(i) == '"') {
                i++; // omite comilla inicial
                StringBuilder sb = new StringBuilder();
                boolean escapar = false;
                while (i < contenido.length()) {
                    char c = contenido.charAt(i);
                    if (escapar) {
                        sb.append(c);
                        escapar = false;
                    } else if (c == '\\') {
                        escapar = true;
                    } else if (c == '"') {
                        break;
                    } else {
                        sb.append(c);
                    }
                    i++;
                }
                valor = unescape(sb.toString());
                i++; // omite comilla de cierre
            } else {
                int finValor = i;
                while (finValor < contenido.length() && contenido.charAt(finValor) != ',') {
                    finValor++;
                }
                valor = contenido.substring(i, finValor).trim();
                i = finValor;
            }
            valores.put(clave, valor);
        }
        return valores;
    }

    private String formatHora(LocalTime hora) {
        return hora == null ? "" : hora.toString();
    }

    private LocalTime parseHora(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? LocalTime.of(7, 0) : LocalTime.parse(valor);
        } catch (Exception e) {
            return LocalTime.of(7, 0);
        }
    }

    private String nullToEmpty(String valor) {
        return valor == null ? "" : valor;
    }

    private String emptyToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    private String escape(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescape(String valor) {
        return valor.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
