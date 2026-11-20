#!/bin/sh
# Cles JWT : JWT_PRIVATE_PEM_B64 / JWT_PUBLIC_PEM_B64 si fournies, sinon paire RSA
# generee au demarrage (suffisant pour la demo : la base H2 repart aussi de zero).
K=/app/keys
if [ -n "$JWT_PRIVATE_PEM_B64" ] && [ -n "$JWT_PUBLIC_PEM_B64" ]; then
  echo "$JWT_PRIVATE_PEM_B64" | base64 -d > $K/jwt-private.pem
  echo "$JWT_PUBLIC_PEM_B64" | base64 -d > $K/jwt-public.pem
else
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out $K/jwt-private.pem 2>/dev/null
  openssl pkey -in $K/jwt-private.pem -pubout -out $K/jwt-public.pem
fi
export APP_JWT_PRIVATE_KEY_LOCATION=file:$K/jwt-private.pem
export APP_JWT_PUBLIC_KEY_LOCATION=file:$K/jwt-public.pem
exec java $JAVA_OPTS -jar app.jar
