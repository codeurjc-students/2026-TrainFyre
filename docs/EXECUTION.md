# Execution

This document explains how to run TrainFyre with Docker, without installing Java or Node.js. To edit the code, see the [development guide](DEVELOPMENT.md).

## Requirements

- Windows and macOS: [Docker Desktop](https://docs.docker.com/desktop/)
- Linux: [Docker Engine](https://docs.docker.com/engine/install/) and the [Docker Compose plugin](https://docs.docker.com/compose/install/)
- [Git](https://git-scm.com/) to clone the repository
- [OpenSSL](https://www.openssl.org/) to create the local TLS certificate and the JWT keys (included in Git for Windows, macOS and most Linux distributions)

The application is served by HTTPS on port 443, so that port must be free on the host.

## Two ways to run the application

| | Published version | Development version |
| --- | --- | --- |
| Compose file | `docker/docker-compose.yml` | `docker/docker-compose-dev.yml` |
| Application image | `escipion48/trainfyre:<version>` from DockerHub | Built locally from the source code (tag `dev`) |
| Spring profile | `prod` | `dev` |
| Sample data | No | Yes (300 incidences and 3 users) |
| Database | PostgreSQL 18, data kept in a Docker volume | PostgreSQL 18, schema recreated on every start (`create-drop`) |
| Avatar storage | External S3-compatible service configured with `S3_*` variables | Garage container included in the Compose file |

## 1. Clone the repository

```bash
git clone https://github.com/codeurjc-students/2026-TrainFyre.git
cd 2026-TrainFyre
```

## 2. Create the local credentials

The application needs a TLS certificate (PKCS12 keystore) and a pair of RSA keys to sign the JWT access tokens. They are read from `backend/TrainFyre/secrets/`, a folder that is ignored by Git and must never be committed.

Choose a password for the keystore and use the same value later as `TRAINFYRE_TLS_PASSWORD`:

```bash
mkdir -p backend/TrainFyre/secrets
cd backend/TrainFyre/secrets

# Self-signed certificate for localhost, packed in a PKCS12 keystore
openssl req -x509 -newkey rsa:2048 -sha256 -nodes -days 365 \
  -keyout tls.key -out tls.crt -subj "/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1" \
  -addext "basicConstraints=critical,CA:TRUE"
openssl pkcs12 -export -in tls.crt -inkey tls.key \
  -out trainfyre-local.p12 -name trainfyre-local \
  -passout pass:CHANGE_ME
rm tls.key

# RSA key pair for the JWT access tokens
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl pkey -in jwt-private.pem -pubout -out jwt-public.pem

cd ../../..
```

The keystore alias must be `trainfyre-local`, as in the command above.

On Linux the container runs as user `10001`, so that user must be able to read the files:

```bash
sudo chown 10001:10001 backend/TrainFyre/secrets/trainfyre-local.p12 \
  backend/TrainFyre/secrets/jwt-private.pem backend/TrainFyre/secrets/jwt-public.pem
sudo chmod 0400 backend/TrainFyre/secrets/trainfyre-local.p12 \
  backend/TrainFyre/secrets/jwt-private.pem backend/TrainFyre/secrets/jwt-public.pem
```

## 3. Create the environment file

```bash
cp docker/.env.example docker/.env
```

Edit `docker/.env` and replace every `change-me` value. The file is not committed.

| Variable | Used by | Description |
| --- | --- | --- |
| `DB_PASSWORD` | both | Password of the PostgreSQL database |
| `TRAINFYRE_TLS_PASSWORD` | both | Password of the PKCS12 keystore created in step 2 |
| `DOCKERHUB_USERNAME` | both | DockerHub account that owns the image (default `escipion48`) |
| `IMAGE_TAG` | published | Version of the image to run (default `0.1.0`) |
| `GARAGE_DEFAULT_ACCESS_KEY`, `GARAGE_DEFAULT_SECRET_KEY`, `GARAGE_DEFAULT_BUCKET` | development | Credentials and bucket of the Garage container |
| `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_PATH_STYLE` | published | External S3-compatible storage for the avatars |

## 4a. Run the published version

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d
```

Docker downloads `escipion48/trainfyre:<IMAGE_TAG>` and `postgres:18-alpine`. The application waits until the database passes its health check before starting.

- **Latest version**: set `IMAGE_TAG=latest` in `docker/.env`. The `latest` tag is updated every time a release is published.
- **A specific version**: set `IMAGE_TAG=0.1.0` (or any published version).

This Compose file does not include an object storage service. To use avatars, point the `S3_*` variables to an S3-compatible service. Without it the rest of the application works but avatar operations fail.

The published version starts with an empty database: there is no sample data and no administrator account. The schema is created by Hibernate (`ddl-auto: update`) and the data is kept in the `trainfyre-db-data` volume between restarts.

## 4b. Run the development version with sample data

```bash
docker compose --env-file docker/.env -f docker/docker-compose-dev.yml up -d --build
```

This builds the image from the source code (the first build can take several minutes) and starts PostgreSQL, Garage and the application. The database schema is recreated every time the application starts and the sample data is loaded again.

## 5. Open the application

Open <https://localhost>. The certificate is self-signed, so the browser shows a warning that has to be accepted once.

The development version contains the following accounts, created from `backend/TrainFyre/src/main/resources/seed/users-postgresql.sql`. They are fictitious accounts with public credentials that exist only in the `dev` profile:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123456789` | `ADMIN` |
| `dev_alice` | `TrainFyre-dev-Alice-2026!` | `USER` |
| `dev_bob` | `TrainFyre-dev-Bob-2026!` | `USER` |

### Sample data

The development version loads 300 fictitious incidences distributed between 2026-09-03 and 2026-10-02 over three maps (`1` metro, `2` bus, `3` commuter rail) with invented line identifiers. The data comes from `seed/incidences-postgresql.sql` and covers every severity and cause. Maps and lines do not exist as entities, so their identifiers are only numbers.

With the published version, any person can create an account at `#/registro`, but no sample incidences exist and incidences can only be created by an administrator.

## Stop the application

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down        # published version
docker compose --env-file docker/.env -f docker/docker-compose-dev.yml down    # development version
```

Add `-v` to also delete the volumes (database and object storage).

## Deployment on an external server

Version 0.1 does not include a deployment specific to any hosting or cloud provider. The published Compose file can be run on any machine with Docker by following the same steps. See [CI/CD and releases](CI_CD.md) for how the images and Compose files are published.
