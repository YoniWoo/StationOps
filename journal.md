# Projet *StationOps*

Une entreprise exploite un réseau de transport composé de stations dans lesquelles sont installés différents
équipements : ascenseurs, escaliers mécaniques, portiques, distributeurs de titres de transport, écrans
d’information, etc.
Lorsqu’un équipement tombe en panne, un incident est déclaré. L’entreprise doit pouvoir suivre cet incident,
mesurer depuis combien de temps il est ouvert, l’affecter à un prestataire, enregistrer sa résolution et évaluer
ce prestataire.
Votre mission consiste à réaliser une application full stack permettant de gérer les équipements et les incidents,
mais surtout de fournir aux responsables d’exploitation une vision claire de la situation.

## ReadMe

Le projet StationOps utilise Java 21, Spring Boot 4.1.1, Maven, Thymeleaf et MariaDB. Le développement est réalisé sur macOS avec IntelliJ IDEA. La base locale stationops est accessible via le serveur fourni par XAMPP.
Les paramètres de connexion sont externalisés dans les variables d’environnement DB_URL, DB_USERNAME et DB_PASSWORD. Un fichier .env.example décrit les variables nécessaires sans contenir de secrets. Les variables sont actuellement renseignées dans la configuration de lancement IntelliJ ; le fichier .env n’est pas chargé automatiquement.
Le profil Spring demo active la création des comptes de démonstration. Les mots de passe initiaux sont fournis par DEMO_RESPONSABLE_PASSWORD et DEMO_AGENT_PASSWORD.
Deux comptes sont disponibles :
- responsable@stationops.test : responsable d’exploitation.
- agent@stationops.test : agent de terrain.

Les mots de passe ne sont pas inscrits dans le dépôt. L’initialisation crée uniquement les comptes absents : une modification des variables d’environnement ne remplace pas le mot de passe d’un compte existant.
Limite technique identifiée : la version MariaDB fournie par XAMPP est la 10.4.28. Hibernate signale un minimum pris en charge de 10.6. Le démarrage fonctionne, mais une version compatible devra être utilisée pour la validation finale.

### Initialisation de la base

1. Démarrer MariaDB.
2. Exécuter le fichier `db/schema.sql`.
3. Configurer les variables DB_URL, DB_USERNAME et DB_PASSWORD.
4. Pour charger la démonstration, activer le profil Spring `demo`
   et renseigner DEMO_RESPONSABLE_PASSWORD et DEMO_AGENT_PASSWORD.
5. Démarrer l’application.

Hibernate vérifie le schéma avec `spring.jpa.hibernate.ddl-auto=validate`
sans le modifier.

Le profil `demo` crée les utilisateurs absents, puis initialise les
données métier dans une transaction si les tables métier sont vides.
Il ne supprime ni ne remplace les données existantes.

Le jeu initial comprend trois stations, six équipements, un prestataire,
six incidents et une évaluation. Les incidents comprennent quatre
non affectés, un affecté et un résolu.

Les dates sont calculées relativement à l’heure du premier remplissage.
Elles vieillissent ensuite normalement et ne sont pas recalculées
à chaque redémarrage.

## Architecture

### Modèle de données

Les six entités JPA ont été définies : Utilisateur, Station,
Equipement, Incident, Prestataire et Evaluation.

Les énumérations Profil, StatutIncident, EtatEquipement et
TypeEquipement représentent les valeurs métier autorisées.

| Entité source | Relation | Entité cible | Contrainte |
|---|---|---|---|
| Equipement | Plusieurs vers une | Station | Obligatoire |
| Incident | Plusieurs vers un | Equipement | Obligatoire |
| Incident | Plusieurs vers un | Utilisateur déclarant | Obligatoire |
| Incident | Plusieurs vers un | Prestataire | Facultatif |
| Evaluation | Une vers un | Incident | Obligatoire et unique |

Les relations sont portées par les entités qui contiennent les clés
étrangères, sans ajouter de collections inverses inutiles.

La définition des entités ne constitue pas encore une validation de
la persistance : le schéma SQL complet reste à créer et à vérifier.

## Test

Les vérifications manuelles ont confirmé le démarrage de l’application, la connexion à MariaDB et la détection du repository utilisateur.
Les deux comptes de démonstration ont pu se connecter après correction du problème d’initialisation du compte agent. L’absence de doublons lors des redémarrages a également été vérifiée.
Trois tests unitaires de ServiceAuthentificationTest ont été exécutés avec succès :

