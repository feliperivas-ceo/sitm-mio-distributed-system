# sitm-mio-distributed-system

Sistema distribuido para monitoreo en tiempo real, procesamiento de eventos y análisis operativo de la red de transporte SITM-MIO, usando ZeroC ICE y patrones de arquitectura distribuida.

---

## Inicio rápido

```bash
cd sitm-mio-system
./gradlew clean build -x test
java -jar build/libs/sitm-mio-system-0.0.1-SNAPSHOT.jar
```

| Interfaz | URL |
|---|---|
| Panel operador | http://localhost:8080/dashboard.html |
| Mapa ciudadano | http://localhost:8080/index.html |
| API REST | http://localhost:8080/api/ |
| Consola H2 | http://localhost:8080/h2-console |
| WebSocket | ws://localhost:8080/ws |
| ICE RPC (buses) | tcp://localhost:10000 |

Credenciales de desarrollo (sembradas automáticamente):

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | admin123 | ADMIN |
| controlador1 | ctrl123 | CONTROLADOR — Zona Norte |
| controlador2 | ctrl123 | CONTROLADOR — Zona Sur |

---

## Arquitectura de despliegue

```plantuml
@startuml Despliegue-SITM-MIO

!theme plain
skinparam packageStyle rectangle
skinparam defaultTextAlignment center
skinparam wrapWidth 160
skinparam linetype ortho

node "Máquina del Desarrollador (localhost)" as DEV {

  node "Proceso JVM — sitm-mio-system-0.0.1-SNAPSHOT.jar" as JVM {

    package "Interfaces de Entrada" as IFACES {
      [HTTP :8080\nAPI REST + Archivos Estáticos] as HTTP
      [WebSocket :8080/ws\nSTOMP sobre HTTP] as WS
      [ICE TCP :10000\nRPC ZeroC ICE] as ICE
    }

    package "Aplicación Spring Boot" as SBA {
      [Spring Security\nRoles: ADMIN / CONTROLADOR] as SEC
      [Controladores\nDashboard · Mapa · Analíticas\nEventos · Zonas · Alertas · Usuarios] as CTRL
      [SimpleBroker /topic\nEnvío en tiempo real] as WS_BROKER
      [Tareas Programadas\ntamaño del pool: 5] as SCHED
    }

    package "Núcleo de Procesamiento Distribuido" as DPROC {
      [ServidorCentral\nServant ICE] as CS
      queue "ColaDatagramas\nBlockingQueue en memoria" as QUEUE
      [MaestroTrabajador\nhilo consumidor] as MW
      [ProcesadorDeHilos\n5 hilos trabajadores] as TP
      [TrabajadorDatagrama\npor datagrama] as DW
      [ProcesadorEventos] as EP
      [ProcesadorGPS] as GPS
    }

    package "Módulo de Analíticas" as ANALYTICS {
      [MaestroVelocidadDistribuida] as DSM
      [TrabajadorVelocidad-1] as DSW1
      [TrabajadorVelocidad-N] as DSWN
      [FusionadorResultadosParciales] as PRM
    }

    package "Capa de Persistencia" as PERSIST {
      [Spring Data JPA\nBus · Ruta · Estacion\nEvento · Zona · Usuario · Datagrama] as JPA
      file "data/datagramas_live.csv\n(volcado cada 50 registros)" as CSV
      database "BD H2 En Memoria\njdbc:h2:mem:sitmmiodb\nefímera — se pierde al reiniciar" as H2
      [DatabaseConfig\nsiembra automática al iniciar:\n5 rutas · 3 zonas · 3 usuarios\n20 buses · 100 eventos] as SEED
    }

    package "Simulador de Buses (en proceso)" as BUSSIM {
      [SimuladorMultiBus] as MBS
      [SimuladorBus-1] as BS1
      [SimuladorBus-N] as BSN
      [GeneradorEventos] as EG
      [SensorGPS] as SGPS
      [SensorMotor] as SMOT
      [SensorPuertas] as SPRT
      [ClienteIceBus\nProxy ICE → :10000] as ICECLI
    }

  }

  node "Cliente Navegador" as BROWSER {
    [dashboard.html\nPanel del Operador] as UI_DASH
    [index.html\nMapa Ciudadano Público] as UI_PUB
    [app.js / map.js / analytics.js] as JS
  }

}

package "NO PRESENTE — Brechas de Infraestructura" as GAPS #EEEEEE {
  [Docker / Compose] as DOCKER #lightgray
  [Kubernetes] as K8S #lightgray
  [Pipeline CI/CD] as CICD #lightgray
  database "PostgreSQL\n(driver disponible, sin configurar)" as PRODDB #lightgray
  [Proxy Inverso / Nginx] as NGINX #lightgray
}

' --- Interfaces de entrada ---
HTTP --> SEC         : archivos estáticos + REST
WS   --> WS_BROKER   : tramas STOMP
ICE  --> CS          : RPC Datagrama

' --- Spring Boot Application ---
SEC       --> CTRL
CTRL     <--> WS_BROKER : push /topic/buses\n/topic/eventos

' --- Pipeline de procesamiento ---
CS    --> QUEUE
QUEUE --> MW
MW    --> TP
TP    --> DW
DW    --> EP
DW    --> GPS
DW    --> CSV
DW    --> JPA

' --- Analíticas ---
SCHED --> DSM
DSM   --> DSW1
DSM   --> DSWN
DSW1  --> PRM
DSWN  --> PRM

' --- Persistencia ---
JPA  <--> H2
SEED  --> JPA

' --- Simulador de buses ---
MBS  --> BS1
MBS  --> BSN
BS1  --> EG
BS1  --> SGPS
BS1  --> SMOT
BS1  --> SPRT
BS1  --> ICECLI
BSN  --> ICECLI
ICECLI --> ICE : ICE TCP :10000

' --- Navegador ---
UI_DASH --> JS
UI_PUB  --> JS
JS --> HTTP : REST GET
JS --> WS   : Conexión WebSocket

' --- Difusión del planificador ---
SCHED --> WS_BROKER : difundir posiciones de buses

@enduml
```

---

## Puertos

| Puerto | Protocolo | Propósito |
|---|---|---|
| 8080 | HTTP / WS | Dashboard, API REST, archivos estáticos |
| 10000 | TCP (ICE) | Recepción de datagramas desde buses |

## Configuración

Copia `.env.example` como `.env` y ajusta los valores antes de correr en producción. Las propiedades activas están en `sitm-mio-system/src/main/resources/application.properties`.

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.x |
| Lenguaje | Java 17 |
| Build | Gradle 8 |
| RPC distribuido | ZeroC ICE 3.7 |
| Base de datos (dev) | H2 en memoria |
| Base de datos (prod) | PostgreSQL (disponible, sin configurar) |
| Tiempo real | WebSocket + STOMP |
| Seguridad | Spring Security (BCrypt) |
| Frontend | HTML5 + JS vanilla + Thymeleaf |
