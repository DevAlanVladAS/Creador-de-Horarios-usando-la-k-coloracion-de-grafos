import java.time.LocalTime;

/**
 * Validador que verifica que bloques con conflicto de horario (mismo salon/profesor/grupo)
 * tengan horarios distintos. Usa CatalogoRecursos para validar disponibilidad de salones.
 */
public class ValidadorPorSalon implements Validador {

    private CatalogoRecursos catalogo;

    public ValidadorPorSalon(CatalogoRecursos catalogo) {
        this.catalogo = catalogo;
    }

    // Constructor sin catálogo (por compatibilidad)
    public ValidadorPorSalon() {
        this.catalogo = null;
    }

    @Override
    public boolean esValido(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        // Si comparten salon y dia, verificar que no se solapen en tiempo
        if (mismoSalon(bloqueA, bloqueB) && mismodia(bloqueA, bloqueB)) {
            return !horariosSeSuperponen(bloqueA, bloqueB);
        }
        
        // Validar disponibilidad del salón si se tiene catálogo
        if (catalogo != null && bloqueA.getDia() != null) {
            if (!validarSalon(bloqueA)) return false;
        }
        if (catalogo != null && bloqueB.getDia() != null) {
            if (!validarSalon(bloqueB)) return false;
        }
        
        return true;
    }

    /**
     * Valida si el salón de un bloque está disponible en el día asignado.
     */
    private boolean validarSalon(BloqueHorario bloque) {
        String salonId = bloque.getSalonId();
        if (salonId == null) return true; // Si no tiene id, permitir
        
        Salon s = catalogo.obtenerSalonPorId(salonId);
        if (s == null) return false; // Salón no encontrado
        
        // Por ahora no implementamos disponibilidad de salones,
        // pero la estructura está lista si se necesita
        return true;
    }

    private boolean mismoSalon(BloqueHorario a, BloqueHorario b) {
        if (a.getSalonId() != null && b.getSalonId() != null) {
            return a.getSalonId().equals(b.getSalonId());
        }
        if (a.getSalon() != null && b.getSalon() != null) {
            return a.getSalon().equalsIgnoreCase(b.getSalon());
        }
        return false;
    }

    private boolean mismodia(BloqueHorario a, BloqueHorario b) {
        String diaA = a.getDia();
        String diaB = b.getDia();
        return diaA != null && diaB != null && diaA.equalsIgnoreCase(diaB);
    }

    private boolean horariosSeSuperponen(BloqueHorario a, BloqueHorario b) {
        LocalTime inicio1 = a.getHoraInicio();
        LocalTime fin1 = a.getHoraFin();
        LocalTime inicio2 = b.getHoraInicio();
        LocalTime fin2 = b.getHoraFin();
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    @Override
    public String getTipoConflicto(BloqueHorario bloqueA, BloqueHorario bloqueB) {
        if (mismoSalon(bloqueA, bloqueB) && mismodia(bloqueA, bloqueB)) {
            return "Conflicto: mismo salon en horarios solapados";
        }
        return "Conflicto de salon desconocido";
    }
}
