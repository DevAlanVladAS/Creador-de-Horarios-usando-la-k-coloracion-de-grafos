class BloqueHorario extends HorarioComponent {

    private Profesor profesor;
    private Grupo grupo;
    private Materia materia;
    private Aula aula;
    private String dia;
    private String horaInicio;
    private String horaFin;

    public BloqueHorario(Profesor profesor, Grupo grupo, Materia materia, Aula aula,
            String dia, String horaInicio, String horaFin) {
        this.profesor = profesor;
        this.grupo = grupo;
        this.materia = materia;
        this.aula = aula;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    @Override
    public void mostrar() {
        System.out.println(dia + " " + horaInicio + "-" + horaFin
                + ": " + materia.getNombre()
                + " (" + grupo.getNombre() + "), "
                + profesor.getNombre()
                + " en " + aula.getNombre());
    }

    public void cambiarAula(Aula nuevaAula) {
        this.aula = nuevaAula;
    }

    public void moverBloque(String nuevoDia, String nuevaHoraInicio, String nuevaHoraFin) {
        this.dia = nuevoDia;
        this.horaInicio = nuevaHoraInicio;
        this.horaFin = nuevaHoraFin;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}
