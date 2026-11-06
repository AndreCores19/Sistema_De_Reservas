# Sistema de Reserva de Recursos

Aplicación de escritorio desarrollada en JavaFX para administrar usuarios, funcionarios, categorías, recursos y reservas de una organización. El sistema permite asignar recursos automáticamente, consultar calendarizaciones, generar estadísticas y exportar información a PDF.

### Integrantes:
- Andrea Cordero Espinoza
- Emily Benavides Rodríguez
- Jose Pablo Sánchez Ramírez


## Requisitos

- JDK 21.
- Maven 3.9 o posterior.
- Sistema operativo con JavaFX compatible.
- Conexión a Internet y una clave de Gemini únicamente para la extracción de reservas mediante lenguaje natural.

## Ejecución

Desde la raíz del proyecto:

```powershell
mvn clean
mvn compile
mvn javafx:run
```

El `javafx-maven-plugin` usa `app.App` como clase principal. También existen `app.Launcher`, que delega en `App.main`, y dos utilidades manuales: `app.QuemadosJson` para crear datos iniciales y `app.PruebaPDF` para probar generación de reportes.

La aplicación usa rutas relativas como `Data/reservas.json`; por eso debe iniciarse con la raíz del proyecto como directorio de trabajo.

Para la función de IA, definir la variable de entorno GEMINI_API_KEY antes de iniciar el servidor.


La clave no está incluida en el repositorio.

## Arquitectura

El proyecto aplica una arquitectura por capas y el patrón MVC en la interfaz JavaFX:

```text
FXML (vista)
  -> app.ui (controladores MVC)
  -> app.Servicios (fachadas de aplicación)
  -> app.Logica (reglas de negocio)
  -> app.Datos (persistencia Gson)
  -> Data/*.json
```

La separación no es completamente estricta: algunos controladores crean servicios directamente y algunas clases de lógica crean objetos de datos o de otras capas. La sesión activa se mantiene en `app.Controllers.SesionActual`, mediante un `UsuarioDTO` estático.

### Paquetes principales

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

Objetos de transferencia utilizados entre capas:

`UsuarioDTO`, `AdministradorDTO`, `FuncionarioDTO`, `CategoriaDTO`, `RecursoDTO`, `ReservaDTO`, `ResultadoDeAsignacionDTO`, `MatrizDTO`, `CeldaActividadDTO`, `ExtraccionIADTO` y `ResultadoEstadisticaDTO`.

#### `app.Servicios`

Fachadas utilizadas por la interfaz: `ServicioUsuario`, `ServicioFuncionario`, `ServicioCategoria`, `ServicioRecurso`, `ServicioReservas`, `ServicioCalendarizacionRecursos`, `ServicioProgramacion`, `ServicioEstadisticas`, `ServicioGemini`, `ServicioGeneradorGrafico`, `ServicioPDF` y `ServicioImpresion`.

#### `app.Logica`

Contiene las reglas de autenticación, CRUD, asignación y reportes:

- `UsuarioLogica`, `FuncionarioLogica`, `CategoriaLogica` y `RecursoLogica`.
- `LogicaReservas`, que valida traslapes y selecciona el primer recurso libre de cada categoría.
- `CalendarizacionRecursosLogica` y `ReporteLogica`.
- `GeneradorReportePDFLogica`, basado en iText.
- `GeneradorGraficoLogica`, basado en JFreeChart.
- `LogicaGemini`, cliente HTTP de la API de Gemini.

#### `app.Datos`

`UsuarioDatos`, `AdministradorDatos`, `FuncionarioDatos`, `CategoriaDatos`, `RecursoDatos` y `ReservaDatos` leen y escriben listas de DTO con Gson. `ReservaDatos` incluye adaptadores para `LocalDate` y `LocalTime`.

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

## Funcionalidades

### Login y sesión

El usuario ingresa su ID y clave. La aplicación valida campos, consulta `UsuarioLogica`, registra el usuario en `SesionActual` y carga la interfaz correspondiente. También permite cerrar sesión y cambiar la clave.

### Reservas

Un funcionario puede:

