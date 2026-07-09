# ================================================================
#  FinAdvisor — Stop All Services
#  Run this from the project root: .\stop-all.ps1
# ================================================================

Write-Host ""
Write-Host "  🛑 Stopping all FinAdvisor services..." -ForegroundColor Red
Write-Host ""

# Kill all Java processes (Spring Boot services)
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    $count = $javaProcs.Count
    $javaProcs | Stop-Process -Force
    Write-Host "  ✅ Stopped $count Java process(es) (Spring Boot services)" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  No Java processes running" -ForegroundColor Gray
}

# Kill Node.js (Vite frontend)
$nodeProcs = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $count = $nodeProcs.Count
    $nodeProcs | Stop-Process -Force
    Write-Host "  ✅ Stopped $count Node.js process(es) (Frontend)" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  No Node.js processes running" -ForegroundColor Gray
}

Write-Host ""
Write-Host "  ✅ All services stopped." -ForegroundColor Cyan
Write-Host ""
