package src;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Representa un snapshot completo del proyecto actual para permitir guardarlo y restaurarlo.
 */
public class ProyectoDatos {

    private final ConfiguracionProyecto configuracion;
    private final List<Profesor> profesores;
    private final List<Salon> salones;
    private final List<GrupoEstudiantes> grupos;
    private final List<Materia> materias;
    private final List<AsignacionAcademica> asignaciones;
    private final List<BloqueHorario> bloques;
    private final Map<String, List<String>> asignacionBloques;

    public ProyectoDatos(ConfiguracionProyecto configuracionOriginal, CatalogoRecursos catalogo) {
        if (catalogo == null) {
            throw new IllegalArgumentException("El catÃ¡logo no puede ser nulo");
        }
        this.configuracion = copiarConfiguracion(configuracionOriginal);
        this.profesores = copiarProfesores(catalogo.getTodosLosProfesores());
        this.salones = copiarSalones(catalogo.getTodosLosSalones());
        this.grupos = copiarGrupos(catalogo.getTodosLosGrupos());
        this.materias = copiarMaterias(catalogo.getTodasLasMaterias());
        this.asignacionBloques = catalogo.getMapaAsignacionBloques();
        Map<String, BloqueHorario> bloquesPorId = new HashMap<>();
        this.bloques = copiarBloques(catalogo.getTodosLosBloques(), bloquesPorId);
        this.asignaciones = copiarAsignaciones(catalogo.getAsignaciones(), bloquesPorId, this.asignacionBloques);
    }

    private ProyectoDatos(ConfiguracionProyecto configuracion,
                          List<Profesor> profesores,
                          List<Salon> salones,
                          List<GrupoEstudiantes> grupos,
                          List<Materia> materias,
                          List<AsignacionAcademica> asignaciones,
                          List<BloqueHorario> bloques,
                          Map<String, List<String>> asignacionBloques) {
        this.configuracion = configuracion;
        this.profesores = profesores;
        this.salones = salones;
        this.grupos = grupos;
        this.materias = materias;
        this.asignaciones = asignaciones;
        this.bloques = bloques;
        this.asignacionBloques = asignacionBloques;
    }

    public void restaurarEn(InterfazGrafica interfaz, CatalogoRecursos catalogo) {
        if (interfaz == null || catalogo == null) {
            throw new IllegalArgumentException("Interfaz y catÃ¡logo son obligatorios");
        }

        Map<String, BloqueHorario> bloquesPorId = new HashMap<>();
        List<BloqueHorario> bloquesClon = copiarBloques(this.bloques, bloquesPorId);
        Map<String, List<String>> mapaAsignaciones = copiarMapaAsignacion(this.asignacionBloques);
        List<AsignacionAcademica> asignacionesClon = copiarAsignaciones(this.asignaciones, bloquesPorId, mapaAsignaciones);

        catalogo.restaurarDesdeDatos(
                copiarProfesores(this.profesores),
                copiarSalones(this.salones),
                copiarGrupos(this.grupos),
                copiarMaterias(this.materias),
                asignacionesClon,
                bloquesClon,
                mapaAsignaciones
        );

        GestorHorarios.getInstance().limpiarTodo();
        interfaz.establecerConfiguracionProyecto(copiarConfiguracion(this.configuracion));
        interfaz.recargarDesdeCatalogo();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> bloqueAsignacion = crearMapaBloqueAsignacion(asignacionBloques);

        sb.append("{");
        sb.append("\"configuracion\":").append(configuracionToJson());
        sb.append(",\"profesores\":").append(arrayToJson(profesores, this::profesorToJson));
        sb.append(",\"salones\":").append(arrayToJson(salones, this::salonToJson));
        sb.append(",\"grupos\":").append(arrayToJson(grupos, this::grupoToJson));
        sb.append(",\"materias\":").append(arrayToJson(materias, this::materiaToJson));
        sb.append(",\"asignaciones\":").append(arrayToJson(asignaciones, this::asignacionToJson));
        sb.append(",\"bloques\":").append(arrayToJson(bloques, b -> bloqueToJson(b, bloqueAsignacion)));
        sb.append("}");
        return sb.toString();
    }

