# Imagen base con Java 17
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copiar solo el codigo fuente
COPY src ./src

# Compilar todas las clases respetando los paquetes
RUN javac -d out $(find src -name "*.java")

# Clase principal (usa el paquete src)
CMD ["java", "-cp", "out", "src.Main"]
