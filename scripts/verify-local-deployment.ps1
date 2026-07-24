[CmdletBinding()]
param(
    [string]$ApplicationUrl = "http://localhost:8080",
    [string]$IdentityProviderUrl = "http://localhost:9000",
    [string]$Username = "banking-developer",
    [string]$Password = "banking-local-change-me"
)

$ErrorActionPreference = "Stop"

if (-not [Uri]::IsWellFormedUriString($ApplicationUrl, [UriKind]::Absolute)) {
    throw "ApplicationUrl must be an absolute URL."
}
if (-not [Uri]::IsWellFormedUriString($IdentityProviderUrl, [UriKind]::Absolute)) {
    throw "IdentityProviderUrl must be an absolute URL."
}
if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw "Username and Password must not be blank."
}

$readiness = Invoke-RestMethod `
    -Uri "$ApplicationUrl/actuator/health/readiness" `
    -Method Get
if ($readiness.status -ne "UP") {
    throw "Application readiness status is '$($readiness.status)', expected 'UP'."
}

$tokenResponse = Invoke-RestMethod `
    -Uri "$IdentityProviderUrl/realms/banking/protocol/openid-connect/token" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        grant_type = "password"
        client_id  = "banking-cli"
        username   = $Username
        password   = $Password
    }

if ([string]::IsNullOrWhiteSpace($tokenResponse.access_token)) {
    throw "The identity provider did not return an access token."
}

$headers = @{
    Authorization = "Bearer $($tokenResponse.access_token)"
}
$uniqueSuffix = [Guid]::NewGuid().ToString("N")
$customer = Invoke-RestMethod `
    -Uri "$ApplicationUrl/api/v1/customers" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json" `
    -Body (@{
        fullName = "Local Deployment Verification"
        emailAddress = "deployment-$uniqueSuffix@example.com"
    } | ConvertTo-Json)

$account = Invoke-RestMethod `
    -Uri "$ApplicationUrl/api/v1/accounts" `
    -Method Post `
    -Headers $headers `
    -ContentType "application/json" `
    -Body (@{
        ownerId = $customer.customerId
        currencyCode = "USD"
    } | ConvertTo-Json)

$depositHeaders = @{
    Authorization = "Bearer $($tokenResponse.access_token)"
    "Idempotency-Key" = [Guid]::NewGuid().ToString()
}
$deposit = Invoke-RestMethod `
    -Uri "$ApplicationUrl/api/v1/accounts/$($account.accountId)/deposits" `
    -Method Post `
    -Headers $depositHeaders `
    -ContentType "application/json" `
    -Body (@{
        amount = 25.00
        currencyCode = "USD"
        description = "Local deployment verification"
    } | ConvertTo-Json)

if ($deposit.balance -ne 25.00) {
    throw "Unexpected balance '$($deposit.balance)', expected '25.00'."
}

[pscustomobject]@{
    Readiness = $readiness.status
    TokenIssuer = $IdentityProviderUrl
    CustomerId = $customer.customerId
    AccountId = $account.accountId
    Balance = $deposit.balance
    Result = "PASS"
}
