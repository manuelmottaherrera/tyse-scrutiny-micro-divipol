# language: es
@municipios @regression
Característica: Consultar municipios por departamento
  Como usuario de la API DIVIPOL
  Quiero consultar municipios filtrados por departamento
  Para obtener información de división política municipal

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar municipios de Bolívar exitosamente
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene municipios
    Y cada municipio pertenece al departamento 5
    Y cada municipio tiene los siguientes campos:
      | campo              |
      | coddepto           |
      | codmipio           |
      | nomdepto           |
      | nommipio           |
      | potencialTotal     |
      | potencialFemenino  |
      | potencialMasculino |
      | mesas              |

  @edge-case
  Escenario: Consultar municipios de departamento inexistente
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 municipios

  @edge-case
  Escenario: Consultar municipios con codDepto=0 es válido
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=0"
    Entonces recibo un código de respuesta 200

  @validacion
  Escenario: Consultar municipios con código inválido debe fallar
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=-1"
    Entonces recibo un código de respuesta 400

  @coherencia
  Escenario: Validar que municipios pertenecen al departamento solicitado
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y todos los municipios tienen coddepto igual a 5
    Y todos los municipios tienen nomdepto igual a "BOLIVAR"

  @coherencia
  Escenario: Validar coherencia de datos en municipios
    Cuando consulto el endpoint GET "/api/divipol/municipios?codDepto=5"
    Entonces recibo un código de respuesta 200
    Y cada municipio cumple la regla: potencialTotal = potencialFemenino + potencialMasculino
    Y todos los municipios tienen:
      | campo              | validacion        |
      | codmipio           | mayor o igual a 0 |
      | potencialTotal     | mayor o igual a 0 |
      | mesas              | mayor o igual a 0 |
