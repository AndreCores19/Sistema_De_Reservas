# Sistema de Reserva de Recursos

Aplicación de escritorio desarrollada en JavaFX para administrar usuarios, funcionarios, categorías, recursos y reservas de una organización. El sistema permite asignar recursos automáticamente, consultar calendarizaciones, generar estadísticas, exportar información a PDF y recibir notificaciones de reservas nuevas.

Está dividida en un **frontend** (interfaz JavaFX) y un **backend** (servidor con la lógica y el acceso a MySQL) que se comunican por la red.

### Integrantes:
- Andrea Cordero Espinoza
- Emily Benavides Rodríguez
- Jose Pablo Sánchez Ramírez


## Requisitos

- JDK 21 y Maven 3.9 o posterior (el de IntelliJ sirve).
- MySQL 8 con el script `Database/baseDatos.sql` ejecutado.
- Una clave de Gemini, solo en la máquina del servidor, para la extracción de reservas con lenguaje natural.

## Ejecución

El sistema son dos programas que se ejecutan por separado y pueden estar en computadoras distintas. Los dos deben iniciarse con la raíz del proyecto como directorio de trabajo.

### 1. Base de datos

Ejecutar `Database/baseDatos.sql` en MySQL con un usuario administrador. El script **borra y recrea** la base `sistema_reservas`, carga datos de prueba y crea el usuario `reservas_app` que usa el servidor.

### 2. Servidor (backend)

Ejecutar la clase `app.ServidorApp`. Levanta:

- el servidor HTTP en el puerto **8080**;
- el servidor de notificaciones por socket en el puerto **9090**.

Para la función de IA hay que definir antes la variable de entorno `GEMINI_API_KEY` en la máquina del servidor (en IntelliJ: *Run → Edit Configurations → Environment variables*). La clave no está incluida en el repositorio y la interfaz no la necesita, porque es el servidor quien consulta a Gemini.

### 3. Interfaz (frontend)

Con el servidor ya iniciado:

```powershell
mvn javafx:run
```

El `javafx-maven-plugin` usa `app.App` como clase principal. También existe `app.Launcher`, que delega en `App.main`.

Credenciales de prueba: `admin` / `hola123` (administrador) y `111` / `111` (funcionario).

### Servidor en otra computadora

Por defecto la interfaz se conecta a `localhost`. Para conectarse a un servidor que está en otra máquina:

1. Copiar `servidor.properties.example` como `servidor.properties` en la raíz del proyecto.
2. Escribir en él la IP del servidor (`host=192.168.1.25`). La IP se obtiene con `ipconfig` en la computadora del servidor.
3. En la computadora del servidor, permitir conexiones entrantes a los puertos 8080 y 9090 en el firewall de Windows.
4. Reiniciar la interfaz.

`servidor.properties` no se sube al repositorio, porque cada computadora tiene su propia IP. Si el archivo no existe, se usa `localhost`; también se puede indicar el servidor con la variable de entorno `SERVIDOR_HOST`, pero el archivo tiene prioridad. Para volver a ejecutar todo en una sola computadora, basta con borrar el archivo o dejar `host=localhost`.

## Arquitectura

La aplicación se divide en un frontend y un backend que se comunican por la red. Solo el backend accede a la base de datos.

```text
FRONTEND (JavaFX)                          BACKEND (servidor)

FXML -> app.ui (controladores MVC)
          -> app.Servicios.*Cliente   --HTTP-->   app.Servidor (handlers)
          <- notificaciones <--socket--           -> app.Servicios (fachadas)
                                                  -> app.Logica
                                                  -> app.Datos (JDBC)
                                                  -> MySQL
```

#### Reglas de capas

- Los controladores solo importan DTOs y las clases `...Cliente`. Nunca `Logica`, `Datos` ni `ConexionBD`.
- Frontend y backend no se llaman directamente: todo pasa por el servidor.
- Los DTOs de `app.DTO` se comparten entre ambos lados y viajan como parámetros de formulario o como JSON (Gson).
- El servidor no confía en el cliente: genera por su cuenta los ids, el estado y los recursos asignados de cada reserva, y valida todos los datos que recibe.

### Paquetes principales

