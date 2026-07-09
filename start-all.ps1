# ================================================================
#  FinAdvisor — Start All Services
#  Run this from the project root: .\start-all.ps1
#  Each service opens in its own colour-coded terminal window.
# ================================================================

param(
    [switch]$SkipFrontend,
    [switch]$Help
)

if ($Help) {
    Write-Host ""
    Write-Host "Usage: .\start-all.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -SkipFrontend   Don't start the React frontend"
    Write-Host "  -Help           Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\start-all.ps1                  # Start everything"
    Write-Host "  .\start-all.ps1 -SkipFrontend    # Start backend only"
    Write-Host ""
    exit
}

$ROOT = $PSScriptRoot

# ── Colours ──────────────────────────────────────────────────────
function Write-Banner {
    param($Text, $Color = "Cyan")
    Write-Host ""
    Write-Host "  ══════════════════════════════════════════" -ForegroundColor $Color
    Write-Host "   $Text" -ForegroundColor $Color
    Write-Host "  ══════════════════════════════════════════" -ForegroundColor $Color
    Write-Host ""
}

function Write-Step {
    param($Icon, $Text, $Color = "White")
    Write-Host "  $Icon  $Text" -ForegroundColor $Color
}

# ── Helper: open a new PowerShell window for a service ───────────
function Start-Service {
    param(
        [string]$Name,
        [string]$Dir,
        [string]$Cmd,
        [string]$Color = "White"
    )
    $fullDir = Join-Path $ROOT $Dir
    if (-not (Test-Path $fullDir)) {
        Write-Step "⚠️" "Directory not found: $Dir — skipping $Name" "Yellow"
        return
    }
    Write-Step "▶" "Starting $Name ..." $Color
    Start-Process powershell -ArgumentList `
        "-NoExit", `
        "-Command", `
        "& { `$host.UI.RawUI.WindowTitle = '$Name'; cd '$fullDir'; $Cmd }"
}

# ── Helper: wait until a port is open ────────────────────────────
function Wait-ForPort {
    param([string]$Name, [int]$Port, [int]$TimeoutSec = 120)
    Write-Step "⏳" "Waiting for $Name (port $Port) to be ready..." "Yellow"
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect("localhost", $Port)
            $tcp.Close()
            Write-Step "✅" "$Name is UP on port $Port" "Green"
            return $true
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    Write-Step "❌" "$Name did not start within ${TimeoutSec}s on port $Port" "Red"
    return $false
}

# ═══════════════════════════════════════════════════════════════
Write-Banner "💰 FinAdvisor — Starting All Services" "Cyan"
Write-Host "  Project root: $ROOT" -ForegroundColor Gray
Write-Host ""

# ─── Step 1: Eureka (must start first — others register to it) ───
Write-Banner "① Eureka Discovery Server" "Magenta"
Start-Service `
    -Name  "Eureka (8761)" `
    -Dir   "eureka" `
    -Cmd   "mvn spring-boot:run" `
    -Color "Magenta"

Write-Step "⏳" "Giving Eureka 20s head start before launching other services..." "Yellow"
Start-Sleep -Seconds 20

$eurekaUp = Wait-ForPort -Name "Eureka" -Port 8761 -TimeoutSec 90
if (-not $eurekaUp) {
    Write-Host ""
    Write-Host "  ❌ Eureka failed to start. Aborting." -ForegroundColor Red
    Write-Host "     Check the Eureka terminal window for errors." -ForegroundColor Red
    exit 1
}

# ─── Step 2: Backend Services (all in parallel) ───────────────────
Write-Banner "② Backend Services (starting in parallel)" "Blue"

Start-Service `
    -Name  "Auth Service (5054)" `
    -Dir   "auth-serviceAlex" `
    -Cmd   "mvn spring-boot:run" `
    -Color "Blue"

Start-Service `
    -Name  "User Service (5053)" `
    -Dir   "user-serviceAlex" `
    -Cmd   "mvn spring-boot:run" `
    -Color "Blue"

Start-Service `
    -Name  "AI Service (5055)" `
    -Dir   "ai-serviceAlex" `
    -Cmd   "mvn spring-boot:run" `
    -Color "DarkCyan"

Start-Service `
    -Name  "Crypto News (5056)" `
    -Dir   "cryptonewsAlexz" `
    -Cmd   "mvn spring-boot:run" `
    -Color "DarkCyan"

Start-Service `
    -Name  "Tax Optimizer (5057)" `
    -Dir   "tax-optimizerAlexz" `
    -Cmd   "mvn spring-boot:run" `
    -Color "DarkGreen"

Start-Service `
    -Name  "Credit Card (5058)" `
    -Dir   "credit-card-service" `
    -Cmd   "mvn spring-boot:run" `
    -Color "DarkGreen"

# ─── Step 3: API Gateway (after all services are launching) ───────
Write-Banner "③ API Gateway" "Yellow"
Write-Step "⏳" "Waiting 15s before starting API Gateway..." "Yellow"
Start-Sleep -Seconds 15

Start-Service `
    -Name  "API Gateway (5051)" `
    -Dir   "api-gatewayAlexz" `
    -Cmd   "mvn spring-boot:run" `
    -Color "Yellow"

# ─── Step 4: Frontend ─────────────────────────────────────────────
if (-not $SkipFrontend) {
    Write-Banner "④ Frontend" "Green"
    $frontendDir = Join-Path $ROOT "crypto-adviser-frontend"
    if (Test-Path $frontendDir) {
        # Install deps if node_modules missing
        if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
            Write-Step "📦" "node_modules not found — running npm install first..." "Yellow"
            Start-Process powershell -ArgumentList `
                "-NoExit", `
                "-Command", `
                "& { `$host.UI.RawUI.WindowTitle = 'Frontend (5173)'; cd '$frontendDir'; npm install; npm run dev }" `
                -Wait:$false
        } else {
            Start-Service `
                -Name  "Frontend (5173)" `
                -Dir   "crypto-adviser-frontend" `
                -Cmd   "npm run dev" `
                -Color "Green"
        }
    } else {
        Write-Step "⚠️" "Frontend directory not found — skipping" "Yellow"
    }
}

# ─── Done ─────────────────────────────────────────────────────────
Write-Banner "🚀 All Services Launched!" "Cyan"
Write-Host "  Services are starting up in separate windows." -ForegroundColor White
Write-Host "  Give them 1–2 minutes to fully initialise." -ForegroundColor Gray
Write-Host ""
Write-Host "  📍 URLs:" -ForegroundColor Cyan
Write-Host "     🌐 Frontend       →  http://localhost:5173" -ForegroundColor Green
Write-Host "     🔀 API Gateway    →  http://localhost:5051" -ForegroundColor Yellow
Write-Host "     🔍 Eureka         →  http://localhost:8761" -ForegroundColor Magenta
Write-Host "     🐰 RabbitMQ UI    →  http://localhost:15672  (guest / guest)" -ForegroundColor DarkYellow
Write-Host ""
Write-Host "  💡 Tip: To stop all services, close each terminal window." -ForegroundColor Gray
Write-Host "     Or run:  Get-Process java | Stop-Process -Force" -ForegroundColor Gray
Write-Host ""
