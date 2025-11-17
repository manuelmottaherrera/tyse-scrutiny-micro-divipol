# language: es
@zonas @regression
Característica: Consultar zonas por municipio
  Como usuario de la API DIVIPOL
  Quiero consultar zonas filtradas por municipio
  Para obtener información de división política por zonas

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar zonas de Cartagena exitosamente
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=5&codMpio=1"
    Entonces recibo un código de respuesta 200
    Y cada zona tiene los siguientes campos:
      | campo    |
      | coddepto |
      | codmipio |
      | codzona  |
      | nomdepto |
      | nommipio |

  @edge-case
  Escenario: Consultar zonas de municipio inexistente
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=999&codMpio=999"
    Entonces recibo un código de respuesta 200
    Y la respuesta contiene 0 zonas

  @edge-case
  Escenario: Consultar zonas con codZona=0 es válido (sin zonificación)
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=5&codMpio=1"
    Entonces recibo un código de respuesta 200

  Esquema del escenario: Consultar zonas con diferentes parámetros
    Cuando consulto el endpoint GET "/api/divipol/zonas?codDepto=<codDepto>&codMpio=<codMpio>"
    Entonces recibo un código de respuesta <codigo>

    Ejemplos:
      | codDepto | codMpio | codigo |
      | 5        | 1       | 200    |
      | 999      | 999     | 200    |
      | -1       | 1       | 400    |
      | 5        | -1      | 400    |