- `app.ui`: controladores de las vistas (frontend).
- `app.ui.utilidades`: generación de gráficos, reportes PDF e impresión. Solo presentación: trabajan sobre DTOs que el frontend ya recibió del servidor.
- `app.Servicios`: contiene dos tipos de clases. Las `...Cliente` (frontend) hacen las peticiones HTTP a partir de `ClienteBase`. Las fachadas `Servicio...` (backend) son las que llaman a la Lógica.
- `app.Servidor`: `ServidorHTTP`, un handler por entidad, `HttpUtil` y `ServidorNotificaciones`.
- `app.Logica`: reglas de negocio.
- `app.Datos`: acceso a MySQL con JDBC.
- `app.DTO`: objetos de transferencia y `GsonUtil` (adaptadores de `LocalDate` y `LocalTime` para JSON).
- `app.Controllers`: `SesionActual`, que guarda el usuario con la sesión activa en el frontend.

#### `app.ui`

Controladores de las vistas:

- `LoginViewController`: autenticación y carga de la interfaz según el rol.
- `CambiarClaveViewController`: cambio de clave del usuario autenticado.
- `ReservasViewController`: creación, consulta, cancelación y extracción con IA.
- `FuncionarioViewController`: mantenimiento administrativo de funcionarios.
- `CategoriaViewController`: mantenimiento administrativo de categorías.
- `RecursoViewController`: mantenimiento administrativo de recursos.
- `CalendarizacionRecursosViewController`: matriz diaria de recursos.
- `CalendarizacionActividadesViewController`: matriz semanal de actividades.
- `EstadisticasViewController`: estadísticas y gráficos.
- `FilaCalendario`: modelo auxiliar para filas de tablas de calendarización.

#### `app.DTO`

Objetos de transferencia utilizados entre el frontend y el backend:

`UsuarioDTO`, `AdministradorDTO`, `FuncionarioDTO`, `CategoriaDTO`, `RecursoDTO`, `ReservaDTO`, `AlertaReservaDTO`, `ResultadoDeAsignacionDTO`, `MatrizDTO`, `CeldaActividadDTO`, `ExtraccionIADTO` y `ResultadoEstadisticaDTO`.

#### `app.Servicios`

- **Clientes (frontend):** `ClienteBase` (envío de peticiones y manejo de errores), `ConfigCliente` (dirección del servidor), `ServicioUsuarioCliente`, `ServicioFuncionarioCliente`, `ServicioCategoriaCliente`, `ServicioRecursoCliente`, `ServicioReservasCliente`, `ServicioCalendarizacionRecursosCliente`, `ServicioProgramacionCliente`, `ServicioEstadisticasCliente`, `ServicioGeminiCliente` y `ServicioNotificacionesCliente` (conexión al socket de notificaciones).
- **Fachadas (backend):** `ServicioUsuario`, `ServicioFuncionario`, `ServicioCategoria`, `ServicioRecurso`, `ServicioReservas`, `ServicioCalendarizacionRecursos`, `ServicioProgramacion`, `ServicioEstadisticas` y `ServicioGemini`.

#### `app.Servidor`

- `ServidorHTTP`: crea el `HttpServer`, registra una ruta por entidad y define el grupo de hilos que atiende las peticiones.
- Un handler por entidad: `UsuarioHandler`, `ReservasHandler`, `CategoriaHandler`, `RecursoHandler`, `FuncionarioHandler`, `CalendarizacionHandler`, `EstadisticasHandler` y `GeminiHandler`.
- `HttpUtil`: lectura de parámetros, respuestas JSON y respuestas de error.
- `ServidorNotificaciones`: servidor de sockets que avisa de las reservas nuevas.

#### `app.Logica`

Contiene las reglas de autenticación, CRUD y asignación:

- `UsuarioLogica`, `FuncionarioLogica`, `CategoriaLogica` y `RecursoLogica`.
- `LogicaReservas`, que valida traslapes y selecciona el primer recurso libre de cada categoría. Las operaciones que modifican reservas se ejecutan bajo un candado para que dos peticiones simultáneas no tomen el mismo recurso.
- `CalendarizacionRecursosLogica`.
- `LogicaGemini`, cliente HTTP de la API de Gemini (se ejecuta en el servidor).

Las clases de reportes y gráficos (`ReporteLogica`, `GeneradorReportePDFLogica`, `GeneradorGraficoLogica`) pertenecen al frontend y se encuentran en `app.ui.utilidades`.

#### `app.Datos`

