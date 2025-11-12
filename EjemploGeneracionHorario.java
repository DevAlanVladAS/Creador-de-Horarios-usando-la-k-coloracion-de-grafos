import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ejemplo de uso del sistema de horarios con k-coloración de grafos.
 * 
 * Flujo:
 * 1. Creamos un catalogo de recursos (profesores, salones, grupos)
 * 2. Creamos bloques horarios (sin asignar dia)
 * 3. Construir grafica de conflictos automaticamente
 * 4. Asignar bloques a dias usando algoritmos o manualmente
 * 5. Validar con validadores
 * 6. Mostrar resultado
 */
public class EjemploGeneracionHorario {

    public static void main(String[] args) {
       

        // Paso 1: Crear catalogo de recursos
        System.out.println("Creando catalogo de recursos...");
        CatalogoRecursos catalogo = crearCatalogo();

        // Paso 2: Crear HorarioSemana y bloques
        System.out.println("Creando estructura de horario y bloques...");
        List<BloqueHorario> bloques = crearBloques(catalogo);
        System.out.println("" + bloques.size() + " bloques creados");

            // Crear la gráfica de horarios
            AdaptadorGraficaDeHorarios grafica = new AdaptadorGraficaDeHorarios(bloques, catalogo);
            
        // Mostrar adyacencias (conflictos)
        System.out.println("\nAdyacencias (conflictos):");
        Map<String, Set<String>> adyacencias = grafica.obtenerAdyacencias();
        if (grafica.obtenerNumeroAristas() == 0) {
            System.out.println("  [OK] No hay conflictos de horario (los bloques no se solapan en tiempo)");
        } else {
            for (String id : adyacencias.keySet()) {
                if (!adyacencias.get(id).isEmpty()) {
                    System.out.println(id.substring(0, 8) + "... -> " + adyacencias.get(id));
                }
            }
        }

        // Generar horario usando EstrategiaColoracion
        EstrategiaGeneracion estrategia = new EstrategiaColoracion();
        HorarioSemana horarioGenerado = estrategia.generarHorario(grafica);

        // Mostrar resultado de la coloracion
        System.out.println("\nResultado de la coloracion (asignacion de dias):");
        for (HorarioDia diaObj : horarioGenerado.getDiasSemana()) {
            String dia = diaObj.getDia();
            System.out.println("Dia: " + dia);
            for (BloqueHorario bloque : diaObj.getBloques()) {
                System.out.println("  - " + bloque.getMateria() + " | Profesor: " + catalogo.obtenerProfesorPorId(bloque.getProfesorId()).getNombre() + " | Grupo: " + catalogo.obtenerGrupoPorId(bloque.getGrupoId()).getNombre() + " | Salon: " + catalogo.obtenerSalonPorId(bloque.getSalonId()).getNombre());
            }
        }

        // Mostrar estadisticas de la grafica
        System.out.println("\n--- Estadisticas de la grafica de conflictos ---");
        System.out.println("Nodos: " + grafica.obtenerNumeroNodos());
        System.out.println("Aristas (conflictos): " + grafica.obtenerNumeroAristas());
        var stats = grafica.obtenerEstadisticas();
        System.out.println("Grado promedio: " + String.format("%.2f", stats.get("grado_promedio")));
        System.out.println("Densidad: " + String.format("%.2f", stats.get("densidad")));

        // Validar horario generado
        System.out.println("\n--- Validacion del horario generado ---");
        ValidadorPorDia validadorDia = new ValidadorPorDia(
            Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes"),
            catalogo
        );
        ValidadorPorProfesor validadorProfe = new ValidadorPorProfesor(catalogo);
        ValidadorPorSalon validadorSalon = new ValidadorPorSalon(catalogo);
        validarHorario(horarioGenerado, validadorDia, validadorProfe, validadorSalon);

        // Mostrar resultado final
        System.out.println("\n--- Horario asignado ---");
        horarioGenerado.mostrarInfo();

        System.out.println("\n=== FIN DEL EJEMPLO ===");
    }

