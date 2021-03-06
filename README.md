# Bosch Keggle competition on H2O


## Dependencies
Sparkling Water 1.6.5 which integrates:
- Spark 1.6.1
- H2O 3.0 Shannon

Directiories in /opt/data/bosch/:
- /input - input data
- /model  - model data build from input files
- /output - and output submission file


## Status
- Step 1: Data preparation


## TODO
- Data munging - raw
- Machine Learning


## Project structure
 
```
├─ gradle/        - Gradle definition files
├─ src/           - Source code
│  ├─ main/       - Main implementation code 
│  │  ├─ scala/
│  ├─ test/       - Test code
│  │  ├─ scala/
├─ build.gradle   - Build file for this project
├─ gradlew        - Gradle wrapper 
```



## Project building

For building, please, use provided `gradlew` command:
```
./gradlew build
```

### Run
For running an application:
```
./gradlew run
```

## Running tests

To run tests, please, run:
```
./gradlew test
```

# Checking code style

To check codestyle:
```
./gradlew scalaStyle
```

## Creating and Running Spark Application

Create application assembly which can be directly submitted to Spark cluster:
```
./gradlew shadowJar
```
The command creates jar file `build/libs/bosch.jar` containing all necessary classes to run application on top of Spark cluster.

Submit application to Spark cluster (in this case, local cluster is used):
```
export MASTER='local-cluster[3,2,1024]'
$SPARK_HOME/bin/spark-submit --class com.yarenty.bosch.MLProcessor build/libs/bosch.jar
```

or 
```
export Maste='local'
$SPARK_HOME/bin/spark-submit --driver-memory=14g --class com.yarenty.bosch.MLProcessor build/libs/bosch.jar

```