`ConexionBD`, `UsuarioDatos`, `FuncionarioDatos`, `CategoriaDatos`, `RecursoDatos` y `ReservaDatos` leen y escriben en MySQL con JDBC. `ReservaDatos` guarda una reserva y sus listas de categorías y recursos en una sola transacción.

## Comunicación entre cliente y servidor

### Rutas HTTP (puerto 8080)

Todas son `POST`. Los datos simples van como parámetros de formulario (`nombre=valor&...`) y los DTOs complejos como JSON. Las respuestas son JSON. Los errores devuelven `{"error": "mensaje"}` con código 400 (datos inválidos), 401 (credenciales incorrectas), 404 (ruta inexistente), 405 (método no permitido) o 500 (error interno).

| Ruta | Recibe | Responde |
|---|---|---|
| `/usuario/login` | `id`, `clave` | Usuario (sin la clave) |
| `/usuario/cambiarClave` | `id`, `claveActual`, `claveNueva` | Mensaje |
| `/reserva/crear` | `ReservaDTO` en JSON | Mensaje |
| `/reserva/listarPorFuncionario` | `idFuncionario` | Lista de reservas |
| `/reserva/cancelar` | `idReserva` | Mensaje |
| `/estadistica/recursos` | `desde`, `hasta` (aaaa-mm-dd) | Lista de resultados |
| `/estadistica/actividades` | `desde`, `hasta` (aaaa-mm-dd) | Lista de resultados |
| `/calendarizacion/recursos` | `idCategoria`, `fecha` | Matriz de recursos |
| `/calendarizacion/actividades` | `fecha` | Matriz de actividades |
| `/categorias/listar`, `buscarPorId`, `buscarPorDescripcion`, `agregar`, `modificar`, `eliminar` | `id`, `texto`, `descripcion` según la ruta | Categoría(s) o mensaje |
| `/recursos/listar`, `filtrarPorCategoria`, `buscarPorNumActivo`, `agregar`, `modificar`, `eliminar` | `numActivo`, `idCategoria`, `descripcion` según la ruta | Recurso(s) o mensaje |
| `/funcionarios/listar`, `buscarPorId`, `buscarPorNombre`, `agregar`, `modificar`, `eliminar` | `id`, `texto`, `nombre`, `telefono` según la ruta | Funcionario(s) sin clave o mensaje |
| `/gemini/extraer` | `frase` | Datos extraídos (actividad, fecha, horas y categorías sugeridas) |

Las claves de los usuarios nunca se devuelven al cliente.

### Notificaciones de reservas nuevas (puerto 9090)

Cuando se crea una reserva, el servidor se la envía a los demás funcionarios conectados, que la reciben sin tener que consultar al servidor.

1. Al iniciar sesión, el cliente abre un socket al puerto 9090.
2. Envía una primera línea con el id del funcionario.
3. El servidor guarda la conexión. Cada vez que se crea una reserva, envía una línea con el `ReservaDTO` en JSON a todos los clientes conectados, **excepto** al funcionario que la creó.
4. El cliente lee las líneas en un hilo de fondo y las entrega al panel de Alertas (ver la sección *Alertas*).
5. Al cerrar sesión, el cliente cierra el socket y el servidor lo quita de su lista.

Si la notificación falla, la reserva ya queda guardada: el aviso es independiente de su creación.

## Vistas y navegación

Las vistas se encuentran en `src/main/resources/app/ui/`:

| FXML | Controlador | Uso |
|---|---|---|
| `login-view.fxml` | `LoginViewController` | Inicio de sesión |
| `cambiarClave-view.fxml` | `CambiarClaveViewController` | Cambio de clave |
| `reservas-view.fxml` | `ReservasViewController` | Reservas del funcionario |
| `funcionariosAdmin-view.fxml` | `FuncionarioViewController` | CRUD de funcionarios |
| `categoriaAdmin-view.fxml` | `CategoriaViewController` | CRUD de categorías |
| `recursosAdmin-view.fxml` | `RecursoViewController` | CRUD de recursos |
| `calendarizacionRecursos-view.fxml` | `CalendarizacionRecursosViewController` | Disponibilidad diaria |
| `calendarizacionActividades-view.fxml` | `CalendarizacionActividadesViewController` | Actividades por semana |
| `estadisticas-view.fxml` | `EstadisticasViewController` | Estadísticas y gráficos |

