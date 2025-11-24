package test;

import src.AsignacionAcademica;
import src.CatalogoRecursos;
import src.GrupoEstudiantes;
import src.Materia;
import src.Profesor;
import src.Salon;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CatalogoRecursosTest {

    private CatalogoRecursos catalogo;

    @Before
    public void setUp() {
        catalogo = CatalogoRecursos.getInstance();
        catalogo.reset();
    }

    @Test
    public void testSingletonInstance() {
        CatalogoRecursos otraInstancia = CatalogoRecursos.getInstance();
        assertSame("getInstance() debe retornar siempre la misma instancia", catalogo, otraInstancia);
    }

    @Test
    public void testAddAndGetProfesor() {
        Profesor p = new Profesor("Dr. Garcia", null, Arrays.asList("Lunes"), null);
        catalogo.addProfesor(p);

        Profesor obtenido = catalogo.obtenerProfesorPorId(p.getId());
        assertNotNull(obtenido);
        assertEquals("Dr. Garcia", obtenido.getNombre());

        List<Profesor> todos = catalogo.getTodosLosProfesores();
        assertEquals(1, todos.size());
        assertEquals(p.getId(), todos.get(0).getId());
    }

    @Test
    public void testAddAndGetSalon() {
        Salon s = new Salon("Laboratorio 1", 0);
        catalogo.addSalon(s);

        Salon obtenido = catalogo.obtenerSalonPorId(s.getId());
        assertNotNull(obtenido);
        assertEquals("Laboratorio 1", obtenido.getNombre());

        List<Salon> todos = catalogo.getTodosLosSalones();
        assertEquals(1, todos.size());
        assertEquals(s.getId(), todos.get(0).getId());
    }

    @Test
    public void testAddAndGetGrupo() {
        GrupoEstudiantes g = new GrupoEstudiantes("Grupo 301", 0);
        catalogo.addGrupo(g);

        GrupoEstudiantes obtenido = catalogo.obtenerGrupoPorId(g.getId());
        assertNotNull(obtenido);
        assertEquals("Grupo 301", obtenido.getNombre());

        List<GrupoEstudiantes> todos = catalogo.getTodosLosGrupos();
        assertEquals(1, todos.size());
        assertEquals(g.getId(), todos.get(0).getId());
    }

    @Test
    public void testRemoveRecurso() {
        Profesor p = new Profesor("Dr. Temporal", null, Arrays.asList("Lunes"), null);
        catalogo.addProfesor(p);
        assertNotNull(catalogo.obtenerProfesorPorId(p.getId()));

        catalogo.removeProfesor(p.getId());
        assertNull(catalogo.obtenerProfesorPorId(p.getId()));
    }

    @Test
    public void testReset() {
        catalogo.addProfesor(new Profesor("Profesor", null, Arrays.asList("Lunes"), null));
        catalogo.addSalon(new Salon("Salon", 0));
        catalogo.addGrupo(new GrupoEstudiantes("Grupo", 0));

        assertFalse(catalogo.getTodosLosProfesores().isEmpty());
        assertFalse(catalogo.getTodosLosSalones().isEmpty());
        assertFalse(catalogo.getTodosLosGrupos().isEmpty());

        catalogo.reset();

        assertTrue(catalogo.getTodosLosProfesores().isEmpty());
        assertTrue(catalogo.getTodosLosSalones().isEmpty());
        assertTrue(catalogo.getTodosLosGrupos().isEmpty());
    }

    @Test
    public void testFindByName() {
        Profesor p = new Profesor("Dr. Garcia", null, Arrays.asList("Lunes"), null);
        Salon s = new Salon("Sala A", 0);
        GrupoEstudiantes g = new GrupoEstudiantes("Grupo 1A", 0);
        catalogo.addProfesor(p);
        catalogo.addSalon(s);
        catalogo.addGrupo(g);

        Optional<Profesor> profEncontrado = catalogo.findProfesorByName("Dr. Garcia");
        assertTrue(profEncontrado.isPresent());
        assertEquals(p.getId(), profEncontrado.get().getId());

        Optional<Salon> salonEncontrado = catalogo.findSalonByName("Sala A");
        assertTrue(salonEncontrado.isPresent());
        assertEquals(s.getId(), salonEncontrado.get().getId());

        Optional<GrupoEstudiantes> grupoEncontrado = catalogo.findGrupoByName("Grupo 1A");
        assertTrue(grupoEncontrado.isPresent());
        assertEquals(g.getId(), grupoEncontrado.get().getId());
    }

    @Test
    public void testFindByNameCaseInsensitive() {
        Profesor p = new Profesor("Dr. Garcia", null, Arrays.asList("Lunes"), null);
        catalogo.addProfesor(p);

        Optional<Profesor> profEncontrado = catalogo.findProfesorByName("dr. garcia");
        assertTrue("La busqueda debe ser insensible a mayusculas/minusculas", profEncontrado.isPresent());
        assertEquals(p.getId(), profEncontrado.get().getId());
    }

    @Test
    public void testFindByNameNotFound() {
        Optional<Profesor> profEncontrado = catalogo.findProfesorByName("Inexistente");
        assertFalse(profEncontrado.isPresent());
    }

    @Test
    public void testMateriasPrecargadas() {
        List<Materia> materias = catalogo.getTodasLasMaterias();
        assertFalse("Debe haber materias precargadas", materias.isEmpty());
        assertTrue(materias.stream().anyMatch(m -> m.getNombre().equalsIgnoreCase("Matematicas")));
    }

    @Test
    public void testAsignacionGeneraBloques() {
        Profesor profesor = new Profesor("Profesor Test", "Matematicas");
        catalogo.addProfesor(profesor);
        GrupoEstudiantes grupo = new GrupoEstudiantes("1A", 0);
        catalogo.addGrupo(grupo);
        Salon salon = new Salon("Aula 1", 30);
        catalogo.addSalon(salon);
        Materia materia = catalogo.findMateriaByName("Matematicas").orElseGet(() -> {
            Materia nueva = new Materia("Matematicas", 5);
            catalogo.addMateria(nueva);
            return nueva;
        });

        AsignacionAcademica asignacion = new AsignacionAcademica(
                grupo.getId(),
                profesor.getId(),
                materia.getId(),
                salon.getId(),
                3
        );
        catalogo.addAsignacionAcademica(asignacion);

        assertEquals(3, catalogo.getBloquesByGrupoId(grupo.getId()).size());
    }
}
