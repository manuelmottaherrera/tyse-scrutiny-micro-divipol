# language: es
@reclamaciones
Característica: Gestión de Reclamaciones Electorales
  Como usuario de la API
  Quiero gestionar reclamaciones electorales
  Para registrar y resolver irregularidades en el proceso electoral

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Listar reclamaciones paginadas
    Cuando consulto el endpoint GET "/api/reclamaciones?page=0&size=10"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene una lista paginada de reclamaciones

  @happy-path
  Escenario: Filtrar reclamaciones por estado
    Cuando consulto el endpoint GET "/api/reclamaciones?estado=PRESENTADA&page=0&size=10"
    Entonces recibo un código de respuesta 200

  @happy-path
  Escenario: Filtrar reclamaciones por tipo
    Cuando consulto el endpoint GET "/api/reclamaciones?tipo=ERROR_ARITMETICO&page=0&size=10"
    Entonces recibo un código de respuesta 200

  @edge-case
  Escenario: Reclamación inexistente retorna 404
    Cuando consulto la reclamación con ID 999999
    Entonces recibo un código de respuesta 404

  @edge-case
  Escenario: Crear reclamación sin testigo retorna 404
    Cuando creo una reclamación con testigo inexistente
    Entonces recibo un código de respuesta 404

  @edge-case
  Escenario: Crear reclamación con descripción corta retorna 400
    Dado que existe un testigo con documento "BDD-RECLAM-001"
    Cuando creo una reclamación con descripción corta
    Entonces recibo un código de respuesta 400

  @edge-case
  Escenario: Resolver reclamación inexistente retorna 404
    Cuando resuelvo la reclamación con ID 999999
    Entonces recibo un código de respuesta 404

  @happy-path
  Escenario: Consultar reclamaciones por testigo
    Dado que existe un testigo con documento "BDD-RECLAM-TST"
    Cuando consulto reclamaciones del testigo
    Entonces recibo un código de respuesta 200

  @happy-path
  Escenario: Consultar reclamaciones por mesa
    Cuando consulto el endpoint GET "/api/reclamaciones/mesa/1"
    Entonces recibo un código de respuesta 200

  @happy-path
  Escenario: Consultar reclamaciones por comisión
    Cuando consulto el endpoint GET "/api/reclamaciones/comision/1"
    Entonces recibo un código de respuesta 200