`SistemaDeReservasFuncionario.fxml` y `SistemaDeReservasFuncionarioAdmin.fxml` son contenedores `TabPane` que incorporan las vistas mediante `fx:include`. Después del login, `LoginViewController` carga el contenedor de funcionario o administrador según `UsuarioDTO.getRol()`. Por ello, los mantenimientos administrativos no aparecen en la interfaz de un funcionario.

Ambos contenedores muestran `alertas-view.fxml` a la derecha de las pestañas. `AlertasViewController.recibirAlerta` y `recibirReserva` son los puntos de entrada seguros para el hilo de notificaciones del cliente: internamente usan `Platform.runLater`. Las alertas se mantienen en una lista observable, se pueden consultar en una ventana emergente y descartarse únicamente de forma local.

`SistemaDeReservasViewController` es el controlador de ambos contenedores principales. Su método `configurarNotificaciones(NotificacionesCliente)` registra el listener de reservas, inicia el cliente de notificaciones y entrega cada reserva a `AlertasViewController`.

## Funcionalidades

### Login y sesión

El usuario ingresa su ID y clave. La interfaz los envía al servidor (`/usuario/login`), que los valida contra la base de datos. Si son correctos, se registra el usuario en `SesionActual` y se carga la interfaz correspondiente. También permite cerrar sesión y cambiar la clave.

### Reservas

Un funcionario puede:

- Seleccionar una o más categorías, actividad, fecha, hora inicial y hora final.
- Consultar sus reservas.
- Cancelar reservas futuras.
- Imprimir sus reservas.
- Escribir una descripción en lenguaje natural y solicitar que Gemini complete el formulario.

El servidor valida que la actividad no esté vacía ni exceda el largo máximo, que el funcionario exista, que la fecha y la hora de inicio sean futuras y que la hora final sea posterior a la inicial. Para cada categoría seleccionada intenta asignar el primer recurso libre; si alguna no tiene recursos disponibles en ese horario, rechaza la reserva e indica cuáles. El traslape se detecta con la condición:

``` java
horaInicioA.isBefore(horaFinB)
    && horaInicioB.isBefore(horaFinA)
```

Las reservas canceladas no se consideran ocupación. La extracción de IA espera actividad, fecha (`YYYY-MM-DD`), horas (`HH:mm`) y nombres de categorías, y solo selecciona categorías que existan en el sistema.

### Alertas

# AQUÍ VA LO DE JP
(panel de Alertas: cómo se muestran las reservas nuevas, Ver y Descartar, y cómo se conecta el hilo receptor al socket de notificaciones).

### Mantenimientos administrativos

- **Funcionarios:** búsqueda por ID o nombre, inclusión, consulta, modificación y eliminación. La clave inicial de un funcionario es igual a su ID. No se permite eliminar un funcionario con reservas activas.
- **Categorías:** búsqueda, CRUD y generación de IDs con formato `CAT-000001`. No se permite eliminar una categoría asociada a recursos.
- **Recursos:** búsqueda por activo o descripción, filtrado por categoría y CRUD. No se permite eliminar un recurso utilizado por reservas activas.

### Calendarización

- **Recursos:** para una fecha y categoría muestra una matriz donde las filas son bloques horarios y las columnas son recursos; las celdas indican la actividad y el funcionario cuando existe una reserva.
- **Actividades:** para una semana muestra una matriz con horas en las filas y días de la semana en las columnas, incluyendo actividad y funcionario responsable.

Ambas vistas tienen opciones de impresión.

### Estadísticas y reportes

El servidor calcula (`ServicioEstadisticas`):

1. Cantidad de recursos/categorías reservados en un rango de fechas.
2. Cantidad de actividades agrupadas por la semana de cada lunes.

El frontend recibe los resultados ya calculados. `GeneradorGraficoLogica` crea los gráficos de barras con JFreeChart, y `ReporteLogica`, `GeneradorReportePDFLogica`, `ServicioPDF` y `ServicioImpresion` generan o abren reportes PDF para reservas, funcionarios, categorías, recursos, matrices, programación y estadísticas.

## Persistencia

Los datos se guardan en MySQL (base `sistema_reservas`) y solo el backend accede a ella, mediante JDBC en `app.Datos`. El script `Database/baseDatos.sql` crea el esquema y los datos de prueba.

