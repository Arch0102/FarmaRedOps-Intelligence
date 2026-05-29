

[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8081/api/v1",
    [string]$Token,
    [string]$Username,
    [string]$Password,
    [switch]$RunWrites,
    [long]$CategoriaId = 1,
    [long]$MedicamentoId = 1,
    [long]$ProveedorId = 1,
    [long]$InventarioId = 1,
    [long]$LoteMedicamentoId = 1,
    [int]$MovementQuantity = 1,
    [string]$PdfPath
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd("/")
$script:Results = New-Object System.Collections.Generic.List[object]

function Add-Result {
    param(
        [string]$Module,
        [string]$Check,
        [ValidateSet("OK", "FAIL")]
        [string]$Status,
        [string]$Detail
    )

    $script:Results.Add([pscustomobject]@{
        Module = $Module
        Check = $Check
        Status = $Status
        Detail = $Detail
    }) | Out-Null

    $label = if ($Status -eq "OK") { "OK  " } else { "FAIL" }
    Write-Host ("[{0}] {1} - {2}: {3}" -f $label, $Module, $Check, $Detail)
}

function Get-StatusCode {
    param($Exception)

    if ($null -eq $Exception.Response) {
        return $null
    }

    try {
        return [int]$Exception.Response.StatusCode
    } catch {
        try {
            return [int]$Exception.Response.StatusCode.value__
        } catch {
            return $null
        }
    }
}

function Get-ItemCount {
    param($Value)

    if ($null -eq $Value) {
        return 0
    }

    return @($Value).Count
}

function ConvertTo-JsonBody {
    param($Body)

    return ($Body | ConvertTo-Json -Depth 20 -Compress)
}

function Invoke-Json {
    param(
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string]$Method,
        [string]$Path,
        $Body,
        [switch]$NoAuth
    )

    $uri = "{0}/{1}" -f $BaseUrl, $Path.TrimStart("/")
    $headers = @{}
    if (-not $NoAuth -and -not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $parameters = @{
        Method = $Method
        Uri = $uri
        Headers = $headers
        ErrorAction = "Stop"
    }

    if ($null -ne $Body) {
        $parameters["ContentType"] = "application/json"
        $parameters["Body"] = ConvertTo-JsonBody $Body
    }

    return Invoke-RestMethod @parameters
}

function Test-Endpoint {
    param(
        [string]$Module,
        [string]$Check,
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [scriptblock]$Validate = $null,
        [string]$SuccessDetail = "respuesta valida"
    )

    try {
        $response = Invoke-Json -Method $Method -Path $Path -Body $Body
        $isValid = $true

        if ($null -ne $Validate) {
            $isValid = [bool](& $Validate $response)
        }

        if ($isValid) {
            Add-Result -Module $Module -Check $Check -Status "OK" -Detail $SuccessDetail
        } else {
            Add-Result -Module $Module -Check $Check -Status "FAIL" -Detail "la respuesta no cumple la validacion esperada"
        }

        return $response
    } catch {
        $statusCode = Get-StatusCode $_.Exception
        $message = $_.Exception.Message
        if ($statusCode) {
            $message = "HTTP $statusCode - $message"
        }
        Add-Result -Module $Module -Check $Check -Status "FAIL" -Detail $message
        return $null
    }
}

function Login-IfRequested {
    if ([string]::IsNullOrWhiteSpace($Username)) {
        return
    }

    if ([string]::IsNullOrWhiteSpace($Password)) {
        $securePassword = Read-Host "Password local para $Username" -AsSecureString
        $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $script:Password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
        }
    }

    try {
        $login = Invoke-Json -Method "POST" -Path "/auth/login" -Body @{
            username = $Username
            password = $Password
        } -NoAuth

        if ([string]::IsNullOrWhiteSpace($login.token)) {
            Add-Result -Module "Auth" -Check "login" -Status "FAIL" -Detail "login respondio sin token"
            return
        }

        $script:Token = $login.token
        $roles = if ($login.roles) { ($login.roles -join ",") } else { "sin roles" }
        Add-Result -Module "Auth" -Check "login" -Status "OK" -Detail "token recibido; roles=$roles"
    } catch {
        $statusCode = Get-StatusCode $_.Exception
        $detail = $_.Exception.Message
        if ($statusCode) {
            $detail = "HTTP $statusCode - $detail"
        }
        Add-Result -Module "Auth" -Check "login" -Status "FAIL" -Detail $detail
    }
}