    public static ProyectoDatos fromJson(String json) {
        SimpleJsonParser parser = new SimpleJsonParser(json);
        Object rootObj = parser.parse();
        if (!(rootObj instanceof Map)) {
            throw new IllegalArgumentException("El formato del archivo no es vÃ¡lido");
        }
        Map<String, Object> root = castToMap(rootObj);

        ConfiguracionProyecto configuracion = parseConfiguracion(castToMap(root.get("configuracion")));
        List<Profesor> profesores = parseProfesores(castToList(root.get("profesores")));
        List<Salon> salones = parseSalones(castToList(root.get("salones")));
        List<GrupoEstudiantes> grupos = parseGrupos(castToList(root.get("grupos")));
        List<Materia> materias = parseMaterias(castToList(root.get("materias")));
        List<AsignacionAcademica> asignaciones = parseAsignaciones(castToList(root.get("asignaciones")));

        Map<String, BloqueHorario> bloquesPorId = new HashMap<>();
        Map<String, List<String>> asignacionBloques = new HashMap<>();
        List<BloqueHorario> bloques = parseBloques(castToList(root.get("bloques")), bloquesPorId, asignacionBloques);

        if (!asignaciones.isEmpty()) {
            for (AsignacionAcademica asignacion : asignaciones) {
                List<String> ids = asignacionBloques.get(asignacion.getId());
                if (ids == null || ids.isEmpty()) {
                    continue;
                }
                List<BloqueHorario> asociados = new ArrayList<>();
                for (String id : ids) {
                    BloqueHorario bloque = bloquesPorId.get(id);
                    if (bloque != null) {
                        asociados.add(bloque);
                    }
                }
                if (!asociados.isEmpty()) {
                    asignacion.registrarBloques(asociados);
                }
            }
        }

        return new ProyectoDatos(configuracion, profesores, salones, grupos, materias, asignaciones, bloques, asignacionBloques);
    }

