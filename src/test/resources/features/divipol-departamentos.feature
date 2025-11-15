# language: es
@departamentos @smoke @critical
Característica: Consultar departamentos de Colombia
  Como usuario de la API DIVIPOL
  Quiero consultar la lista de departamentos de Colombia
  Para obtener información de división política a nivel departamental

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  @happy-path
  Escenario: Consultar todos los departamentos exitosamente
    Cuando consulto el endpoint GET "/api/divipol/departamentos"
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "application/json"
    Y la respuesta contiene al menos 32 departamentos
    Y cada departamento tiene los siguientes campos obligatorios:
      | campo          |
      | coddepto       |
      | nomdepto       |
      | totalPotencial |
      | mesas          |
      | mujeres        |
      | hombres        |

  @seguridad
  Escenario: Consultar departamentos sin autenticación debe fallar
    Dado que NO estoy autenticado
    Cuando consulto el endpoint GET "/api/divipol/departamentos"
    Entonces recibo un código de respuesta 401

  @validacion-datos @coherencia
  Escenario: Verificar coherencia de datos en departamentos
    Cuando consulto el endpoint GET "/api/divipol/departamentos"
    Entonces recibo un código de respuesta 200
    Y cada departamento cumple la regla: totalPotencial = mujeres + hombres
    Y todos los departamentos tienen:
      | campo          | validacion        |
      | coddepto       | mayor a 0         |
      | totalPotencial | mayor o igual a 0 |
      | mesas          | mayor o igual a 0 |
      | mujeres        | mayor o igual a 0 |
      | hombres        | mayor o igual a 0 |
    Y al menos un departamento se llama "BOLIVAR"
    Y al menos un departamento se llama "ANTIOQUIA"
