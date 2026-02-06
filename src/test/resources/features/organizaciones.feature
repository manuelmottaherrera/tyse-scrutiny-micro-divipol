# language: es
@organizaciones
Característica: CRUD de Organizaciones Políticas
  Como usuario de la API
  Quiero gestionar organizaciones políticas
  Para administrar partidos, movimientos y coaliciones electorales

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path @smoke
  Escenario: Crear organización política exitosamente
    Cuando creo una organización política con los datos:
      | nombre | Partido BDD Test     |
      | sigla  | PBDD                 |
      | tipo   | PARTIDO              |
    Entonces recibo un código de respuesta 201
    Y la organización creada tiene nombre "Partido BDD Test"
    Y la organización creada tiene sigla "PBDD"
    Y la organización creada está activa

  @happy-path
  Escenario: Obtener organización por ID
    Dado que existe una organización política "Partido GetById BDD"
    Cuando consulto la organización por su ID
    Entonces recibo un código de respuesta 200
    Y la organización tiene nombre "Partido GetById BDD"

  @happy-path
  Escenario: Listar organizaciones paginadas
    Dado que existe una organización política "Partido List BDD"
    Cuando consulto el endpoint GET "/api/organizaciones?page=0&size=10"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene una lista paginada de organizaciones

  @happy-path
  Escenario: Actualizar organización
    Dado que existe una organización política "Partido Update BDD"
    Cuando actualizo la organización con los datos:
      | nombre | Partido Actualizado BDD |
      | sigla  | PABDD                   |
    Entonces recibo un código de respuesta 200
    Y la organización creada tiene nombre "Partido Actualizado BDD"

  @happy-path
  Escenario: Eliminar organización soft delete
    Dado que existe una organización política "Partido Delete BDD"
    Cuando elimino la organización
    Entonces recibo un código de respuesta 204
    Y al consultar la organización eliminada recibo 404

  @happy-path
  Escenario: Crear movimiento político
    Cuando creo una organización política con los datos:
      | nombre | Movimiento BDD Test |
      | sigla  | MBDD                |
      | tipo   | MOVIMIENTO          |
    Entonces recibo un código de respuesta 201
    Y la organización creada tiene tipo "MOVIMIENTO"

  @happy-path
  Escenario: Crear coalición
    Cuando creo una organización política con los datos:
      | nombre | Coalición BDD Test |
      | sigla  | CBDD               |
      | tipo   | COALICION          |
    Entonces recibo un código de respuesta 201
    Y la organización creada tiene tipo "COALICION"

  @edge-case
  Escenario: Nombre duplicado retorna 409
    Dado que existe una organización política "Partido Duplicado BDD"
    Cuando creo una organización política con los datos:
      | nombre | Partido Duplicado BDD |
      | sigla  | PDUP                  |
      | tipo   | PARTIDO               |
    Entonces recibo un código de respuesta 409

  @edge-case
  Escenario: Organización inexistente retorna 404
    Cuando consulto la organización con ID 999999
    Entonces recibo un código de respuesta 404
