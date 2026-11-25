-- Lucrarea 8 - crearea bazei de date lab8 si a tabelelor persoane / excursii
CREATE DATABASE IF NOT EXISTS lab8;
USE lab8;

DROP TABLE IF EXISTS excursii;
DROP TABLE IF EXISTS persoane;

CREATE TABLE persoane (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nume    VARCHAR(45) NOT NULL,
    varsta  INT NOT NULL
);

CREATE TABLE excursii (
    id_excursie INT AUTO_INCREMENT PRIMARY KEY,
    id_persoana INT NOT NULL,
    destinatia  VARCHAR(45) NOT NULL,
    anul        INT NOT NULL,
    CONSTRAINT fk_excursii_persoane
        FOREIGN KEY (id_persoana) REFERENCES persoane(id)
        ON DELETE CASCADE
);
