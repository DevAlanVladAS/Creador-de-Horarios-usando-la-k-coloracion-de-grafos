# Imagen base con Java 17
FROM openjdk:17-jdk-slim

# Crear directorio de trabajo
WORKDIR /app

# Copiar solo el código fuente y otros archivos necesarios
COPY src ./src

# Compilar los archivos Java (recursivo dentro de /src)
RUN javac $(find src -name "*.java") -d out

# Establecer el classpath y ejecutar la clase principal
CMD ["java", "-cp", "out", "InterfazGrafica"]