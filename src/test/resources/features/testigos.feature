# language: es
@testigos
Característica: CRUD de Testigos Electorales
  Como usuario de la API
  Quiero gestionar testigos electorales
  Para registrar y administrar testigos en puestos de votación

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path @smoke
  Escenario: Crear testigo exitosamente
    Cuando creo un testigo con los datos:
      | tipoDocumento   | CC             |
      | numeroDocumento | BDD-CREATE-001 |
      | nombres         | Juan           |
      | apellidos       | Pérez          |
      | telefono        | 3001234567     |
      | email           | juan@test.com  |
    Entonces recibo un código de respuesta 201
    Y el testigo creado tiene nombre "Juan"
    Y el testigo creado tiene apellido "Pérez"
    Y el testigo creado está activo

  @happy-path
  Escenario: Obtener testigo por ID
    Dado que existe un testigo con documento "BDD-GETID-001"
    Cuando consulto el testigo por su ID
    Entonces recibo un código de respuesta 200
    Y el testigo tiene documento "BDD-GETID-001"

  @happy-path
  Escenario: Listar testigos paginados
    Dado que existe un testigo con documento "BDD-LIST-001"
    Cuando consulto el endpoint GET "/api/testigos?page=0&size=10"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene una lista paginada de testigos

  @happy-path
  Escenario: Actualizar testigo
    Dado que existe un testigo con documento "BDD-UPDATE-001"
    Cuando actualizo el testigo con los datos:
      | nombres   | Juan Carlos |
      | apellidos | Pérez López |
      | telefono  | 3009876543  |
    Entonces recibo un código de respuesta 200
    Y el testigo creado tiene nombre "Juan Carlos"

  @happy-path
  Escenario: Eliminar testigo soft delete
    Dado que existe un testigo con documento "BDD-DELETE-001"
    Cuando elimino el testigo
    Entonces recibo un código de respuesta 204
    Y al consultar el testigo eliminado recibo 404

  @happy-path
  Escenario: Buscar testigos por nombre
    Dado que existe un testigo con documento "BDD-SEARCH-001" y nombre "Xiomara"
    Cuando busco testigos con el término "Xiomara"
    Entonces recibo un código de respuesta 200
    Y la búsqueda contiene resultados

  @edge-case
  Escenario: Documento duplicado retorna 409
    Dado que existe un testigo con documento "BDD-DUP-001"
    Cuando creo un testigo con los datos:
      | tipoDocumento   | CC          |
      | numeroDocumento | BDD-DUP-001 |
      | nombres         | Otro        |
      | apellidos       | Nombre      |
    Entonces recibo un código de respuesta 409

  @edge-case
  Escenario: Búsqueda corta retorna 400
    Cuando busco testigos con el término "a"
    Entonces recibo un código de respuesta 400

  @edge-case
  Escenario: Testigo inexistente retorna 404
    Cuando consulto el testigo con ID 999999
    Entonces recibo un código de respuesta 404

  # =====================================================
  # Escenarios para organizacionId
  # =====================================================

  @happy-path
  Escenario: Crear testigo con organización política
    Dado que existe una organización política "Partido Testigo Create BDD"
    Cuando creo un testigo con organización:
      | tipoDocumento   | CC              |
      | numeroDocumento | BDD-ORG-001     |
      | nombres         | María           |
      | apellidos       | González        |
    Entonces recibo un código de respuesta 201
    Y el testigo creado tiene organización asignada

  @happy-path
  Escenario: Actualizar organización de un testigo
    Dado que existe una organización política "Partido Testigo Update BDD"
    Y que existe un testigo con documento "BDD-UPDORG-001"
    Cuando actualizo el testigo con organización
    Entonces recibo un código de respuesta 200
    Y el testigo creado tiene organización asignada

  @happy-path
  Escenario: Consultar testigo retorna organizacionId
    Dado que existe una organización política "Partido Testigo Consulta BDD"
    Y que existe un testigo con documento "BDD-GETORG-001" y organización
    Cuando consulto el testigo por su ID
    Entonces recibo un código de respuesta 200
    Y el testigo tiene organización asignada
