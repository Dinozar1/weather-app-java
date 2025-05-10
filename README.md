# WeatherAppJava

WeatherAppJava is a JavaFX-based collage project for retrieving and visualizing current and historical weather data using the Open-Meteo API. It supports caching data in Redis and provides a user-friendly interface for searching weather by city or coordinates.


## Prerequisites

- **Java 17 or higher** (JDK with JavaFX support)
- **Maven** (for building the project)
- **Docker** (for running Redis in a container)
- **Git** (for cloning the repository)

## Setup and Running on Linux (Arch Linux or Debian)

### 1. Install and Setup All Needed Packages

#### Arch Linux
1. Update the system and install required packages:
   ```bash
   sudo pacman -Syu
   sudo pacman -S jdk17-openjdk maven docker git
   ```
2. Install JavaFX (available in the AUR):
   ```bash
   yay -S openjfx
   ```
3. Enable and start the Docker service:
   ```bash
   sudo systemctl enable docker
   sudo systemctl start docker
   ```

#### Debian
1. Update the system and install required packages:
   ```bash
   sudo apt update && sudo apt upgrade -y
   sudo apt install openjdk-17-jdk maven docker.io git -y
   ```
2. Install JavaFX:
   ```bash
   sudo apt install openjfx -y
   ```
3. Enable and start the Docker service:
   ```bash
   sudo systemctl enable docker
   sudo systemctl start docker
   ```
4. Add your user to the Docker group to run Docker without `sudo`:
   ```bash
   sudo usermod -aG docker $USER
   ```

### 2. Run Container with Redis
1. Pull the Redis Docker image:
   ```bash
   docker pull redis
   ```
2. Run a Redis container, exposing port 6379:
   ```bash
   docker run --name weather-redis -p 6379:6379 -d redis
   ```
3. Verify that Redis is running:
   ```bash
   docker ps
   ```
   You should see `redis-container` in the output.

### 3. Run the Application
1. Clone the repository:
   ```bash
   git clone https://github.com/Dinozar1/weather-app-java.git
   cd weatherappjava
   ```
2. Build the project with Maven:
   ```bash
   mvn clean package
   ```
   This compiles the project and creates an executable JAR in the `target` directory.
3. Run the application:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.example.weatherappjava.WeatherApplication"
   ```