- Seleccionar una o más categorías, actividad, fecha, hora inicial y hora final.
- Consultar sus reservas.
- Cancelar reservas futuras.
- Imprimir sus reservas.
- Escribir una descripción en lenguaje natural y solicitar que Gemini complete el formulario.

La lógica valida fechas no anteriores al día actual, horarios pasados para reservas del día actual y que la hora final sea posterior a la inicial. Para cada categoría seleccionada intenta asignar el primer recurso libre. El traslape se detecta con la condición:

```java
horaInicioA.isBefore(horaFinB)
    && horaInicioB.isBefore(horaFinA)
```

Las reservas canceladas no se consideran ocupación. La extracción de IA espera actividad, fecha (`YYYY-MM-DD`), horas (`HH:mm`) y nombres de categorías, y solo selecciona categorías que existan en el sistema.

### Mantenimientos administrativos

- **Funcionarios:** búsqueda por ID o nombre, inclusión, consulta, modificación y eliminación. La clave inicial de un funcionario es igual a su ID. No se permite eliminar un funcionario con reservas activas.
- **Categorías:** búsqueda, CRUD y generación de IDs con formato `CAT-000001`. No se permite eliminar una categoría asociada a recursos.
- **Recursos:** búsqueda por activo o descripción, filtrado por categoría y CRUD. No se permite eliminar un recurso utilizado por reservas activas.

### Calendarización

- **Recursos:** para una fecha y categoría muestra una matriz donde las filas son bloques horarios y las columnas son recursos; las celdas indican la actividad y el funcionario cuando existe una reserva.
- **Actividades:** para una semana muestra una matriz con horas en las filas y días de la semana en las columnas, incluyendo actividad y funcionario responsable.

Ambas vistas tienen opciones de impresión.

### Estadísticas y reportes

`ServicioEstadisticas` calcula:

1. Cantidad de recursos/categorías reservados en un rango de fechas.
2. Cantidad de actividades agrupadas por la semana de cada lunes.

`GeneradorGraficoLogica` crea gráficos de barras con JFreeChart. `ReporteLogica`, `GeneradorReportePDFLogica`, `ServicioPDF` y `ServicioImpresion` generan o abren reportes PDF para reservas, funcionarios, categorías, recursos, matrices, programación y estadísticas.

## Persistencia

La aplicación utiliza archivos JSON como almacenamiento local. Los archivos administrados bajo `Data/` son:

```text
Data/administradores.json
Data/funcionarios.json
Data/categorias.json
Data/recursos.json
Data/reservas.json
```

Gson serializa los DTO con formato legible. Las fechas y horas de `ReservaDTO` se convierten mediante adaptadores de `ReservaDatos`.

El directorio `Data/` contiene los datos iniciales del sistema y los reportes PDF generados por la aplicación.

## Dependencias principales

Definidas en `pom.xml`:

- JavaFX Controls, FXML y Swing `21.0.2`.
- Gson `2.11.0`.
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

Las pruebas de integración usan copias de los datos reales en `src/test/resources/` (`*_prueba.json`) para no depender ni modificar los archivos de `Data/`. Las de escritura además usan `@TempDir` para no compartir estado entre ellas.

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
| `GeneradorGraficoLogicaTest` | Unidad | Validación de argumentos del generador de gráficos |
| `GeneradorReportePDFLogicaTest` | Unidad | Validación de argumentos y generación real de PDF (contenido, filas vacías, ruta inválida) |
| `LogicaReservasHayConflictoHorasTest` | Unidad | Detección de traslape de horarios (solapado, contenido, límites exactos) |


## Estructura resumida

```text
Proyecto_1/
├── Data/                         # Persistencia JSON y PDFs generados
├── src/main/java/app/
│   ├── Controllers/              # Sesión global
│   ├── DTO/                      # Objetos de transferencia
│   ├── Datos/                    # Acceso a JSON
│   ├── Logica/                   # Reglas de negocio
│   ├── Servicios/                # Fachadas de aplicación
│   └── ui/                      # Controladores JavaFX
├── src/main/resources/app/ui/    # FXML e imágenes
├── pom.xml
└── README.md
```