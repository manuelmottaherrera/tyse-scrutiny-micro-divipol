# language: es
@comisiones
Característica: CRUD de Comisiones Escrutadoras
  Como usuario de la API
  Quiero gestionar comisiones escrutadoras
  Para administrar las comisiones auxiliares, municipales y distritales

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path @smoke
  Escenario: Crear comisión escrutadora exitosamente
    Cuando creo una comisión escrutadora con los datos:
      | nombre    | Comisión Auxiliar BDD Test |
      | tipo      | AUXILIAR                   |
      | ubicacion | Centro Electoral BDD       |
    Entonces recibo un código de respuesta 201
    Y la comisión creada tiene nombre "Comisión Auxiliar BDD Test"
    Y la comisión creada tiene tipo "AUXILIAR"
    Y la comisión creada está activa

  @happy-path
  Escenario: Obtener comisión por ID
    Dado que existe una comisión escrutadora "Comisión GetById BDD"
    Cuando consulto la comisión por su ID
    Entonces recibo un código de respuesta 200
    Y la comisión tiene nombre "Comisión GetById BDD"

  @happy-path
  Escenario: Listar comisiones paginadas
    Dado que existe una comisión escrutadora "Comisión List BDD"
    Cuando consulto el endpoint GET "/api/comisiones?page=0&size=10"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene una lista paginada de comisiones

  @happy-path
  Escenario: Actualizar comisión
    Dado que existe una comisión escrutadora "Comisión Update BDD"
    Cuando actualizo la comisión con los datos:
      | nombre    | Comisión Actualizada BDD |
      | ubicacion | Nueva Ubicación BDD      |
    Entonces recibo un código de respuesta 200
    Y la comisión creada tiene nombre "Comisión Actualizada BDD"

  @happy-path
  Escenario: Eliminar comisión soft delete
    Dado que existe una comisión escrutadora "Comisión Delete BDD"
    Cuando elimino la comisión
    Entonces recibo un código de respuesta 204
    Y al consultar la comisión eliminada recibo 404

  @happy-path
  Escenario: Crear comisión municipal
    Cuando creo una comisión escrutadora con los datos:
      | nombre    | Comisión Municipal BDD |
      | tipo      | MUNICIPAL              |
      | ubicacion | Alcaldía BDD           |
    Entonces recibo un código de respuesta 201
    Y la comisión creada tiene tipo "MUNICIPAL"

  @happy-path
  Escenario: Crear comisión distrital
    Cuando creo una comisión escrutadora con los datos:
      | nombre    | Comisión Distrital BDD |
      | tipo      | DISTRITAL              |
      | ubicacion | Gobernación BDD        |
    Entonces recibo un código de respuesta 201
    Y la comisión creada tiene tipo "DISTRITAL"

  @edge-case
  Escenario: Comisión inexistente retorna 404
    Cuando consulto la comisión con ID 999999
    Entonces recibo un código de respuesta 404
