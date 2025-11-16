package test;

import src.BloqueHorario;
import src.HorarioDia;
import src.HorarioSemana;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class HorarioSemanaTest {

    private HorarioSemana horarioSemana;
    private BloqueHorario bloque1;
    private BloqueHorario bloque2;

    @Before
    public void setUp() {
        horarioSemana = new HorarioSemana();
        horarioSemana.agregarDia(new HorarioDia("Lunes"));
        horarioSemana.agregarDia(new HorarioDia("Martes"));

        bloque1 = new BloqueHorario(
            LocalTime.of(8, 0),
            LocalTime.of(9, 30),
            "Matematicas",
            "prof1", "salon1", "grupo1"
        );

        bloque2 = new BloqueHorario(
            LocalTime.of(10, 0),
            LocalTime.of(11, 30),
            "Fisica",
            "prof2", "salon2", "grupo2"
        );
    }

    @Test
    public void testAgregarDia() {
        assertEquals(2, horarioSemana.getDiasSemana().size());
        horarioSemana.agregarDia(new HorarioDia("Miercoles"));
        assertEquals(3, horarioSemana.getDiasSemana().size());
    }

    @Test
    public void testAgregarBloqueEnDia() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);

        List<BloqueHorario> bloquesLunes = horarioSemana.getDiasSemana().get(0).getBloques();
        assertEquals(1, bloquesLunes.size());
        assertEquals(bloque1.getId(), bloquesLunes.get(0).getId());
        assertEquals("Lunes", bloque1.getDia());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAgregarBloqueEnDiaInexistente() {
        horarioSemana.agregarBloqueEnDia("Sabado", bloque1);
    }

    @Test
    public void testAgregarBloqueSinAsignar() {
        horarioSemana.agregarBloqueSinAsignar(bloque1);
        assertEquals(1, horarioSemana.getBloquesSinAsignar().size());
        assertNull(bloque1.getDia());
    }

    @Test
    public void testObtenerBloquePorID_Asignado() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);
        Optional<BloqueHorario> encontrado = horarioSemana.obtenerBloquePorID(bloque1.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(bloque1.getId(), encontrado.get().getId());
    }

    @Test
    public void testObtenerBloquePorID_SinAsignar() {
        horarioSemana.agregarBloqueSinAsignar(bloque2);
        Optional<BloqueHorario> encontrado = horarioSemana.obtenerBloquePorID(bloque2.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(bloque2.getId(), encontrado.get().getId());
    }

    @Test
    public void testObtenerBloquePorID_NoExistente() {
        Optional<BloqueHorario> encontrado = horarioSemana.obtenerBloquePorID("id-no-existe");
        assertFalse(encontrado.isPresent());
    }

    @Test
    public void testAsignarBloqueADia() {
        horarioSemana.agregarBloqueSinAsignar(bloque1);
        assertEquals(1, horarioSemana.getBloquesSinAsignar().size());

        horarioSemana.asignarBloqueADia(bloque1.getId(), "Lunes");

        assertEquals("Lunes", bloque1.getDia());
        assertEquals(0, horarioSemana.getBloquesSinAsignar().size());
        assertEquals(1, horarioSemana.getDiasSemana().get(0).getBloques().size());
    }

    @Test
    public void testMoverBloque() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);
        assertEquals(1, horarioSemana.getDiasSemana().get(0).getBloques().size());
        assertEquals(0, horarioSemana.getDiasSemana().get(1).getBloques().size());

        horarioSemana.moverBloque(bloque1.getId(), "Martes");

        assertEquals("Martes", bloque1.getDia());
        assertEquals(0, horarioSemana.getDiasSemana().get(0).getBloques().size());
        assertEquals(1, horarioSemana.getDiasSemana().get(1).getBloques().size());
    }

    @Test
    public void testDesasignarBloqueADia() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);
        assertEquals(1, horarioSemana.getDiasSemana().get(0).getBloques().size());
        assertEquals(0, horarioSemana.getBloquesSinAsignar().size());

        horarioSemana.desasignarBloqueADia(bloque1.getId());

        assertNull(bloque1.getDia());
        assertEquals(0, horarioSemana.getDiasSemana().get(0).getBloques().size());
        assertEquals(1, horarioSemana.getBloquesSinAsignar().size());
    }

    @Test
    public void testGetDiaAsignado() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);
        Optional<String> dia = horarioSemana.getDiaAsignado(bloque1.getId());

        assertTrue(dia.isPresent());
        assertEquals("Lunes", dia.get());
    }

    @Test
    public void testGetTodosLosBloques() {
        horarioSemana.agregarBloqueEnDia("Lunes", bloque1);
        horarioSemana.agregarBloqueSinAsignar(bloque2);

        List<BloqueHorario> todos = horarioSemana.getBloques();
        assertEquals(2, todos.size());
        assertTrue(todos.stream().anyMatch(b -> b.getId().equals(bloque1.getId())));
        assertTrue(todos.stream().anyMatch(b -> b.getId().equals(bloque2.getId())));
    }
}