- Un responsable existant possède les informations et le rôle attendus.
- Un agent existant possède son rôle sans disposer du rôle responsable.
- Un email inconnu provoque une exception d’authentification.

Ces tests utilisent un repository simulé et ne nécessitent pas MariaDB. Ils vérifient le chargement des utilisateurs et de leurs rôles, mais pas le parcours HTTP complet.
Commande ciblée :

- ./mvnw -Dtest=ServiceAuthentificationTest test

- Les tests HTTP de connexion, déconnexion et restrictions d’accès ont été reportés. Les protections de la future API restent également à vérifier.

## Choix Technique - Authentification et sécurité

L’authentification repose sur Spring Security et une session HTTP.
UtilisateurRepository recherche un utilisateur par son email. ServiceAuthentification implémente UserDetailsService et fournit à Spring Security les informations nécessaires à l’authentification.
Les mots de passe sont hachés avec BCrypt avant leur enregistrement. L’application vérifie le mot de passe saisi avec un PasswordEncoder ; elle ne déchiffre pas le hash.
L’objet UtilisateurConnecte conserve l’identifiant, le prénom et le profil nécessaires aux écrans et aux futurs contrôles métier, sans conserver toute l’entité JPA.
Les routes /login et /error sont accessibles sans authentification. Les autres routes nécessitent une connexion. L’accès à /supervision et à ses sous-routes est réservé au responsable d’exploitation côté serveur.
La protection CSRF est conservée. La déconnexion utilise une requête POST, invalide la session, efface l’authentification et demande la suppression du cookie JSESSIONID.
Le profil Spring demo, qui active l’initialisation, est distinct des profils utilisateurs qui déterminent les droits.

## Choix Technique - Interface de connexion

Une page Thymeleaf de connexion en français a été créée. Elle permet de saisir l’email et le mot de passe et affiche un message compréhensible en cas d’échec.
Après connexion, l’utilisateur est redirigé vers l’accueil. Celui-ci utilise un fragment d’en-tête réutilisable affichant le prénom, le profil et le bouton de déconnexion.
Une page de supervision provisoire a été ajoutée pour préparer F11 et contrôler les accès. Elle ne contient pas encore le tableau de bord des incidents.
Le lien de supervision est masqué pour l’agent. Cette adaptation de l’interface complète le contrôle serveur ; elle ne le remplace pas.

## Choix Technique - Règles métier complémentaires

- Un équipement peut avoir plusieurs incidents actifs correspondant à des problèmes distincts.
- Dans notre modèle simplifié, un incident déclaré représente une panne bloquante et entraîne la mise hors service de l’équipement.
- L’équipement ne peut être remis en service que lorsqu’aucun incident actif ne subsiste.
- Une résolution sans prestataire est autorisée pour représenter une intervention interne ; aucune évaluation n’est alors possible.
- Après résolution, la description, l’équipement et le prestataire de l’incident ne sont plus modifiables.
- La réouverture et la suppression d’incidents ne sont pas prévues dans le socle.
- Le niveau d’urgence sera calculé à partir de l’heure courante et ne sera pas stocké comme une couleur en base.

Ces règles sont retenues pour la conception ; leur implémentation complète reste à réaliser.

## Choix technique - Protection de la cohérence métier

### Protection de la cohérence métier

L’entité Incident expose des méthodes pour modifier sa description,
affecter un prestataire et enregistrer sa résolution, plutôt que
des setters permettant de changer librement son statut.

Un incident résolu ne peut plus être modifié ou réaffecté.
La résolution ne peut pas précéder son ouverture.

Une évaluation nécessite un incident résolu associé à un prestataire.
Sa note est comprise entre 1 et 5. Par choix de cohérence,
sa date ne peut pas précéder la résolution.

Des exceptions personnalisées signalent les transitions et
évaluations interdites. Leur traduction en réponses HTTP reste
à mettre en place.

Les contrôles de droits et les opérations portant sur plusieurs
entités seront réalisés dans les services.

## Choix Technique - Principes SOLID

La responsabilité unique est mise en œuvre dans la séparation entre accès aux données, authentification, configuration de sécurité et présentation des pages.
L’inversion des dépendances est illustrée par l’injection de l’interface UtilisateurRepository et de PasswordEncoder. Le repository peut ainsi être remplacé par une simulation pendant les tests.
Les autres principes SOLID seront analysés au fil de l’implémentation et justifiés dans le journal.



## Lot Alpha

TBD

## Bilan

Développeurs:
- Yoni ATLAN
- Tim HRDY