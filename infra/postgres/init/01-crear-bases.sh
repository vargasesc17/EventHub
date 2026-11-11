#!/bin/bash
# Crea una base de datos y un usuario por microservicio (ADR-0002).
# Cada usuario solo tiene permisos sobre su propia base: la separación la impone el motor.
# PostgreSQL ejecuta este script solo la primera vez, cuando el volumen está vacío.
# Si cambias algo aquí: docker compose down -v && docker compose up -d
set -euo pipefail

crear_base() {
  local base="$1" usuario="$2" clave="$3"
  echo "Creando $base para el usuario $usuario"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-SQL
    CREATE USER $usuario WITH PASSWORD '$clave';
    CREATE DATABASE $base OWNER $usuario;
    REVOKE ALL ON DATABASE $base FROM PUBLIC;
SQL
}

crear_base eventos_db        "$EVENTOS_DB_USER"        "$EVENTOS_DB_PASSWORD"
crear_base boleteria_db      "$BOLETERIA_DB_USER"      "$BOLETERIA_DB_PASSWORD"
crear_base notificaciones_db "$NOTIFICACIONES_DB_USER" "$NOTIFICACIONES_DB_PASSWORD"
