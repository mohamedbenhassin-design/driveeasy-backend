# Démarre tout le backend DriveEasy dans l'ordre recommandé (Windows PowerShell).
# Prérequis : JDK 21, Maven, Docker. Lancer depuis le dossier driveeasy-backend :
#   powershell -ExecutionPolicy Bypass -File .\start-all.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# Utilise un JDK 21 même si le "java" par défaut du PATH est plus ancien
$jdk21 = Get-ChildItem -Directory -ErrorAction SilentlyContinue `
    "$env:ProgramFiles\Microsoft\jdk-21*", "$env:ProgramFiles\Eclipse Adoptium\jdk-21*", "$env:ProgramFiles\Java\jdk-21*" |
    Select-Object -First 1
if ($jdk21) {
    $env:JAVA_HOME = $jdk21.FullName
    $env:Path = "$($jdk21.FullName)\bin;$env:Path"
    Write-Host "JDK utilisé : $($jdk21.FullName)" -ForegroundColor DarkGray
} else {
    Write-Host "Attention : JDK 21 introuvable, utilisation du java du PATH" -ForegroundColor Yellow
}

Write-Host "1/4 Compilation (mvn clean package)..." -ForegroundColor Cyan
mvn -q clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Echec de la compilation" }

Write-Host "2/4 Zipkin (Docker)..." -ForegroundColor Cyan
docker compose up -d

function Start-Microservice($name) {
    Write-Host "   -> $name" -ForegroundColor Green
    Start-Process -FilePath "java" -ArgumentList "-jar", "$name\target\$name-1.0.0.jar" -WindowStyle Minimized
}

function Wait-Url($url) {
    for ($i = 0; $i -lt 60; $i++) {
        try { if ((Invoke-WebRequest -UseBasicParsing $url -TimeoutSec 2).StatusCode -eq 200) { return } } catch {}
        Start-Sleep -Seconds 2
    }
    throw "Timeout en attendant $url"
}

Write-Host "3/4 Service Registry puis Config Server..." -ForegroundColor Cyan
Start-Microservice "service-registry"
Wait-Url "http://localhost:8761"
Start-Microservice "config-server"
Wait-Url "http://localhost:8888/actuator/health"

Write-Host "4/4 Microservices métier puis API Gateway..." -ForegroundColor Cyan
Start-Microservice "vehicule-service"
Start-Microservice "client-service"
Start-Microservice "location-service"
Wait-Url "http://localhost:8083/actuator/health"
Start-Microservice "api-gateway"
Wait-Url "http://localhost:8080/actuator/health"

Write-Host ""
Write-Host "Backend démarré :" -ForegroundColor Cyan
Write-Host "  Eureka   http://localhost:8761"
Write-Host "  Gateway  http://localhost:8080/api/vehicules"
Write-Host "  Zipkin   http://localhost:9411"
Write-Host "(La gateway peut répondre 503 pendant ~30 s, le temps de récupérer le registre Eureka.)"
