# language: es
@puestos @regression
Característica: Consultar puestos por zona
  Como usuario de la API DIVIPOL
  Quiero consultar puestos filtrados por zona
  Para obtener información detallada de puestos de votación

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar puestos de una zona exitosamente
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=5&codMpio=1&codZona=1"
    Entonces recibo un código de respuesta 200
    Y cada puesto tiene los siguientes campos:
      | campo     |
      | coddepto  |
      | codmipio  |
      | codzona   |
      | codpuesto |
      | nompuesto |

  @edge-case
  Escenario: Consultar puestos de zona inexistente
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=999&codMpio=999&codZona=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 puestos

  @edge-case
  Escenario: Consultar puestos de Tumaco con códigos alfanuméricos
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=52&codMpio=835&codZona=99"
    Entonces recibo un código de respuesta 200
    Y cada puesto tiene los siguientes campos:
      | campo     |
      | coddepto  |
      | codmipio  |
      | codzona   |
      | codpuesto |
      | nompuesto |

  Esquema del escenario: Validar parámetros de puestos
    Cuando consulto el endpoint GET "/api/divipol/puestos?codDepto=<depto>&codMpio=<mpio>&codZona=<zona>"
    Entonces recibo un código de respuesta <codigo>

    Ejemplos:
      | depto | mpio | zona | codigo |
      | 5     | 1    | 1    | 200    |
      | -1    | 1    | 1    | 400    |
      | 5     | -1   | 1    | 400    |
      | 5     | 1    | -1   | 400    |
