CREATE DATABASE IF NOT EXISTS stationops
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE stationops;

CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    profil VARCHAR(50) NOT NULL,

    CONSTRAINT chk_utilisateur_profil
    CHECK (profil IN (
           'RESPONSABLE_EXPLOITATION',
           'AGENT_TERRAIN'
                     ))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS stations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS equipements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    etat VARCHAR(50) NOT NULL,
    station_id INT NOT NULL,

    CONSTRAINT fk_equipement_station
    FOREIGN KEY (station_id) REFERENCES stations(id),

    CONSTRAINT chk_equipement_type
    CHECK (type IN (
        'ASCENSEUR',
        'ESCALIER_MECANIQUE',
        'PORTIQUE',
        'DISTRIBUTEUR_TITRES',
        'ECRAN_INFORMATION'
    )),

    CONSTRAINT chk_equipement_etat
    CHECK (etat IN ('EN_SERVICE', 'HORS_SERVICE'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS prestataires (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telephone VARCHAR(30)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS incidents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(2000) NOT NULL,
    ouvert_le DATETIME(6) NOT NULL,
    statut VARCHAR(30) NOT NULL,
    resolu_le DATETIME(6),
    equipement_id INT NOT NULL,
    declarant_id INT NOT NULL,
    prestataire_id INT,

    CONSTRAINT fk_incident_equipement
        FOREIGN KEY (equipement_id) REFERENCES equipements(id),

    CONSTRAINT fk_incident_declarant
        FOREIGN KEY (declarant_id) REFERENCES utilisateurs(id),

    CONSTRAINT fk_incident_prestataire
        FOREIGN KEY (prestataire_id) REFERENCES prestataires(id),

    CONSTRAINT chk_incident_dates
        CHECK (resolu_le IS NULL OR resolu_le >= ouvert_le),

    CONSTRAINT chk_incident_coherence
        CHECK (
            (
                statut = 'NON_AFFECTE'
                AND prestataire_id IS NULL
                AND resolu_le IS NULL
            )
            OR
            (
                statut = 'AFFECTE'
                AND prestataire_id IS NOT NULL
                AND resolu_le IS NULL
            )
            OR
            (
                statut = 'RESOLU'
                AND resolu_le IS NOT NULL
            )
    ),

    INDEX idx_incident_statut_ouverture (statut, ouvert_le),
    INDEX idx_incident_resolution (resolu_le)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL UNIQUE,
    note INT NOT NULL,
    commentaire VARCHAR(2000),
    date_evaluation DATETIME(6) NOT NULL,

    CONSTRAINT fk_evaluation_incident
        FOREIGN KEY (incident_id) REFERENCES incidents(id),

    CONSTRAINT chk_evaluation_note
        CHECK (note BETWEEN 1 AND 5)
) ENGINE = InnoDB;