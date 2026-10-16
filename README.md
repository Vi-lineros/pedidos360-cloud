# Innovatech Chile - Contenerización y Automatización (ISY1101)

## Arquitectura

La solución se compone de:

- `ms-ventas` (Spring Boot + JPA) expuesto en `8081`.
- `ms-despachos` (Spring Boot + JPA) expuesto en `8082`.
- `database` (MySQL 8.0) expuesto en `3306` con persistencia en volumen nombrado.
- `frontend` (Vite build + Nginx) expuesto en `80` (Nginx escucha en `8080` dentro del contenedor, ejecutándose como usuario no privilegiado).

Todos los contenedores se conectan por una red bridge interna: `network-innovatech`.

## Requisitos

- Docker Desktop / Docker Engine
- Docker Compose v2 (`docker compose`)

## Ejecución local (docker compose)

Desde la raíz del repositorio:

```bash
docker compose up --build
```

Accesos:

- Frontend: `http://localhost/`
- Ventas: `http://localhost:8081/`
- Despachos: `http://localhost:8082/`
- MySQL: `localhost:3306`

## Persistencia (IE3)

La base de datos usa un Named Volume:

- Volumen: `db_data`
- Montaje: `/var/lib/mysql`

Para reiniciar desde cero (borra datos persistidos):

```bash
docker compose down -v
```

## Imágenes y tags (para CI/CD)

El `docker-compose.yml` define `image` y `build` en los servicios de aplicación para soportar:

- Local: `docker compose up --build`
- Producción: `docker compose pull` + `docker compose up -d`

Variables:

- `IMAGE_PREFIX`: prefijo/namespace del registro (por ejemplo tu usuario de Docker Hub).
- `IMAGE_TAG`: tag a ejecutar (por defecto `latest`).

Ejemplo:

```bash
IMAGE_PREFIX=tuusuario IMAGE_TAG=latest docker compose up -d
```

## GitHub Actions (IE4)

Workflows:

- `.github/workflows/backend-ci-cd.yml`
- `.github/workflows/frontend-ci-cd.yml`

Se ejecutan en `push` a la rama `deploy`.

Secrets mínimos (Docker Hub + Deploy por SSH):

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `EC2_SSH_PRIVATE_KEY`
- `EC2_USERNAME`
- `EC2_HOST_FRONT`
- `EC2_HOST_BACK`

Secrets opcionales (AWS ECR en etapa build/push):

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `AWS_REGION`

Prerequisito del deploy por SSH:

- En la(s) instancia(s) EC2 debe existir el directorio `/opt/innovatech` con `docker-compose.yml` y Docker/Compose instalados.

## Guía AWS Academy (IE5–IE7)

Recomendación de Security Groups:

- Frontend (EC2 pública):
  - Inbound: `80/tcp` desde `0.0.0.0/0`
  - Inbound: `22/tcp` restringido a tu IP (administración)
- Backend (EC2 privada o aislada):
  - Inbound: `8081/tcp` y `8082/tcp` solo desde el Security Group del Frontend
  - Inbound: `22/tcp` restringido (o vía bastión si aplica)
- Base de datos:
  - Si es contenedor en la misma EC2 del backend, no expongas `3306` públicamente (deja el mapeo solo para local).
  - Si es RDS, permite `3306/tcp` solo desde el Security Group del Backend.

## Justificación técnica (IE1–IE3)

- Multi-stage build: reduce tamaño final de imagen y separa build/runtime.
- Non-root: `devopsuser` en backends y `nginx` en frontend reduce superficie de ataque.
- Named Volumes: mayor portabilidad y menor acoplamiento a rutas del host que bind mounts, evitando errores entre Windows/Linux y simplificando operación.
