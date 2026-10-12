# Adara · Gestión de turnos

Sistema de escritorio para administrar los turnos de una estética de **uñas y pestañas**.
Lo usa la secretaria del estudio para registrar clientas y profesionales y para dar turnos
sin que se superpongan.

Proyecto final individual de **Programación III**.

---

## Funcionalidades

- **Clientas**: alta, modificación, baja lógica y reactivación. Se puede buscar por nombre.
- **Profesionales**: alta, modificación, baja lógica y reactivación. Cada una pertenece a un rubro: *Pestañas* o *Uñas*.
- **Turnos**:
  - Se dan desde la ficha de cada clienta.
  - Los servicios que se ofrecen dependen del rubro de la profesional elegida.
  - Solo se muestran las **horas libres**, así que dos turnos no pueden superponerse.
  - Un turno se puede modificar, cancelar o marcar como realizado (en ese caso se registra el precio cobrado).
- **Agenda de la profesional**: muestra una fila por cada hora del día (libre u ocupada) y permite moverse de un día a otro.
- **Horario del estudio**: de lunes a viernes, de 9 a 17 h (el último turno es a las 16 h).

## Tecnologías

| | |
|---|---|
| Lenguaje | Java 17 o superior |
| Interfaz | Java Swing |
| Base de datos | SQLite (driver `sqlite-jdbc`, incluido en `lib/`) |
| IDE | Apache NetBeans (proyecto Ant) |

## Requisitos de la materia

| Requisito | Dónde se cumple |
|---|---|
| Al menos 2 tablas relacionadas | `clientas` y `profesionales`, relacionadas con `turnos` mediante claves foráneas |
| Patrón MVC | Paquetes `model`, `view` y `controller`, más la capa de acceso a datos `dao` |
| Al menos 3 vistas | `VentanaPrincipal`, `PanelClientas` y `PanelProfesionales`, más los formularios |
| Herencia | `Clienta` y `Profesional` heredan de `Persona` |
| Abstracción | `Persona` es una clase abstracta y `CrudDAO<T>` es una interfaz |
| Polimorfismo | `getDescripcion()` (método abstracto implementado en cada subclase) y `validar()` sobrescrito en `Profesional` con `super.validar()` |
| Encapsulamiento | Atributos `private` con getters y setters que validan los datos |
| ABM de todos los datos | Clientas, profesionales y turnos (las bajas son lógicas) |
| Patrón Singleton | `ConexionBD`, una única conexión a la base de datos |

## Estructura del proyecto

```
src/esteticaturnos/
├── EsteticaTurnos.java      → clase principal (main)
├── model/                   → Persona, Clienta, Profesional, Turno, Rubro, EstadoTurno, HorarioLaboral
├── dao/                     → ConexionBD (Singleton), EsquemaBD, CrudDAO, ClientaDAO, ProfesionalDAO, TurnoDAO
├── controller/              → ClientaController, ProfesionalController, TurnoController, ValidacionException
└── view/                    → VentanaPrincipal, PanelClientas, PanelProfesionales, formularios y utilidades
```

Flujo de una acción, por ejemplo dar un turno:

```
Vista (FormTurno) → Controller (TurnoController) → DAO (TurnoDAO) → SQLite
```

## Base de datos

```
clientas (1) ───< turnos >─── (1) profesionales
```

- **clientas**: `id`, `nombre`, `telefono`, `observaciones`, `activo`
- **profesionales**: `id`, `nombre`, `telefono`, `rubro`, `activo`
- **turnos**: `id`, `id_clienta`, `id_profesional`, `fecha`, `hora`, `servicio`, `detalle`, `estado`, `precio`
- **Índice único** sobre `(fecha, hora, id_profesional)` para los turnos no cancelados: la base tampoco permite dos turnos a la misma hora con la misma profesional.

El archivo `estetica.db` se crea solo la primera vez que se ejecuta el programa, con algunos datos de prueba.

## Cómo ejecutarlo

1. Clonar o descargar el repositorio (**Code → Download ZIP**).
2. Abrirlo en NetBeans: **File → Open Project** y elegir la carpeta del proyecto.
3. Verificar que las librerías de `lib/` estén agregadas: **Properties → Libraries**.
4. **Clean and Build** y después **Run** (F6).

Opcional: para evitar avisos en la consola, en **Properties → Run → VM Options** agregar:

```
--enable-native-access=ALL-UNNAMED -Dstdout.encoding=UTF-8
```

## Autora

Emilia Pisinatti · Licenciatura en Sistemas de Información
