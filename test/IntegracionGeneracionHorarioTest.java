package test;

import src.AdaptadorGraficaDeHorarios;
import src.BloqueHorario;
import src.CatalogoRecursos;
import src.EstrategiaColoracion;
import src.EstrategiaGeneracion;
import src.GrupoEstudiantes;
import src.HorarioSemana;
import src.Profesor;
import src.Salon;
import src.Validador;
import src.ValidadorPorProfesor;
import src.ValidadorPorSalon;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegracionGeneracionHorarioTest {
    private CatalogoRecursos catalogo;
    private List<BloqueHorario> bloques;

    @Before
    public void setUp() {
        catalogo = CatalogoRecursos.getInstance();
        catalogo.reset();
        crearRecursosEnCatalogo(catalogo);
        bloques = crearBloques(catalogo);
    }

    @Test
    public void testGeneracionHorarioSinConflictos() {
        // 1. Crear la gráfica
        AdaptadorGraficaDeHorarios grafica = new AdaptadorGraficaDeHorarios(bloques, catalogo);
        grafica.construirGraficaAutomaticamente();

        // 2. Generar el horario usando la estrategia
        EstrategiaGeneracion estrategia = new EstrategiaColoracion();
        HorarioSemana horario = estrategia.generarHorario(grafica);

        // 3. Validar el resultado
        assertNotNull(horario);
        // Comprobar que todos los bloques fueron asignados (en un caso ideal)
        assertEquals("Todos los bloques deberían haber sido asignados", bloques.size(), horario.getBloques().size());
        assertTrue("No debería haber bloques sin asignar", horario.getBloquesSinAsignar().isEmpty());

        // 4. Validar que no hay conflictos en el horario generado
        List<Validador> validadores = Arrays.asList(
            new ValidadorPorProfesor(catalogo),
            new ValidadorPorSalon(catalogo)
        );

        List<BloqueHorario> bloquesAsignados = horario.getBloques();
        boolean hayConflictos = false;

        for (int i = 0; i < bloquesAsignados.size(); i++) {
            for (int j = i + 1; j < bloquesAsignados.size(); j++) {
                BloqueHorario b1 = bloquesAsignados.get(i);
                BloqueHorario b2 = bloquesAsignados.get(j);

                // Solo validar si están en el mismo día
                if (b1.getDia() != null && b1.getDia().equals(b2.getDia())) {
                    for (Validador v : validadores) {
                        if (!v.esValido(b1, b2)) {
                            System.out.println("Conflicto detectado: " + v.getTipoConflicto());
                            System.out.println("Bloque 1: " + b1.getMateria() + " en " + b1.getDia() + " " + b1.getHoraInicio());
                            System.out.println("Bloque 2: " + b2.getMateria() + " en " + b2.getDia() + " " + b2.getHoraInicio());
                            hayConflictos = true;
                            break;
                        }
                    }
                }
            }
            if (hayConflictos) break;
        }

        assertFalse("El horario generado no debería tener conflictos", hayConflictos);
    }

    private void crearRecursosEnCatalogo(CatalogoRecursos cat) {
        List<String> todosLosDias = Arrays.asList("Lunes", "Martes", "Miercoles", "Jueves", "Viernes");
        // Correccion: El último parámetro debe ser una lista de horas, no de días. Asi ya jala bien =)
        List<String> todasLasHoras = Arrays.asList("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00");
        cat.addProfesor(new Profesor("Prof A", "Matematicas", todosLosDias, todasLasHoras));
        cat.addProfesor(new Profesor("Prof B", "Ciencias", todosLosDias, todasLasHoras));
        cat.addSalon(new Salon("Salon 101", 0));
        cat.addSalon(new Salon("Salon 102", 0));
        cat.addGrupo(new GrupoEstudiantes("Grupo 1"));
        cat.addGrupo(new GrupoEstudiantes("Grupo 2"));
    }

    private List<BloqueHorario> crearBloques(CatalogoRecursos cat) {
        Profesor pA = cat.findProfesorByName("Prof A").get();
        Profesor pB = cat.findProfesorByName("Prof B").get();
        Salon s101 = cat.findSalonByName("Salon 101").get();
        Salon s102 = cat.findSalonByName("Salon 102").get();
        GrupoEstudiantes g1 = cat.findGrupoByName("Grupo 1").get();
        GrupoEstudiantes g2 = cat.findGrupoByName("Grupo 2").get();

        List<BloqueHorario> blocks = new ArrayList<>();

        // Escenario con conflictos claros para forzar la coloración
        // Bloque 1 y 2: Conflicto de profesor
        blocks.add(new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 0), "Algebra", pA.getId(), s101.getId(), g1.getId(), true));
        blocks.add(new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 0), "Calculo", pA.getId(), s102.getId(), g2.getId(), true));

        // Bloque 3 y 4: Conflicto de salón
        blocks.add(new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(11, 0), "Fisica", pA.getId(), s101.getId(), g1.getId(), true));
        blocks.add(new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(11, 0), "Quimica", pB.getId(), s101.getId(), g2.getId(), true));

        // Bloque 5 y 6: Conflicto de grupo
        blocks.add(new BloqueHorario(LocalTime.of(12, 0), LocalTime.of(13, 0), "Historia", pA.getId(), s101.getId(), g1.getId(), true));
        blocks.add(new BloqueHorario(LocalTime.of(12, 0), LocalTime.of(13, 0), "Geografia", pB.getId(), s102.getId(), g1.getId(), true));

        // Bloque 7: Sin conflictos con los demás en el mismo slot de tiempo
        blocks.add(new BloqueHorario(LocalTime.of(14, 0), LocalTime.of(15, 0), "Arte", pB.getId(), s102.getId(), g2.getId(), true));

        return blocks;
    }
}