    private static CatalogoRecursos crearCatalogo() {
        CatalogoRecursos catalogo = new CatalogoRecursos();

        // Crear profesores con disponibilidad
        Profesor prof1 = new Profesor("Dr. Garcia", Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes"));
        Profesor prof2 = new Profesor("Dra. Lopez", Arrays.asList("Martes", "Miercoles", "Jueves")); // No lunes ni viernes
        Profesor prof3 = new Profesor("Ing. Martinez", Arrays.asList("Lunes", "Miercoles", "Viernes"));

        catalogo.addProfesor(prof1);
        catalogo.addProfesor(prof2);
        catalogo.addProfesor(prof3);

        // Crear salones
        Salon salon1 = new Salon("Sala A");
        Salon salon2 = new Salon("Sala B");
        Salon salon3 = new Salon("Laboratorio");

        catalogo.addSalon(salon1);
        catalogo.addSalon(salon2);
        catalogo.addSalon(salon3);

        // Crear grupos
        GrupoEstudiantes grupo1 = new GrupoEstudiantes("Grupo 1A");
        GrupoEstudiantes grupo2 = new GrupoEstudiantes("Grupo 2B");

        catalogo.addGrupo(grupo1);
        catalogo.addGrupo(grupo2);

        System.out.println("  [OK] Catalogo creado con:");
        System.out.println("    - 3 profesores");
        System.out.println("    - 3 salones");
        System.out.println("    - 2 grupos");

        return catalogo;
    }


    private static List<BloqueHorario> crearBloques(CatalogoRecursos catalogo) {
        // Obtener ids de recursos
        Profesor prof1 = obtenerProfesor(catalogo, "Dr. Garcia");
        Profesor prof2 = obtenerProfesor(catalogo, "Ing. Martinez");
        Profesor prof3 = obtenerProfesor(catalogo, "Dra. Lopez");
        
        Salon salon1 = obtenerSalon(catalogo, "Sala A");
        Salon salon2 = obtenerSalon(catalogo, "Sala B");
        Salon salon3 = obtenerSalon(catalogo, "Laboratorio");
        
        GrupoEstudiantes grupo1 = obtenerGrupo(catalogo, "Grupo 1A");
        GrupoEstudiantes grupo2 = obtenerGrupo(catalogo, "Grupo 2B");

        List<BloqueHorario> bloques = new java.util.ArrayList<>();
        
        bloques.add(new BloqueHorario(
            LocalTime.of(8, 0), LocalTime.of(9, 30),
            "Matematicas",
            prof1.getId(),
            salon1.getId(),
            grupo1.getId(),
            true
        ));
        
        bloques.add(new BloqueHorario(
            LocalTime.of(9, 45), LocalTime.of(11, 15),
            "Fisica",
            prof2.getId(),
            salon3.getId(),
            grupo1.getId(),
            true
        ));
        
        bloques.add(new BloqueHorario(
            LocalTime.of(11, 30), LocalTime.of(13, 0),
            "Quimica",
            prof3.getId(),
            salon3.getId(),
            grupo2.getId(),
            true
        ));
        
        bloques.add(new BloqueHorario(
            LocalTime.of(14, 0), LocalTime.of(15, 30),
            "Historia",
            prof1.getId(),
            salon2.getId(),
            grupo2.getId(),
            true
        ));
        
        bloques.add(new BloqueHorario(
            LocalTime.of(15, 45), LocalTime.of(17, 15),
            "Literatura",
            prof3.getId(),
            salon1.getId(),
            grupo1.getId(),
            true
        ));

        System.out.println("  [OK] " + bloques.size() + " bloques creados");
        return bloques;
    }

    private static void validarHorario(HorarioSemana horario, ValidadorPorDia validadorDia, 
                                        ValidadorPorProfesor validadorProfe, ValidadorPorSalon validadorSalon) {
        List<BloqueHorario> bloques = horario.getBloques();
        int validos = 0;
        int conflictos = 0;

        for (int i = 0; i < bloques.size(); i++) {
            for (int j = i + 1; j < bloques.size(); j++) {
                BloqueHorario b1 = bloques.get(i);
                BloqueHorario b2 = bloques.get(j);

                if (validadorDia.esValido(b1, b2) && 
                    validadorProfe.esValido(b1, b2) &&
                    validadorSalon.esValido(b1, b2)) {
                    validos++;
                } else {
                    conflictos++;
                    if (!validadorDia.esValido(b1, b2)) System.out.println("  [CONFLICTO DIA] " + b1.getMateria() + " vs " + b2.getMateria());
                    if (!validadorProfe.esValido(b1, b2)) System.out.println("  [CONFLICTO PROFESOR] " + b1.getMateria() + " vs " + b2.getMateria());
                    if (!validadorSalon.esValido(b1, b2)) System.out.println("  [CONFLICTO SALON] " + b1.getMateria() + " vs " + b2.getMateria());
                }
            }
        }

        System.out.println("  [OK] Pares validos: " + validos);
        if (conflictos > 0) System.out.println("  [AVISO] Pares con conflicto: " + conflictos);
    }

  

    // Helpers
    private static Profesor obtenerProfesor(CatalogoRecursos catalogo, String nombre) {
        return catalogo.findProfesorByName(nombre)
            .orElseThrow(() -> new RuntimeException("Profesor no encontrado: " + nombre));
    }

    private static Salon obtenerSalon(CatalogoRecursos catalogo, String nombre) {
        return catalogo.findSalonByName(nombre)
            .orElseThrow(() -> new RuntimeException("Salón no encontrado: " + nombre));
    }

    private static GrupoEstudiantes obtenerGrupo(CatalogoRecursos catalogo, String nombre) {
        return catalogo.findGrupoByName(nombre)
            .orElseThrow(() -> new RuntimeException("Grupo no encontrado: " + nombre));
    }
}
