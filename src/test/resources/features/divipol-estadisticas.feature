# language: es
@estadisticas @critical
Característica: Consultar estadísticas de DIVIPOL
  Como usuario de la API DIVIPOL
  Quiero consultar estadísticas agregadas
  Para obtener análisis de división política

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path @smoke
  Escenario: Consultar estadísticas generales
    Dado que la base de datos tiene 18016 registros
    Cuando consulto el endpoint GET "/api/divipol/stats"
    Entonces recibo un código de respuesta 200
    Y las estadísticas contienen:
      | campo              | validacion       |
      | totalDepartamentos | mayor a 30       |
      | totalMunicipios    | mayor a 1000     |
      | totalZonas         | mayor a 0        |
      | totalPuestos       | mayor a 0        |
      | potencialTotal     | mayor a 30000000 |
      | potencialFemenino  | mayor a 15000000 |
      | potencialMasculino | mayor a 15000000 |
      | totalMesas         | mayor a 0        |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de un departamento
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/5"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | > 0   |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de un municipio
    Cuando consulto el endpoint GET "/api/divipol/stats/municipio/5/1"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | 1     |
      | totalZonas         | >= 0  |
    Y el potencial total es la suma de femenino y masculino

  @happy-path
  Escenario: Consultar estadísticas de una zona
    Cuando consulto el endpoint GET "/api/divipol/stats/zona/5/1/1"
    Entonces recibo un código de respuesta 200
    Y las estadísticas muestran:
      | campo              | valor |
      | totalDepartamentos | 1     |
      | totalMunicipios    | 1     |
      | totalZonas         | 1     |

  @edge-case
  Escenario: Estadísticas de departamento inexistente
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/999"
    Entonces recibo un código de respuesta 200

  @validacion
  Escenario: Estadísticas con código inválido
    Cuando consulto el endpoint GET "/api/divipol/stats/departamento/-1"
    Entonces recibo un código de respuesta 400
