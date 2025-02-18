-- insert admin (username a, password aa)
INSERT INTO IWUser (id, enabled, roles, username, password, firstName, lastName, email)
VALUES (1, TRUE, 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 'Pepe', 'Pepito', 'pepe@mail.com');
INSERT INTO IWUser (id, enabled, roles, username, password, firstName, lastName, email)
VALUES (2, TRUE, 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W', 'Juan', 'Valdes', 'juan@mail.com');

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;