    private String configuracionToJson() {
        ConfiguracionProyecto config = configuracion != null ? configuracion : new ConfiguracionProyecto();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"nombreEscuela\":\"").append(escape(nullToEmpty(config.getNombreEscuela()))).append("\",");
        sb.append("\"grupos\":{");
        for (int grado = 1; grado <= 3; grado++) {
            if (grado > 1) {
                sb.append(",");
            }
            sb.append("\"").append(grado).append("\":").append(config.getCantidadGrupos(grado));
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    private String profesorToJson(Profesor profesor) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(profesor.getId())).append("\",")
                .append("\"nombre\":\"").append(escape(nullToEmpty(profesor.getNombre()))).append("\",")
                .append("\"materia\":\"").append(escape(nullToEmpty(profesor.getMateriaAsignada()))).append("\",")
                .append("\"dias\":\"").append(escape(joinList(profesor.getDiasDisponibles()))).append("\",")
                .append("\"horas\":\"").append(escape(joinList(profesor.getHorasDisponibles()))).append("\",")
                .append("\"horasSemanales\":").append(profesor.getHorasSemanales())
                .append("}")
                .toString();
    }

    private String salonToJson(Salon salon) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(salon.getId())).append("\",")
                .append("\"nombre\":\"").append(escape(nullToEmpty(salon.getNombre()))).append("\",")
                .append("\"capacidad\":").append(salon.getCapacidad())
                .append("}")
                .toString();
    }

    private String grupoToJson(GrupoEstudiantes grupo) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(grupo.getId())).append("\",")
                .append("\"nombre\":\"").append(escape(nullToEmpty(grupo.getNombre()))).append("\",")
                .append("\"grado\":").append(grupo.getGrado()).append(",")
                .append("\"profesores\":\"").append(escape(joinList(grupo.getProfesorIds()))).append("\"")
                .append("}")
                .toString();
    }

    private String materiaToJson(Materia materia) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(materia.getId())).append("\",")
                .append("\"nombre\":\"").append(escape(nullToEmpty(materia.getNombre()))).append("\",")
                .append("\"horas\":").append(materia.getHorasSugeridas())
                .append("}")
                .toString();
    }

    private String asignacionToJson(AsignacionAcademica asignacion) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(asignacion.getId())).append("\",")
                .append("\"grupoId\":\"").append(escape(nullToEmpty(asignacion.getGrupoId()))).append("\",")
                .append("\"profesorId\":\"").append(escape(nullToEmpty(asignacion.getProfesorId()))).append("\",")
                .append("\"materiaId\":\"").append(escape(nullToEmpty(asignacion.getMateriaId()))).append("\",")
                .append("\"salonId\":\"").append(escape(nullToEmpty(asignacion.getSalonId()))).append("\",")
                .append("\"horasSemanales\":").append(asignacion.getHorasSemanales())
                .append("}")
                .toString();
    }
    private String bloqueToJson(BloqueHorario bloque, Map<String, String> asignacionPorBloque) {
        return new StringBuilder()
                .append("{")
                .append("\"id\":\"").append(escape(bloque.getId())).append("\",")
                .append("\"materia\":\"").append(escape(nullToEmpty(bloque.getMateria()))).append("\",")
                .append("\"dia\":\"").append(escape(nullToEmpty(bloque.getDia()))).append("\",")
                .append("\"horaInicio\":\"").append(escape(formatHora(bloque.getHoraInicio()))).append("\",")
                .append("\"horaFin\":\"").append(escape(formatHora(bloque.getHoraFin()))).append("\",")
                .append("\"profesorId\":\"").append(escape(nullToEmpty(bloque.getProfesorId()))).append("\",")
                .append("\"salonId\":\"").append(escape(nullToEmpty(bloque.getSalonId()))).append("\",")
                .append("\"grupoId\":\"").append(escape(nullToEmpty(bloque.getGrupoId()))).append("\",")
                .append("\"profesor\":\"").append(escape(nullToEmpty(bloque.getProfesor()))).append("\",")
                .append("\"salon\":\"").append(escape(nullToEmpty(bloque.getSalon()))).append("\",")
                .append("\"grupo\":\"").append(escape(nullToEmpty(bloque.getGrupo()))).append("\",")
                .append("\"asignacionId\":\"").append(escape(nullToEmpty(asignacionPorBloque.get(bloque.getId())))).append("\"")
                .append("}")
                .toString();
    }

    private <T> String arrayToJson(List<T> datos, Function<T, String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        if (datos != null) {
            for (int i = 0; i < datos.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(mapper.apply(datos.get(i)));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static ConfiguracionProyecto copiarConfiguracion(ConfiguracionProyecto original) {
        ConfiguracionProyecto copia = new ConfiguracionProyecto();
        if (original != null) {
            copia.setNombreEscuela(original.getNombreEscuela());
            for (int grado = 1; grado <= 3; grado++) {
                copia.setCantidadGrupos(grado, original.getCantidadGrupos(grado));
            }
        }
        return copia;
    }

    private static List<Profesor> copiarProfesores(List<Profesor> origen) {
        List<Profesor> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (Profesor profesor : origen) {
            Profesor clon = new Profesor(
                    profesor.getId(),
                    profesor.getNombre(),
                    profesor.getMateriaAsignada(),
                    profesor.getDiasDisponibles(),
                    profesor.getHorasDisponibles(),
                    profesor.getHorasSemanales()
            );
            copia.add(clon);
        }
        return copia;
    }

    private static List<Salon> copiarSalones(List<Salon> origen) {
        List<Salon> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (Salon salon : origen) {
            copia.add(new Salon(salon.getId(), salon.getNombre(), salon.getCapacidad()));
        }
        return copia;
    }

    private static List<GrupoEstudiantes> copiarGrupos(List<GrupoEstudiantes> origen) {
        List<GrupoEstudiantes> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (GrupoEstudiantes grupo : origen) {
            copia.add(new GrupoEstudiantes(grupo.getId(), grupo.getNombre(), grupo.getGrado(), grupo.getProfesorIds()));
        }
        return copia;
    }

    private static List<Materia> copiarMaterias(List<Materia> origen) {
        List<Materia> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (Materia materia : origen) {
            copia.add(new Materia(materia.getId(), materia.getNombre(), materia.getHorasSugeridas()));
        }
        return copia;
    }

    private static List<BloqueHorario> copiarBloques(List<BloqueHorario> origen, Map<String, BloqueHorario> destinoMap) {
        List<BloqueHorario> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (BloqueHorario bloque : origen) {
            BloqueHorario clon = new BloqueHorario(
                    bloque.getId(),
                    bloque.getHoraInicio(),
                    bloque.getHoraFin(),
                    bloque.getMateria(),
                    bloque.getProfesor(),
                    bloque.getSalon(),
                    bloque.getGrupo()
            );
            clon.setProfesorId(bloque.getProfesorId());
            clon.setSalonId(bloque.getSalonId());
            clon.setGrupoId(bloque.getGrupoId());
            clon.setDia(bloque.getDia());
            copia.add(clon);
            if (destinoMap != null) {
                destinoMap.put(clon.getId(), clon);
            }
        }
        return copia;
    }

    private static List<AsignacionAcademica> copiarAsignaciones(List<AsignacionAcademica> origen,
                                                                Map<String, BloqueHorario> bloquesPorId,
                                                                Map<String, List<String>> mapaAsignacionBloques) {
        List<AsignacionAcademica> copia = new ArrayList<>();
        if (origen == null) {
            return copia;
        }
        for (AsignacionAcademica asignacion : origen) {
            AsignacionAcademica clon = new AsignacionAcademica(
                    asignacion.getId(),
                    asignacion.getGrupoId(),
                    asignacion.getProfesorId(),
                    asignacion.getMateriaId(),
                    asignacion.getSalonId(),
                    asignacion.getHorasSemanales()
            );
            List<String> ids = mapaAsignacionBloques != null ? mapaAsignacionBloques.get(asignacion.getId()) : null;
            if ((ids == null || ids.isEmpty()) && asignacion.getBloqueIds() != null) {
                ids = asignacion.getBloqueIds();
            }
            if (ids != null && !ids.isEmpty() && bloquesPorId != null) {
                List<BloqueHorario> asociados = new ArrayList<>();
                for (String id : ids) {
                    BloqueHorario bloque = bloquesPorId.get(id);
                    if (bloque != null) {
                        asociados.add(bloque);
                    }
                }
                if (!asociados.isEmpty()) {
                    clon.registrarBloques(asociados);
                }
            }
            copia.add(clon);
        }
        return copia;
    }
    private static Map<String, List<String>> copiarMapaAsignacion(Map<String, List<String>> original) {
        Map<String, List<String>> copia = new HashMap<>();
        if (original == null) {
            return copia;
        }
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            copia.put(entry.getKey(), entry.getValue() != null ? new ArrayList<>(entry.getValue()) : new ArrayList<>());
        }
        return copia;
    }

    private static Map<String, String> crearMapaBloqueAsignacion(Map<String, List<String>> relacion) {
        Map<String, String> inverso = new HashMap<>();
        if (relacion == null) {
            return inverso;
        }
        for (Map.Entry<String, List<String>> entry : relacion.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            for (String bloqueId : entry.getValue()) {
                inverso.put(bloqueId, entry.getKey());
            }
        }
        return inverso;
    }

    private static ConfiguracionProyecto parseConfiguracion(Map<String, Object> data) {
        ConfiguracionProyecto configuracion = new ConfiguracionProyecto();
        configuracion.setNombreEscuela(asString(data.get("nombreEscuela")));
        Map<String, Object> grupos = castToMap(data.get("grupos"));
        for (int grado = 1; grado <= 3; grado++) {
            String key = String.valueOf(grado);
            configuracion.setCantidadGrupos(grado, parseInt(grupos.get(key)));
        }
        return configuracion;
    }

    private static List<Profesor> parseProfesores(List<Object> data) {
        List<Profesor> profesores = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            profesores.add(new Profesor(
                    asString(valores.get("id")),
                    asString(valores.get("nombre")),
                    asString(valores.get("materia")),
                    parseLista(asString(valores.get("dias"))),
                    parseLista(asString(valores.get("horas"))),
                    parseInt(valores.get("horasSemanales"))
            ));
        }
        return profesores;
    }

    private static List<Salon> parseSalones(List<Object> data) {
        List<Salon> salones = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            salones.add(new Salon(
                    asString(valores.get("id")),
                    asString(valores.get("nombre")),
                    parseInt(valores.get("capacidad"))
            ));
        }
        return salones;
    }

    private static List<GrupoEstudiantes> parseGrupos(List<Object> data) {
        List<GrupoEstudiantes> grupos = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            grupos.add(new GrupoEstudiantes(
                    asString(valores.get("id")),
                    asString(valores.get("nombre")),
                    parseInt(valores.get("grado")),
                    parseLista(asString(valores.get("profesores")))
            ));
        }
        return grupos;
    }

    private static List<Materia> parseMaterias(List<Object> data) {
        List<Materia> materias = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            materias.add(new Materia(
                    asString(valores.get("id")),
                    asString(valores.get("nombre")),
                    parseInt(valores.get("horas"))
            ));
        }
        return materias;
    }

    private static List<AsignacionAcademica> parseAsignaciones(List<Object> data) {
        List<AsignacionAcademica> asignaciones = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            asignaciones.add(new AsignacionAcademica(
                    asString(valores.get("id")),
                    asString(valores.get("grupoId")),
                    asString(valores.get("profesorId")),
                    asString(valores.get("materiaId")),
                    emptyToNull(asString(valores.get("salonId"))),
                    parseInt(valores.get("horasSemanales"))
            ));
        }
        return asignaciones;
    }

    private static List<BloqueHorario> parseBloques(List<Object> data,
                                                    Map<String, BloqueHorario> bloquesPorId,
                                                    Map<String, List<String>> asignacionBloques) {
        List<BloqueHorario> bloques = new ArrayList<>();
        for (Object item : data) {
            Map<String, Object> valores = castToMap(item);
            String id = asString(valores.get("id"));
            String materia = asString(valores.get("materia"));
            LocalTime inicio = parseHora(asString(valores.get("horaInicio")));
            LocalTime fin = parseHora(asString(valores.get("horaFin")));
            String profesorNombre = asString(valores.get("profesor"));
            String salonNombre = asString(valores.get("salon"));
            String grupoNombre = asString(valores.get("grupo"));

            BloqueHorario bloque = new BloqueHorario(
                    id,
                    inicio,
                    fin,
                    materia,
                    profesorNombre,
                    salonNombre,
                    grupoNombre
            );
            bloque.setProfesorId(emptyToNull(asString(valores.get("profesorId"))));
            bloque.setSalonId(emptyToNull(asString(valores.get("salonId"))));
            bloque.setGrupoId(emptyToNull(asString(valores.get("grupoId"))));
            bloque.setDia(emptyToNull(asString(valores.get("dia"))));

            bloques.add(bloque);
            if (bloquesPorId != null) {
                bloquesPorId.put(bloque.getId(), bloque);
            }

            String asignacionId = emptyToNull(asString(valores.get("asignacionId")));
            if (asignacionId != null) {
                asignacionBloques.computeIfAbsent(asignacionId, k -> new ArrayList<>()).add(bloque.getId());
            }
        }
        return bloques;
    }
    private static Map<String, Object> castToMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private static List<Object> castToList(Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return Collections.emptyList();
    }

    private static String asString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    private static int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            String texto = asString(value);
            if (texto.isBlank()) {
                return 0;
            }
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static LocalTime parseHora(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(valor);
        } catch (Exception ex) {
            return null;
        }
    }

    private static List<String> parseLista(String data) {
        if (data == null || data.isBlank()) {
            return new ArrayList<>();
        }
        String[] partes = data.split("\\|");
        List<String> lista = new ArrayList<>();
        for (String parte : partes) {
            if (!parte.isBlank()) {
                lista.add(parte.trim());
            }
        }
        return lista;
    }

    private static String joinList(List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return "";
        }
        return String.join("|", valores);
    }

    private static String nullToEmpty(String valor) {
        return valor == null ? "" : valor;
    }

    private static String emptyToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    private static String formatHora(LocalTime hora) {
        return hora == null ? "" : hora.toString();
    }

    private static String escape(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 32) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
    private static final class SimpleJsonParser {
        private final String text;
        private int index;

        SimpleJsonParser(String text) {
            this.text = text != null ? text.trim() : "";
            this.index = 0;
        }

        Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                return null;
            }
            char c = text.charAt(index);
            switch (c) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseString();
                case 't':
                    return parseLiteral("true", Boolean.TRUE);
                case 'f':
                    return parseLiteral("false", Boolean.FALSE);
                case 'n':
                    return parseLiteral("null", null);
                default:
                    if (c == '-' || Character.isDigit(c)) {
                        return parseNumber();
                    }
                    throw new IllegalArgumentException("CarÃ¡cter inesperado en JSON: " + c);
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new HashMap<>();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return map;
            }
            while (index < text.length()) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char c = peek();
                if (c == '}') {
                    index++;
                    break;
                }
                expect(',');
                skipWhitespace();
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                index++;
                return list;
            }
            while (index < text.length()) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                char c = peek();
                if (c == ']') {
                    index++;
                    break;
                }
                expect(',');
                skipWhitespace();
            }
            return list;
        }

        private Object parseNumber() {
            int start = index;
            if (text.charAt(index) == '-') {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
            if (index < text.length() && text.charAt(index) == '.') {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                index++;
                if (text.charAt(index) == '+' || text.charAt(index) == '-') {
                    index++;
                }
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            String numero = text.substring(start, index);
            if (numero.contains(".") || numero.contains("e") || numero.contains("E")) {
                return Double.parseDouble(numero);
            }
            try {
                return Integer.parseInt(numero);
            } catch (NumberFormatException e) {
                return Long.parseLong(numero);
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    if (index >= text.length()) {
                        break;
                    }
                    char escape = text.charAt(index++);
                    switch (escape) {
                        case '"':
                        case '\\':
                        case '/':
                            sb.append(escape);
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            if (index + 4 <= text.length()) {
                                String hex = text.substring(index, index + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                index += 4;
                            }
                            break;
                        default:
                            sb.append(escape);
                            break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Object parseLiteral(String literal, Object value) {
            if (text.startsWith(literal, index)) {
                index += literal.length();
                return value;
            }
            throw new IllegalArgumentException("Se esperaba " + literal + " en JSON");
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private void expect(char esperado) {
            if (index >= text.length() || text.charAt(index) != esperado) {
                throw new IllegalArgumentException("Se esperaba '" + esperado + "' en JSON");
            }
            index++;
        }

        private char peek() {
            return index < text.length() ? text.charAt(index) : '\0';
        }
    }
}





