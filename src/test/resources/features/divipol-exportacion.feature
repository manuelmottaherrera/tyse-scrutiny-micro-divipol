# language: es
@exportacion
Característica: Exportar datos DIVIPOL a CSV y PDF
  Como usuario de la API DIVIPOL
  Quiero exportar datos de división política
  Para generar reportes en formatos CSV y PDF

  Antecedentes:
    Dado que la base de datos tiene cargados los datos de DIVIPOL
    Y que soy un usuario autenticado

  # =====================================================
  # Exportación CSV modo filtros
  # =====================================================

  @csv @filtros @happy-path @smoke
  Escenario: Exportar todos los departamentos a CSV sin filtros
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv"
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Reporte DIVIPOL"
    Y el archivo CSV contiene el texto "Código,Departamento"
    Y el archivo CSV contiene alguno de los textos:
      | texto     |
      | ANTIOQUIA |
      | BOGOTA    |
      | BOLIVAR   |

  @csv @filtros
  Escenario: Exportar municipios de Antioquia a CSV
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv" con parámetros:
      | parámetro | valor |
      | codDepto  | 5     |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Municipio"
    Y el archivo CSV contiene líneas que empiezan con "050"

  @csv @filtros
  Escenario: Exportar zonas de Medellín a CSV
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv" con parámetros:
      | parámetro | valor |
      | codDepto  | 5     |
      | codMpio   | 1     |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Zona"

  @csv @filtros
  Escenario: Exportar puestos de una zona específica a CSV
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv" con parámetros:
      | parámetro | valor |
      | codDepto  | 5     |
      | codMpio   | 1     |
      | codZona   | 1     |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Puesto"

  @csv @filtros
  Escenario: El CSV de departamentos incluye códigos DIVIPOL de 9 dígitos
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv"
    Entonces recibo un código de respuesta 200
    Y el archivo CSV contiene códigos de 9 dígitos

  # =====================================================
  # Exportación PDF modo filtros
  # Nota: Tests de PDF pueden ser lentos (~30s) en CI
  # =====================================================

  @pdf @filtros @happy-path @slow
  Escenario: Exportar todos los departamentos a PDF sin filtros
    Cuando exporto a PDF el endpoint "/api/divipol/export/filters/pdf"
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "application/pdf"
    Y el archivo PDF es válido
    Y el archivo PDF tiene al menos 1 página

  @pdf @filtros @slow
  Escenario: Exportar municipios de Antioquia a PDF
    Cuando exporto a PDF el endpoint "/api/divipol/export/filters/pdf" con parámetros:
      | parámetro | valor |
      | codDepto  | 5     |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "application/pdf"
    Y el archivo PDF es válido
    Y el archivo PDF tiene al menos 1 página

  # =====================================================
  # Exportación CSV modo búsqueda
  # =====================================================

  @csv @busqueda @happy-path @smoke
  Escenario: Exportar resultados de búsqueda por nombre a CSV
    Cuando exporto búsqueda a CSV con:
      | parámetro | valor    |
      | q         | MEDELLIN |
      | mode      | name     |
      | page      | 0        |
      | size      | 20       |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Reporte de Búsqueda DIVIPOL"
    Y el archivo CSV contiene el texto "MEDELLIN"
    Y el archivo CSV contiene el texto "Modo: Nombre"

  @csv @busqueda
  Escenario: Exportar resultados de búsqueda por código a CSV
    Cuando exporto búsqueda a CSV con:
      | parámetro | valor     |
      | q         | 050010000 |
      | mode      | code      |
      | page      | 0         |
      | size      | 20        |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "Modo: Código"

  @csv @busqueda
  Escenario: Exportar todos los resultados de búsqueda con exportAll
    Cuando exporto búsqueda a CSV con:
      | parámetro | valor   |
      | q         | BOLIVAR |
      | mode      | name    |
      | exportAll | true    |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV contiene el texto "BOLIVAR"

  @csv @busqueda
  Escenario: Respetar paginación en exportación de búsqueda
    Cuando exporto búsqueda a CSV con:
      | parámetro | valor |
      | q         | A     |
      | mode      | name  |
      | page      | 0     |
      | size      | 5     |
      | exportAll | false |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "text/csv"
    Y el archivo CSV tiene máximo 5 líneas de datos

  # =====================================================
  # Exportación PDF modo búsqueda
  # =====================================================

  @pdf @busqueda @happy-path @slow
  Escenario: Exportar resultados de búsqueda a PDF
    Cuando exporto búsqueda a PDF con:
      | parámetro | valor  |
      | q         | BOGOTA |
      | mode      | name   |
      | page      | 0      |
      | size      | 20     |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "application/pdf"
    Y el archivo PDF es válido
    Y el archivo PDF tiene al menos 1 página

  @pdf @busqueda @slow
  Escenario: Exportar todos los resultados de búsqueda a PDF
    Cuando exporto búsqueda a PDF con:
      | parámetro | valor        |
      | q         | CUNDINAMARCA |
      | mode      | name         |
      | exportAll | true         |
    Entonces recibo un código de respuesta 200
    Y el content-type de la respuesta es "application/pdf"
    Y el archivo PDF es válido
    Y el archivo PDF tiene al menos 1 página

  # =====================================================
  # Tests de seguridad
  # =====================================================

  @seguridad
  Escenario: Exportar CSV sin autenticación debe fallar
    Dado que NO estoy autenticado
    Cuando exporto a CSV el endpoint "/api/divipol/export/filters/csv"
    Entonces recibo un código de respuesta 401

  @seguridad
  Escenario: Exportar PDF sin autenticación debe fallar
    Dado que NO estoy autenticado
    Cuando exporto a PDF el endpoint "/api/divipol/export/filters/pdf"
    Entonces recibo un código de respuesta 401
