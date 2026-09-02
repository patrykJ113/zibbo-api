#!/usr/bin/env bash
# Pre-commit guard: blocks the commit if .env holds any plaintext value.
set -euo pipefail

ENV_FILE=".env"
[ -f "$ENV_FILE" ] || exit 0

# A setting line: NAME=value
#   ^              start of line
#   [A-Za-z_]      first char of the name: a letter or underscore
#   [A-Za-z0-9_]*  rest of the name: letters, digits, underscores
#   =.+            an equals sign, then at least one character
SETTING_LINE='^[A-Za-z_][A-Za-z0-9_]*=.+'

# An encrypted value: the name, '=', an optional quote, then the marker.
# dotenvx keeps whatever quote style the line had, so allow " ' ` or none.
ENCRYPTED_VALUE='^[A-Za-z_][A-Za-z0-9_]*=["'"'"'`]?encrypted:'

# The public key is not a secret and is never encrypted.
PUBLIC_KEY='^DOTENV_PUBLIC_KEY'

plaintext=$(grep -E "$SETTING_LINE" "$ENV_FILE" \
  | grep -v "$PUBLIC_KEY" \
  | grep -vE "$ENCRYPTED_VALUE" || true)

if [ -n "$plaintext" ]; then
  echo "❌ Run dotenvx encrypt first"
  echo "Plaintext values found:"
  # Print names only — never echo the secret itself.
  printf '%s\n' "$plaintext" | cut -d= -f1 | sed 's/^/  - /'
  exit 1
fi