| Tabla | Contenido |
|---|---|
| `usuario` | Administradores y funcionarios (se distinguen por el campo `rol`) |
| `categoria` | Categorías de recursos |
| `recurso` | Recursos, con su categoría |
| `reserva` | Reservas: actividad, fecha, horas, funcionario y estado (`ACTIVA` o `CANCELADA`) |
| `reserva_categoria` | Categorías solicitadas en cada reserva |
| `reserva_recurso` | Recursos asignados a cada reserva |

Las claves de los usuarios se guardan en texto plano porque es un proyecto académico; en un sistema real deberían almacenarse con un hash seguro. Por la misma razón, la comunicación usa HTTP sin cifrar; en un sistema real se usaría HTTPS.

## Dependencias principales

Definidas en `pom.xml`:

- JavaFX Controls, FXML y Swing `21.0.2`.
- Gson `2.11.0`.
- MySQL Connector/J `8.4.0`.
- iText `8.0.5`.
- JFreeChart `1.5.4`.
- JSON-java `20260814`.
- JUnit Jupiter `5.11.4` como dependencia de pruebas.
- Maven Surefire `3.5.2` y Failsafe `3.5.2`.

## Pruebas

El proyecto incorpora JUnit Jupiter, con Surefire para pruebas de unidad (`*Test`) y Failsafe para pruebas de integración (`*IT`). Los comandos de ejecución son:

```powershell
mvn test
mvn verify
```

Las pruebas de integración usan la base de datos MySQL, por lo que necesitan que el servidor de MySQL esté en ejecución con el script `Database/baseDatos.sql` aplicado. Para no mezclarse con los datos reales, crean un escenario propio (`DatosDePrueba`) con identificadores reconocibles y fechas del año 2099, y lo borran al terminar.

### Cobertura actual

| Clase de prueba | Tipo | Cubre |
|---|---|---|
| `UsuarioLogicaIT` | Integración | Login y cambio de clave (administrador y funcionario) |
| `CategoriaLogicaIT` | Integración | CRUD de categorías |
| `FuncionarioLogicaIT` | Integración | CRUD de funcionarios y asignación de clave/rol por defecto |
| `RecursoLogicaIT` | Integración | Listado, filtrado por categoría, búsqueda y modificación de recursos |
| `LogicaReservasIT` | Integración | Reservas activas por funcionario, listado por funcionario y cancelación de reservas |
| `ServicioProgramacionIT` | Integración | Matriz semanal de actividades |
| `ServicioEstadisticasIT` | Integración | Estadísticas de recursos y de actividades |
| `CalendarizacionRecursosLogicaIT` | Integración | Matriz diaria de disponibilidad de recursos |
| `CategoriaDatosIT`, `RecursoDatosIT`, `ReservaDatosIT`, `ConexionBDIT` | Integración | Acceso a MySQL de cada entidad y de la conexión |
| `GeneradorGraficoLogicaTest` | Unidad | Validación de argumentos del generador de gráficos |
| `GeneradorReportePDFLogicaTest` | Unidad | Validación de argumentos y generación real de PDF (contenido, filas vacías, ruta inválida) |
| `LogicaReservasHayConflictoHorasTest` | Unidad | Detección de traslape de horarios (solapado, contenido, límites exactos) |

## Estructura resumida

```text
Sistema_De_Reservas/
├── Database/
│   └── baseDatos.sql               # Esquema y datos de prueba de MySQL
├── src/main/java/app/
│   ├── App.java, Launcher.java     # Inicio de la interfaz (frontend)
│   ├── ServidorApp.java            # Inicio del servidor (backend)
│   ├── Controllers/                # Sesión activa
│   ├── DTO/                        # Objetos de transferencia y GsonUtil
│   ├── Datos/                      # Acceso a MySQL (JDBC)
│   ├── Logica/                     # Reglas de negocio
│   ├── Servicios/                  # Clientes (frontend) y fachadas (backend)
│   ├── Servidor/                   # Servidor HTTP, handlers y notificaciones
│   └── ui/                         # Controladores JavaFX
│       └── utilidades/             # Gráficos, PDF e impresión
├── src/main/resources/app/ui/      # FXML e imágenes
├── src/test/                       # Pruebas de unidad e integración
├── servidor.properties.example     # Plantilla para indicar la IP del servidor
├── pom.xml
└── README.md
```