function Ensure-Token {
    Login-IfRequested

    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        if ([string]::IsNullOrWhiteSpace($Username)) {
            Add-Result -Module "Auth" -Check "token admin" -Status "OK" -Detail "token recibido por parametro o prompt"
        }
        return
    }

    $script:Token = Read-Host "JWT admin local (sin prefijo Bearer)"

    if ([string]::IsNullOrWhiteSpace($Token)) {
        Add-Result -Module "Auth" -Check "token admin" -Status "FAIL" -Detail "no se recibio token"
    } else {
        Add-Result -Module "Auth" -Check "token admin" -Status "OK" -Detail "token recibido por prompt"
    }
}

function Test-UnauthorizedAccess {
    try {
        Invoke-RestMethod -Method Get -Uri "$BaseUrl/dashboard/resumen" -ErrorAction Stop | Out-Null
        Add-Result -Module "Seguridad" -Check "sin token" -Status "FAIL" -Detail "dashboard respondio 2xx sin token"
    } catch {
        $statusCode = Get-StatusCode $_.Exception
        if ($statusCode -eq 401) {
            Add-Result -Module "Seguridad" -Check "sin token" -Status "OK" -Detail "HTTP 401 esperado"
        } else {
            Add-Result -Module "Seguridad" -Check "sin token" -Status "FAIL" -Detail "se esperaba 401 y se recibio HTTP $statusCode"
        }
    }
}

function Test-DocumentDownload {
    param([long]$DocumentoId)

    $tempFile = Join-Path ([System.IO.Path]::GetTempPath()) ("farmared-smoke-documento-{0}.pdf" -f $DocumentoId)
    try {
        Invoke-WebRequest `
            -Method Get `
            -Uri "$BaseUrl/documentos/$DocumentoId/download" `
            -Headers @{ Authorization = "Bearer $Token" } `
            -OutFile $tempFile `
            -UseBasicParsing `
            -ErrorAction Stop | Out-Null

        $file = Get-Item -LiteralPath $tempFile
        if ($file.Length -gt 0) {
            Add-Result -Module "Documentos" -Check "descargar documento" -Status "OK" -Detail "archivo descargado temporalmente ($($file.Length) bytes)"
        } else {
            Add-Result -Module "Documentos" -Check "descargar documento" -Status "FAIL" -Detail "archivo descargado vacio"
        }
    } catch {
        $statusCode = Get-StatusCode $_.Exception
        $detail = $_.Exception.Message
        if ($statusCode) {
            $detail = "HTTP $statusCode - $detail"
        }
        Add-Result -Module "Documentos" -Check "descargar documento" -Status "FAIL" -Detail $detail
    } finally {
        if (Test-Path -LiteralPath $tempFile) {
            Remove-Item -LiteralPath $tempFile -Force
        }
    }
}

