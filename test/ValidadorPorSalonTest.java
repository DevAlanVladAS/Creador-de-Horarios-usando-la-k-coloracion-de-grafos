package test;

import src.BloqueHorario;
import src.CatalogoRecursos;
import src.Salon;
import src.ValidadorPorSalon;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.Arrays;

public class ValidadorPorSalonTest {
    private ValidadorPorSalon validador;
    private CatalogoRecursos catalogo;
    private Salon salon1, salon2;
    private BloqueHorario b1, b2, b3, b4, b5;

    @Before
    public void setUp() {
        catalogo = CatalogoRecursos.getInstance();
        catalogo.reset();

        salon1 = new Salon("Salon 101", 0);
        salon2 = new Salon("Salon 102", 0);
        catalogo.addSalon(salon1);
        catalogo.addSalon(salon2);

        validador = new ValidadorPorSalon(catalogo);

        // Caso 1: Mismo salón, mismo día, horarios solapados -> INVÁLIDO
        b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia A", "P1", salon1.getId(), "G1", true);
        b1.setDia("Lunes");
        b2 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "Materia B", "P2", salon1.getId(), "G2", true);
        b2.setDia("Lunes");

        // Caso 2: Mismo salón, mismo día, horarios NO solapados -> VÁLIDO
        b3 = new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(11, 0), "Materia C", "P1", salon1.getId(), "G1", true);
        b3.setDia("Lunes");

        // Caso 3: Mismo salón, horarios solapados, pero DIFERENTE día -> VÁLIDO
        b4 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia D", "P1", salon1.getId(), "G1", true);
        b4.setDia("Martes");

        // Caso 4: Diferente salón, mismo día, horarios solapados -> VÁLIDO
        b5 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia E", "P2", salon2.getId(), "G1", true);
        b5.setDia("Lunes");
    }

    @Test
    public void testMismoSalonMismoDiaHorariosSolapadosInvalido() {
        assertFalse("Mismo salón, mismo día y horario solapado debe ser inválido", validador.esValido(b1, b2));
    }

    @Test
    public void testMismoSalonMismoDiaHorariosNoSolapadosValido() {
        assertTrue("Mismo salón, mismo día pero horarios no solapados debe ser válido", validador.esValido(b1, b3));
    }

    @Test
    public void testMismoSalonDiferenteDiaValido() {
        assertTrue("Mismo salón, horarios solapados pero en días diferentes debe ser válido", validador.esValido(b1, b4));
    }

    @Test
    public void testDiferenteSalonMismoDiaValido() {
        assertTrue("Diferente salón en el mismo horario y día debe ser válido", validador.esValido(b1, b5));
    }

    @Test
    public void testGetTipoConflictoSalon() {
        if (!validador.esValido(b1, b2)) {
            String conflicto = validador.getTipoConflicto();
            assertNotNull(conflicto);
            assertTrue(conflicto.contains("Salón"));
        } else {
            fail("Se esperaba un conflicto que no ocurrió");
        }
    }

    @Test
    public void testGetTipoConflictoSinConflicto() {
        if (!validador.esValido(b1, b3)) {
            fail("No se esperaba un conflicto");
        }
        assertTrue(validador.esValido(b1, b3));
    }
}
