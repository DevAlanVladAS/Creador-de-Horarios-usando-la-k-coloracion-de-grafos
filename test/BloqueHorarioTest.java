package test;

import src.BloqueHorario;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalTime;

public class BloqueHorarioTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidoHoraFinAntesDeInicio() {
        new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(9, 0), "Materia", "Prof", "Salon", "Grupo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidoHorasIguales() {
        new BloqueHorario(LocalTime.of(10, 0), LocalTime.of(10, 0), "Materia", "Prof", "Salon", "Grupo");
    }

    @Test
    public void testConstructorValido() {
        BloqueHorario bloque = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 30), "Materia", "Prof", "Salon", "Grupo");
        assertNotNull(bloque);
        assertEquals(LocalTime.of(9, 0), bloque.getHoraInicio());
        assertEquals(LocalTime.of(10, 30), bloque.getHoraFin());
    }

    @Test
    public void testSeSolapaCon_SinSolapamiento() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 0), "A", "P", "S", "G");
        BloqueHorario b2 = new BloqueHorario(LocalTime.of(9, 1), LocalTime.of(10, 0), "B", "P", "S", "G");
        assertFalse("Los bloques no deberían solaparse", b1.seSolapaCon(b2));
        assertFalse("La relación de solapamiento debe ser simétrica", b2.seSolapaCon(b1));
    }

    @Test
    public void testSeSolapaCon_HorasAdyacentes() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 0), "A", "P", "S", "G");
        BloqueHorario b2 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "B", "P", "S", "G");
        // La lógica de seSolapaCon considera que si uno termina justo cuando el otro empieza, no hay solapamiento.
        assertFalse("Bloques adyacentes no se solapan", b1.seSolapaCon(b2));
        assertFalse("La relación de solapamiento debe ser simétrica", b2.seSolapaCon(b1));
    }

    @Test
    public void testSeSolapaCon_SolapamientoParcial() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(9, 30), "A", "P", "S", "G");
        BloqueHorario b2 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "B", "P", "S", "G");
        assertTrue("Los bloques deberían solaparse", b1.seSolapaCon(b2));
        assertTrue("La relación de solapamiento debe ser simétrica", b2.seSolapaCon(b1));
    }

    @Test
    public void testSeSolapaCon_SolapamientoCompleto() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(10, 0), "A", "P", "S", "G");
        BloqueHorario b2 = new BloqueHorario(LocalTime.of(8, 30), LocalTime.of(9, 30), "B", "P", "S", "G");
        assertTrue("Un bloque contenido en otro se solapa", b1.seSolapaCon(b2));
        assertTrue("La relación de solapamiento debe ser simétrica", b2.seSolapaCon(b1));
    }

    @Test
    public void testSeSolapaCon_MismoBloque() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(8, 0), LocalTime.of(10, 0), "A", "P", "S", "G");
        assertTrue("Un bloque se solapa consigo mismo", b1.seSolapaCon(b1));
    }

    @Test
    public void testSeSolapaCon_HorasIdenticas() {
        BloqueHorario b1 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "A", "P", "S", "G");
        BloqueHorario b2 = new BloqueHorario(LocalTime.of(9, 0), LocalTime.of(10, 0), "B", "P", "S", "G");
        assertTrue("Bloques con horas idénticas se solapan", b1.seSolapaCon(b2));
        assertTrue("La relación de solapamiento debe ser simétrica", b2.seSolapaCon(b1));
    }
}