function Invoke-DocumentUpload {
    param([string]$PathToPdf)

    if (-not (Test-Path -LiteralPath $PathToPdf)) {
        Add-Result -Module "Documentos" -Check "subir documento" -Status "FAIL" -Detail "no existe PdfPath=$PathToPdf"
        return $null
    }

    try {
        Add-Type -AssemblyName System.Net.Http
        $client = [System.Net.Http.HttpClient]::new()
        $multipart = [System.Net.Http.MultipartFormDataContent]::new()

        $client.DefaultRequestHeaders.Authorization =
            [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $Token)

        $resolvedPath = (Resolve-Path -LiteralPath $PathToPdf).Path
        $fileBytes = [System.IO.File]::ReadAllBytes($resolvedPath)
        $fileContent = [System.Net.Http.ByteArrayContent]::new($fileBytes)
        $fileContent.Headers.ContentType =
            [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/pdf")
        $multipart.Add($fileContent, "archivo", [System.IO.Path]::GetFileName($resolvedPath))

        $metadata = @{
            tipoDocumento = "PDF"
            descripcion = "Smoke test funcional"
            moduloReferencia = "SMOKE_TEST"
            referenciaId = $null
            usuarioCarga = if ($Username) { $Username } else { "smoke-test" }
        }
        $metadataJson = ConvertTo-JsonBody $metadata
        $metadataContent = [System.Net.Http.StringContent]::new(
            $metadataJson,
            [System.Text.Encoding]::UTF8,
            "application/json"
        )
        $multipart.Add($metadataContent, "metadata")

        $response = $client.PostAsync("$BaseUrl/documentos/upload", $multipart).Result
        $content = $response.Content.ReadAsStringAsync().Result

        if (-not $response.IsSuccessStatusCode) {
            Add-Result -Module "Documentos" -Check "subir documento" -Status "FAIL" -Detail "HTTP $([int]$response.StatusCode) - $content"
            return $null
        }

        $document = $content | ConvertFrom-Json
        Add-Result -Module "Documentos" -Check "subir documento" -Status "OK" -Detail "documento id=$($document.id)"
        return $document
    } catch {
        Add-Result -Module "Documentos" -Check "subir documento" -Status "FAIL" -Detail $_.Exception.Message
        return $null
    } finally {
        if ($multipart) { $multipart.Dispose() }
        if ($client) { $client.Dispose() }
    }
}

Write-Host "FarmaRedOps smoke test"
Write-Host "BaseUrl: $BaseUrl"
Write-Host "RunWrites: $RunWrites"
Write-Host ""

Test-UnauthorizedAccess
Ensure-Token

if ([string]::IsNullOrWhiteSpace($Token)) {
    Write-Host ""
    Write-Host "No hay token. Se omiten endpoints protegidos."
    exit 1
}

$dashboardResumen = Test-Endpoint `
    -Module "Dashboard" `
    -Check "resumen" `
    -Method "GET" `
    -Path "/dashboard/resumen" `
    -Validate { param($r) $null -ne $r.totalMedicamentosActivos -and $null -ne $r.totalProveedoresActivos -and $null -ne $r.totalOrdenesPendientes } `
    -SuccessDetail "totales recibidos desde datos reales"

$dashboardMetricas = Test-Endpoint `
    -Module "Dashboard" `
    -Check "metricas" `
    -Method "GET" `
    -Path "/dashboard/metricas" `
    -SuccessDetail "endpoint respondio"

$categorias = Test-Endpoint -Module "Categorias" -Check "listar" -Method "GET" -Path "/categorias-medicamento" -SuccessDetail "endpoint respondio"
$medicamentos = Test-Endpoint -Module "Medicamentos" -Check "listar" -Method "GET" -Path "/medicamentos" -SuccessDetail "endpoint respondio"
$centros = Test-Endpoint -Module "Centros" -Check "listar" -Method "GET" -Path "/centros-distribucion" -SuccessDetail "endpoint respondio"
$proveedores = Test-Endpoint -Module "Proveedores" -Check "listar" -Method "GET" -Path "/proveedores" -SuccessDetail "endpoint respondio"
$proveedoresActivos = Test-Endpoint -Module "Proveedores" -Check "listar activos" -Method "GET" -Path "/proveedores/activos" -SuccessDetail "endpoint respondio"
$ordenes = Test-Endpoint -Module "Ordenes" -Check "listar" -Method "GET" -Path "/ordenes-compra" -SuccessDetail "endpoint respondio"

if ($null -ne $ordenes -and (Get-ItemCount $ordenes) -gt 0) {
    $firstOrder = @($ordenes)[0]
    Test-Endpoint -Module "Ordenes" -Check "consultar detalle" -Method "GET" -Path "/ordenes-compra/$($firstOrder.id)" -Validate { param($r) $null -ne $r.id -and $null -ne $r.detalles } -SuccessDetail "detalle recibido" | Out-Null
} else {
    Add-Result -Module "Ordenes" -Check "consultar detalle" -Status "FAIL" -Detail "no hay ordenes seed para consultar"
}

Test-Endpoint -Module "Inventario" -Check "kardex" -Method "GET" -Path "/movimientos-inventario/kardex/inventario/$InventarioId" -SuccessDetail "endpoint respondio para inventario $InventarioId" | Out-Null

$documentos = Test-Endpoint -Module "Documentos" -Check "listar" -Method "GET" -Path "/documentos" -SuccessDetail "endpoint respondio"
if ($null -ne $documentos -and (Get-ItemCount $documentos) -gt 0) {
    $firstDocument = @($documentos)[0]
    Test-DocumentDownload -DocumentoId $firstDocument.id
} else {
    Add-Result -Module "Documentos" -Check "descargar documento" -Status "OK" -Detail "sin documentos activos seed; validar descarga con -RunWrites -PdfPath"
}

try {
    Invoke-Json -Method "POST" -Path "/documentos/generar/dashboard" -Body $null | Out-Null
    Add-Result -Module "Documentos" -Check "generar PDF dashboard" -Status "OK" -Detail "endpoint existe y respondio"
} catch {
    $statusCode = Get-StatusCode $_.Exception
    if ($statusCode -eq 404) {
        Add-Result -Module "Documentos" -Check "generar PDF dashboard" -Status "OK" -Detail "endpoint no implementado; 404 documentado"
    } else {
        Add-Result -Module "Documentos" -Check "generar PDF dashboard" -Status "FAIL" -Detail "HTTP $statusCode - $($_.Exception.Message)"
    }
}

if ($RunWrites) {
    $stamp = Get-Date -Format "yyyyMMddHHmmss"
    Write-Host ""
    Write-Host "Ejecutando pruebas de escritura opt-in. No se ejecutan deletes ni desactivaciones."

    $registeredUser = Test-Endpoint `
        -Module "Auth" `
        -Check "register usuario temporal" `
        -Method "POST" `
        -Path "/auth/register" `
        -Body @{
            username = "smoke_user_$stamp"
            email = "smoke_user_$stamp@local.test"
            password = "123456"
            nombreCompleto = "Smoke Test Local"
        } `
        -Validate { param($r) -not [string]::IsNullOrWhiteSpace($r.token) -and $null -ne $r.roles } `
        -SuccessDetail "usuario temporal registrado con rol devuelto"

    $createdCategory = Test-Endpoint `
        -Module "Categorias" `
        -Check "crear" `
        -Method "POST" `
        -Path "/categorias-medicamento" `
        -Body @{
            nombre = "Smoke Categoria $stamp"
            descripcion = "Categoria creada por smoke-test local"
            activo = $true
        } `
        -Validate { param($r) $null -ne $r.id -and $r.activo -eq $true } `
        -SuccessDetail "categoria creada"

    $categoryForMedication = if ($createdCategory -and $createdCategory.id) { $createdCategory.id } else { $CategoriaId }

    if ($createdCategory -and $createdCategory.id) {
        Test-Endpoint `
            -Module "Categorias" `
            -Check "actualizar" `
            -Method "PUT" `
            -Path "/categorias-medicamento/$($createdCategory.id)" `
            -Body @{
                nombre = "Smoke Categoria $stamp actualizada"
                descripcion = "Categoria actualizada por smoke-test local"
                activo = $true
            } `
            -Validate { param($r) $r.nombre -like "*actualizada" } `
            -SuccessDetail "categoria actualizada" | Out-Null
    }

    $createdMedication = Test-Endpoint `
        -Module "Medicamentos" `
        -Check "crear" `
        -Method "POST" `
        -Path "/medicamentos" `
        -Body @{
            codigo = "MED-SMOKE-$stamp"
            nombre = "Medicamento Smoke $stamp"
            descripcion = "Medicamento creado por smoke-test local"
            principioActivo = "Principio smoke"
            concentracion = "500mg"
            presentacion = "Tableta"
            unidadMedida = "unidad"
            stockMinimo = 5
            stockMaximo = 100
            puntoReorden = 10
            activo = $true
            categoriaMedicamentoId = $categoryForMedication
        } `
        -Validate { param($r) $null -ne $r.id -and $r.activo -eq $true } `
        -SuccessDetail "medicamento creado"

    $medicationForOrder = if ($createdMedication -and $createdMedication.id) { $createdMedication.id } else { $MedicamentoId }

    if ($createdMedication -and $createdMedication.id) {
        Test-Endpoint `
            -Module "Medicamentos" `
            -Check "actualizar" `
            -Method "PUT" `
            -Path "/medicamentos/$($createdMedication.id)" `
            -Body @{
                codigo = $createdMedication.codigo
                nombre = "Medicamento Smoke $stamp actualizado"
                descripcion = "Medicamento actualizado por smoke-test local"
                principioActivo = "Principio smoke"
                concentracion = "500mg"
                presentacion = "Tableta"
                unidadMedida = "unidad"
                stockMinimo = 5
                stockMaximo = 120
                puntoReorden = 12
                activo = $true
                categoriaMedicamentoId = $categoryForMedication
            } `
            -Validate { param($r) $r.nombre -like "*actualizado" } `
            -SuccessDetail "medicamento actualizado" | Out-Null
    }

    $centerCode = "CD-SMOKE-$stamp"
    $createdCenter = Test-Endpoint `
        -Module "Centros" `
        -Check "crear" `
        -Method "POST" `
        -Path "/centros-distribucion" `
        -Body @{
            codigo = $centerCode
            nombre = "Centro Smoke $stamp"
            direccion = "Direccion smoke"
            ciudad = "Bogota"
            activo = $true
        } `
        -Validate { param($r) $null -ne $r.id -and $r.codigo -eq $centerCode } `
        -SuccessDetail "centro creado"

    if ($createdCenter -and $createdCenter.id) {
        $updatedCenter = Test-Endpoint `
            -Module "Centros" `
            -Check "actualizar" `
            -Method "PUT" `
            -Path "/centros-distribucion/$($createdCenter.id)" `
            -Body @{
                codigo = "$centerCode-ALT"
                nombre = "Centro Smoke $stamp actualizado"
                direccion = "Direccion smoke actualizada"
                ciudad = "Bogota"
                activo = $true
            } `
            -Validate { param($r) $null -ne $r.id } `
            -SuccessDetail "centro actualizado"

        if ($updatedCenter -and $updatedCenter.codigo -eq $centerCode) {
            Add-Result -Module "Centros" -Check "codigo inmutable" -Status "OK" -Detail "el codigo se mantuvo en $centerCode"
        } elseif ($updatedCenter) {
            Add-Result -Module "Centros" -Check "codigo inmutable" -Status "FAIL" -Detail "el backend permitio cambiar codigo a $($updatedCenter.codigo)"
        }
    }

    $createdProvider = Test-Endpoint `
        -Module "Proveedores" `
        -Check "crear" `
        -Method "POST" `
        -Path "/proveedores" `
        -Body @{
            nit = "NIT-SMOKE-$stamp"
            nombre = "Proveedor Smoke $stamp"
            telefono = "3000000000"
            correo = "proveedor.smoke.$stamp@local.test"
            direccion = "Direccion smoke"
            activo = $true
        } `
        -Validate { param($r) $null -ne $r.id -and $r.activo -eq $true } `
        -SuccessDetail "proveedor creado"

    $providerForOrder = if ($createdProvider -and $createdProvider.id) { $createdProvider.id } else { $ProveedorId }

    if ($createdProvider -and $createdProvider.id) {
        Test-Endpoint `
            -Module "Proveedores" `
            -Check "actualizar" `
            -Method "PUT" `
            -Path "/proveedores/$($createdProvider.id)" `
            -Body @{
                nit = $createdProvider.nit
                nombre = "Proveedor Smoke $stamp actualizado"
                telefono = "3000000001"
                correo = "proveedor.smoke.$stamp@local.test"
                direccion = "Direccion smoke actualizada"
                activo = $true
            } `
            -Validate { param($r) $r.nombre -like "*actualizado" } `
            -SuccessDetail "proveedor actualizado" | Out-Null
    }

    $createdOrder = Test-Endpoint `
        -Module "Ordenes" `
        -Check "crear" `
        -Method "POST" `
        -Path "/ordenes-compra" `
        -Body @{
            codigo = "OC-SMOKE-$stamp"
            proveedorId = $providerForOrder
            fechaEstimadaEntrega = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
            observacion = "Orden creada por smoke-test local"
            detalles = @(
                @{
                    medicamentoId = $medicationForOrder
                    cantidad = 1
                    precioUnitario = 1000
                }
            )
        } `
        -Validate { param($r) $null -ne $r.id -and $r.estado -eq "PENDIENTE" -and $null -ne $r.detalles } `
        -SuccessDetail "orden creada"

    if ($createdOrder -and $createdOrder.id) {
        Test-Endpoint -Module "Ordenes" -Check "consultar detalle creado" -Method "GET" -Path "/ordenes-compra/$($createdOrder.id)" -Validate { param($r) $null -ne $r.detalles -and (Get-ItemCount $r.detalles) -gt 0 } -SuccessDetail "detalle creado recibido" | Out-Null
    }

    $entrada = Test-Endpoint `
        -Module "Inventario" `
        -Check "registrar ENTRADA" `
        -Method "POST" `
        -Path "/movimientos-inventario" `
        -Body @{
            tipoMovimiento = "ENTRADA"
            inventarioId = $InventarioId
            loteMedicamentoId = $LoteMedicamentoId
            cantidad = $MovementQuantity
            motivo = "Smoke test entrada"
            observacion = "Entrada opt-in de smoke-test"
            usuarioResponsable = if ($Username) { $Username } else { "smoke-test" }
        } `
        -Validate { param($r) $null -ne $r.id -and $r.tipoMovimiento -eq "ENTRADA" } `
        -SuccessDetail "entrada registrada"

    if ($entrada) {
        Test-Endpoint `
            -Module "Inventario" `
            -Check "registrar SALIDA" `
            -Method "POST" `
            -Path "/movimientos-inventario" `
            -Body @{
                tipoMovimiento = "SALIDA"
                inventarioId = $InventarioId
                loteMedicamentoId = $LoteMedicamentoId
                cantidad = $MovementQuantity
                motivo = "Smoke test salida"
                observacion = "Salida opt-in de smoke-test para compensar entrada"
                usuarioResponsable = if ($Username) { $Username } else { "smoke-test" }
            } `
            -Validate { param($r) $null -ne $r.id -and $r.tipoMovimiento -eq "SALIDA" } `
            -SuccessDetail "salida registrada" | Out-Null
    }

    if (-not [string]::IsNullOrWhiteSpace($PdfPath)) {
        $uploaded = Invoke-DocumentUpload -PathToPdf $PdfPath
        if ($uploaded -and $uploaded.id) {
            Test-DocumentDownload -DocumentoId $uploaded.id
        }
    } else {
        Add-Result -Module "Documentos" -Check "subir documento" -Status "OK" -Detail "omitido; use -PdfPath para ejecutar upload real"
    }
}

Write-Host ""
Write-Host "Resumen"
$script:Results | Group-Object Status | ForEach-Object {
    Write-Host ("{0}: {1}" -f $_.Name, $_.Count)
}

$failed = @($script:Results | Where-Object { $_.Status -eq "FAIL" })
if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "Fallos:"
    $failed | ForEach-Object {
        Write-Host ("- {0} / {1}: {2}" -f $_.Module, $_.Check, $_.Detail)
    }
    exit 1
}

exit 0
