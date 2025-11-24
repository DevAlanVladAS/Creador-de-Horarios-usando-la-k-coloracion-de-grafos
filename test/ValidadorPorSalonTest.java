package test;

import org.junit.Before;
import org.junit.Test;
import src.BloqueHorario;
import src.HorarioSemana;
import src.ResultadoValidacion;
import src.ValidadorPorSalon;

import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

public class ValidadorPorSalonTest {
    private ValidadorPorSalon validador;
    private HorarioSemana contexto;
    private BloqueHorario b1, b2, b3, b4, b5;

    @Before
    public void setUp() {
        validador = new ValidadorPorSalon();
        contexto = new HorarioSemana();

        // Caso 1: Mismo salón, mismo día, horarios solapados -> INVÁLIDO
        b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia A", "P1", "S1", "G1", true);
        b1.setDia("Lunes");
        b2 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "Materia B", "P2", "S1", "G2", true);
        b2.setDia("Lunes");

        // Caso 2: Mismo salón, mismo día, horarios NO solapados -> VÁLIDO
        b3 = new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(11, 0), "Materia C", "P1", "S1", "G1", true);
        b3.setDia("Lunes");

        // Caso 3: Mismo salón, horarios solapados, pero DIFERENTE día -> VÁLIDO
        b4 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia D", "P1", "S1", "G1", true);
        b4.setDia("Martes");

        // Caso 4: Diferente salón, mismo día, horarios solapados -> VÁLIDO
        b5 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "Materia E", "P2", "S2", "G1", true);
        b5.setDia("Lunes");
    }

    @Test
    public void testMismoSalonMismoDiaHorariosSolapadosInvalido() {
        List<ResultadoValidacion> res = validador.validar(b1, b2, contexto);
        assertFalse("Debe detectarse conflicto por salón", res.isEmpty());
    }

    @Test
    public void testMismoSalonMismoDiaHorariosNoSolapadosValido() {
        List<ResultadoValidacion> res = validador.validar(b1, b3, contexto);
        assertTrue("No debe haber conflicto", res.isEmpty());
    }

    @Test
    public void testMismoSalonDiferenteDiaValido() {
        List<ResultadoValidacion> res = validador.validar(b1, b4, contexto);
        assertTrue("No debe haber conflicto en días distintos", res.isEmpty());
    }

    @Test
    public void testDiferenteSalonMismoDiaValido() {
        List<ResultadoValidacion> res = validador.validar(b1, b5, contexto);
        assertTrue("Salones distintos no deben generar conflicto", res.isEmpty());
    }

    @Test
    public void testMensajeConflictoIncluyeSalon() {
        List<ResultadoValidacion> res = validador.validar(b1, b2, contexto);
        assertFalse(res.isEmpty());
        assertTrue(res.get(0).getMensaje().toLowerCase().contains("sal"));
    }

    @Test
    public void testSinConflictoNoRetornaResultados() {
        List<ResultadoValidacion> res = validador.validar(b1, b3, contexto);
        assertTrue(res.isEmpty());
    }
}
