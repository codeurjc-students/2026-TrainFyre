-- TrainFyre: SOLO DESARROLLO. Cuentas ficticias con credenciales publicas.
-- dev_alice / TrainFyre-dev-Alice-2026!
-- dev_bob   / TrainFyre-dev-Bob-2026!
-- Requiere esquema vacio generado por Hibernate (ddl-auto=create-drop).
-- Argon2id v=19, m=16384 KiB, t=2, p=1; salt=16 bytes, hash=32 bytes.
-- Para Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(), sin prefijo {argon2}.
-- No crea roles ni suscripciones. version=0 para las entidades persistidas.

INSERT INTO users (id, version, username, password)
VALUES
    ('11111111-1111-4111-8111-111111111111', 0, 'dev_alice', '$argon2id$v=19$m=16384,t=2,p=1$j0OP+2ktYB9WN3USUzXAJQ$6WR96wyMp9uC6/W12yxEDtRbVqrGBbu62jr+opm/VNg'),
    ('22222222-2222-4222-8222-222222222222', 0, 'dev_bob', '$argon2id$v=19$m=16384,t=2,p=1$uaXOBSR/ZdP82f0ua7JQ3w$7lK+sN6TjmUlsjRUo42ixm17CxSBBslw8K8vGgJiUxM');
