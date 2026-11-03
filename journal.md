# Projet *StationOps*

Une entreprise exploite un réseau de transport composé de stations dans lesquelles sont installés différents
équipements : ascenseurs, escaliers mécaniques, portiques, distributeurs de titres de transport, écrans
d’information, etc.
Lorsqu’un équipement tombe en panne, un incident est déclaré. L’entreprise doit pouvoir suivre cet incident,
mesurer depuis combien de temps il est ouvert, l’affecter à un prestataire, enregistrer sa résolution et évaluer
ce prestataire.
Votre mission consiste à réaliser une application full stack permettant de gérer les équipements et les incidents,
mais surtout de fournir aux responsables d’exploitation une vision claire de la situation.

## README

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

Le jeu initial comprend trois stations, six équipements, deux prestataires,
six incidents et une évaluation. Les incidents comprennent quatre
non affectés, un affecté et un résolu.

Les dates sont calculées relativement à l’heure du premier remplissage.
Elles vieillissent ensuite normalement et ne sont pas recalculées
à chaque redémarrage.

## ARCHITECTURE

### Modèle de données

Les six entités JPA ont été définies : Utilisateur, Station,
Equipement, Incident, Prestataire et Evaluation.

Les énumérations Profil, StatutIncident, EtatEquipement et
TypeEquipement représentent les valeurs métier autorisées.
NiveauUrgence représente la présentation calculée des incidents ;
il n’est pas persisté comme une couleur en base.

| Entité source | Relation | Entité cible | Contrainte |
|---|---|---|---|
| Equipement | Plusieurs vers une | Station | Obligatoire |
| Incident | Plusieurs vers un | Equipement | Obligatoire |
| Incident | Plusieurs vers un | Utilisateur déclarant | Obligatoire |
| Incident | Plusieurs vers un | Prestataire | Facultatif |
| Evaluation | Une vers un | Incident | Obligatoire et unique |

Les relations sont portées par les entités qui contiennent les clés
étrangères, sans ajouter de collections inverses inutiles.

Le schéma SQL complet a été créé et exécuté sur MariaDB.
Le démarrage avec spring.jpa.hibernate.ddl-auto=validate a confirmé
la correspondance entre le schéma et les entités.
Les repositories sont également utilisés dans les tests sur H2.

## TESTS

Les vérifications manuelles ont confirmé le démarrage de l’application, la connexion à MariaDB et la détection du repository utilisateur.
Les deux comptes de démonstration ont pu se connecter après correction du problème d’initialisation du compte agent. L’absence de doublons lors des redémarrages a également été vérifiée.
Trois tests unitaires de ServiceAuthentificationTest ont été exécutés avec succès :

- Un responsable existant possède les informations et le rôle attendus.

- Un agent existant possède son rôle sans disposer du rôle responsable.

- Un email inconnu provoque une exception d’authentification.

Ces tests utilisent un repository simulé et ne nécessitent pas MariaDB. Ils vérifient le chargement des utilisateurs et de leurs rôles, mais pas le parcours HTTP complet.
Commande ciblée :

```bash
./mvnw -Dtest=ServiceAuthentificationTest test
```

Les tests HTTP de connexion, déconnexion et restrictions d’accès ont été réalisés dans SecuriteHttpTest. Les consultations REST et la résolution sont également testées dans ApiHttpTest.

### Authentification et sécurité — F1

Neuf tests HTTP automatisés ont été exécutés avec MockMvc,
la configuration Spring Security réelle et la base H2 du profil test.

Cas vérifiés :

- Page de connexion accessible sans authentification.

- Visiteur redirigé vers la connexion pour une page protégée.

- Connexion réussie avec des identifiants valides.

- Refus d’un mot de passe incorrect.

- Accès à la supervision interdit à l’agent.

- Accès à la supervision autorisé au responsable.

- Déconnexion avec invalidation de la session.

- Connexion refusée sans jeton CSRF.

- Déconnexion refusée sans jeton CSRF.

Commande :
```bash
./mvnw -Dtest=SecuriteHttpTest test
```

Résultat : 9 tests réussis, aucun échec.

Les comptes de test sont créés dans H2 et les transactions sont
annulées après chaque test. La base MariaDB n’est pas modifiée.

Les protections du périmètre REST réalisé sont couvertes par ApiHttpTest. Les autorisations des futures routes métier restent à vérifier lors de leur intégration.

### Validation manuelle des consultations — F2 et F3

Les parcours de consultation ont été vérifiés avec les profils
Responsable d’exploitation et Agent terrain.

- Affichage des stations et de leurs équipements.

- Affichage de la fiche équipement et de ses informations.

- Affichage des incidents en cours et résolus.

- Vérification du classement des incidents par date d’ouverture décroissante.

- Affichage d’un message lorsqu’un équipement ne possède aucun incident.

- Affichage d’une page 404 pour un équipement inexistant.

- Vérification des liens de retour.

Des données complémentaires ont été ajoutées manuellement dans la base
pour vérifier les cas sans incident et avec plusieurs incidents.
Ces ajouts ne font pas partie du script d’initialisation de démonstration.

Ces vérifications sont manuelles ; elles ne constituent pas des tests
HTTP automatisés.

### Supervision des incidents — F11

- Six tests unitaires du calcul d’urgence avec une horloge fixe :
  47 h, 48 h, juste avant 72 h, 72 h, incident affecté et incident résolu.

- Trois tests de repository sur H2 :
  tri de l’historique, exclusion des actifs de l’historique
  et exclusion des résolus de la supervision.

- Résultat : 9 tests réussis sur 9.

Un test de repository supplémentaire vérifie le comptage SQL natif des
incidents actifs d’un équipement, en excluant les incidents résolus et
ceux des autres équipements. Après l’adaptation des tests du service
et l’ajout de ce test, l’exécution complète avec ./mvnw test a réussi.
La vérification MariaDB après ce changement n’a pas encore été confirmée.

Vérifications manuelles :

- Affichage des incidents actifs et de leurs informations.

- Filtres par station et affectation, seuls et combinés.

- Indicateurs cohérents avec les résultats filtrés, y compris une liste vide.

- Accès responsable autorisé, accès agent refusé avec un statut HTTP 403
  et redirection vers la connexion pour un utilisateur déconnecté.

Chaque ligne de la supervision renvoie vers le détail de l’incident (F5).

### API REST et non-régression de la sécurité

Commande :
```bash
./mvnw -Dtest=ApiHttpTest,SecuriteHttpTest test
```

Résultat avant l’adaptation de la redirection des utilisateurs anonymes :
21 tests réussis, aucun échec. La relance après le remplacement du 401
par une redirection 302 reste à confirmer.

Les 12 tests API couvrent les cas suivants ; les deux assertions relatives
aux utilisateurs anonymes ont été adaptées au comportement 302 attendu :

- Les trois consultations JSON.

- Les erreurs 400 et 404.

- La redirection des consultations anonymes vers /login avec le statut 302.

- Le refus de résolution pour un agent.

- Le refus de résolution sans jeton CSRF.

- La redirection vers /login avec le statut 302 pour une résolution anonyme avec un jeton CSRF valide.

- La résolution par un responsable, avec passage de l’incident à RESOLU
  et remise en service de l’équipement sans autre incident actif.

- Le refus d’une seconde résolution avec une réponse 409.

Les 9 tests de sécurité web passent également.

Les tests utilisent H2 avec annulation des transactions après chaque test.
Ils ne modifient pas les données MariaDB.
Les droits des pages du cycle de vie des incidents sont testés dans IncidentWebTest.

### Cycle de vie des incidents — F4 à F10

Les règles sont testées à deux niveaux.

Tests unitaires avec Mockito et une horloge fixe (IncidentServiceTest, EvaluationServiceTest, EvaluationTest) :
- Un incident déclaré démarre sans prestataire et met l’équipement hors service.
- Un équipement inexistant ou un incident inexistant lève RessourceIntrouvableException.
- La description d’un incident résolu ne peut plus être modifiée.
- Un incident peut être affecté puis réaffecté, mais pas une fois résolu.
- La résolution enregistre l’heure de l’horloge et remet l’équipement en service seulement s’il n’a plus d’incident actif.
- La durée d’ouverture s’arrête à la résolution.
- Une évaluation est refusée si l’incident n’est pas résolu, si une évaluation existe déjà ou si la note sort de 1 à 5.
- L’historique contient la note quand l’incident est évalué.

Tests de repository sur H2 (IncidentRepositoryTest) : historique trié du plus récent au plus ancien, exclusion des incidents actifs, chargement de la station et du prestataire pour la page détail.

Tests HTTP avec MockMvc (IncidentWebTest, 18 tests) :
- Un incident inexistant affiche la page 404.
- Résoudre deux fois le même incident ou évaluer un incident non résolu affiche la page 409.
- Un agent reçoit 403 sur la résolution et sur l’évaluation.
- Une déclaration avec une description vide réaffiche le formulaire avec un message sous le champ.
- Les formulaires de déclaration, de modification et d’évaluation redirigent vers le détail après succès.
- L’historique et l’export CSV sont accessibles aux deux profils.

Commande :

```bash
./mvnw test
```

Non couvert : l’affichage visuel des pages, vérifié à la main dans le navigateur.

## CHOIX TECHNIQUES

### Droits par profil

| N° | Fonctionnalité | Responsable d’exploitation | Agent de terrain |
|---|---|---|---|
| F2–F3 | Consulter équipements et fiches | Accès complet | Accès complet |
| F4 | Déclarer un incident | Accès complet | Accès complet |
| F5–F6 | Consulter / modifier un incident | Accès complet | Accès complet (modification de la description tant que l’incident n’est pas résolu) |
| F7 | Affecter ou réaffecter un prestataire | Accès complet | Interdit |
| F8 | Déclarer un incident résolu | Accès complet | Interdit |
| F9 | Consulter l’historique | Accès complet | Lecture seule |
| F10 | Évaluer un prestataire | Accès complet | Lecture seule (la note est visible sur le détail et dans l’historique) |
| F11 | Tableau de bord | Accès complet | Interdit |

Les restrictions sont appliquées par Spring Security dans ConfigurationSecurite :
les POST /incidents/*/affectation et /incidents/*/resolution ainsi que /incidents/*/evaluation
sont réservés au rôle RESPONSABLE_EXPLOITATION. Les boutons sont aussi masqués pour l’agent,
mais ce n’est qu’un confort : un agent qui envoie la requête à la main reçoit 403.

### Ce qui est modifiable — F6

Seule la description d’un incident est modifiable, par les deux profils, tant que l’incident n’est pas résolu.
L’agent est souvent celui qui voit la panne sur place, il doit pouvoir préciser sa description.
L’équipement n’est pas modifiable : un incident déclaré sur le mauvais équipement a déjà mis cet
équipement hors service, le changer demanderait de recalculer l’état de deux équipements.
Le statut, le prestataire et les dates ne changent que par les actions dédiées (F7, F8), jamais par un formulaire libre.

### Authentification et sécurité

L’authentification repose sur Spring Security et une session HTTP.
UtilisateurRepository recherche un utilisateur par son email. ServiceAuthentification implémente UserDetailsService et fournit à Spring Security les informations nécessaires à l’authentification.
Les mots de passe sont hachés avec BCrypt avant leur enregistrement. L’application vérifie le mot de passe saisi avec un PasswordEncoder ; elle ne déchiffre pas le hash.
L’objet UtilisateurConnecte conserve l’identifiant, le prénom et le profil nécessaires aux écrans et aux futurs contrôles métier, sans conserver toute l’entité JPA.
Les routes /login et /error sont accessibles sans authentification. Les autres routes nécessitent une connexion. L’accès à /supervision et à ses sous-routes est réservé au responsable d’exploitation côté serveur.
La protection CSRF est conservée. La déconnexion utilise une requête POST, invalide la session, efface l’authentification et demande la suppression du cookie JSESSIONID.
Le profil Spring demo, qui active l’initialisation, est distinct des profils utilisateurs qui déterminent les droits.

### Interface de connexion

Une page Thymeleaf de connexion en français a été créée. Elle permet de saisir l’email et le mot de passe et affiche un message compréhensible en cas d’échec.
Après connexion, l’utilisateur est redirigé vers l’accueil. Celui-ci utilise un fragment d’en-tête réutilisable affichant le prénom, le profil et le bouton de déconnexion.
La page de supervision affiche les incidents actifs, leurs indicateurs d’urgence, des filtres par station et affectation, ainsi que des compteurs calculés sur les résultats filtrés. L’accès au détail d’un incident depuis ce tableau reste à intégrer.
Le lien de supervision est masqué pour l’agent. Cette adaptation de l’interface complète le contrôle serveur ; elle ne le remplace pas.

### Règles métier complémentaires

- Un équipement peut avoir plusieurs incidents actifs correspondant à des problèmes distincts.

- Dans notre modèle simplifié, un incident déclaré représente une panne bloquante et entraîne la mise hors service de l’équipement.

- L’équipement ne peut être remis en service que lorsqu’aucun incident actif ne subsiste.

- Une résolution sans prestataire est autorisée pour représenter une intervention interne ; aucune évaluation n’est alors possible.

- Après résolution, la description, l’équipement et le prestataire de l’incident ne sont plus modifiables.

- La réouverture et la suppression d’incidents ne sont pas prévues dans le socle.

- Le niveau d’urgence est calculé à partir de l’heure courante et n’est pas stocké comme une couleur en base.

Ces règles sont mises en œuvre dans le modèle et les services existants. La résolution et la cohérence de l’état de l’équipement sont couvertes par les tests. Elles sont accessibles dans les pages du cycle de vie des incidents (F4 à F10).

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
évaluations interdites. Le gestionnaire REST traduit les ressources
absentes en 404 et les transitions interdites en 409. Les paramètres
de type invalide produisent une réponse 400. Côté pages web,
GestionErreurWeb affiche la page 404 pour une ressource introuvable et une page
« Action impossible » avec le statut 409 pour une transition ou une évaluation interdite,
avec le message en français de l’exception et sans trace technique.

Les services assurent les opérations portant sur plusieurs entités,
notamment la déclaration et la résolution d’un incident. Les droits
d’accès à la supervision et à la résolution REST sont appliqués par
Spring Security. La modification de la description est ouverte
aux deux profils tant que l’incident n’est pas résolu (voir « Ce qui est modifiable — F6 »).

### Principes SOLID

La responsabilité unique est mise en œuvre dans la séparation entre accès aux données, authentification, configuration de sécurité et présentation des pages.
L’inversion des dépendances est illustrée par l’injection de l’interface UtilisateurRepository et de PasswordEncoder. Le repository peut ainsi être remplacé par une simulation pendant les tests.
Les autres principes SOLID seront analysés au fil de l’implémentation et justifiés dans le journal.

### Supervision

L’urgence est calculée à la consultation et n’est pas stockée en base.
Les seuils sont centralisés dans CalculUrgenceService, avec une horloge
injectée pour rendre les tests reproductibles.

Les incidents affectés ont une présentation bleue distincte.
Les styles sont regroupés dans un fichier CSS séparé.

Les filtres sont appliqués dans le service sur la liste des incidents actifs.
Les indicateurs décrivent uniquement les résultats filtrés.

Le calcul actuel utilise LocalDateTime et mesure une durée en heure locale ;
sa limite lors des changements d’heure reste identifiée.

### Requêtes et analyse critique du SQL natif

Les repositories combinent :
- Des requêtes dérivées du nom des méthodes, notamment findByEmail
  et findByEquipementIdOrderByOuvertLeDesc.
- Des requêtes JPQL explicites pour le détail, l’historique et la supervision.
- Une unique requête SQL native :
  IncidentRepository.compterIncidentsActifsParEquipement.

Cette requête native compte les incidents non résolus d’un équipement.
Le service de résolution utilise ce résultat pour remettre l’équipement
en service uniquement lorsqu’aucun incident actif ne subsiste.

Ce choix satisfait l’exigence d’utiliser une requête native dans une
opération métier réelle. La requête est courte et utilise du SQL standard.

Ses limites sont les suivantes :
- Elle dépend directement des noms des tables et colonnes.
- La valeur RESOLU est écrite dans la requête : un changement de cette
  valeur dans le modèle nécessite également une modification du SQL.
- Sa syntaxe n’est pas vérifiée comme du JPQL à partir des entités.
- Une requête dérivée aurait également répondu au besoin.
  Aucun gain de performance particulier n’est revendiqué.

Un test de repository sur H2 vérifie que seuls les incidents actifs
de l’équipement demandé sont comptés. Les tests API vérifient également
la résolution et la remise en service de l’équipement.

### Utilisation de @Transactional

Les transactions sont placées sur les opérations métier des services,
plutôt que dans les contrôleurs.

| Emplacement | Justification |
|---|---|
| IncidentService.declarerIncident | La création de l’incident et la mise hors service de l’équipement appartiennent à la même transaction. |
| IncidentService.modifierDescription | L’incident reste géré par JPA pendant la modification ; Hibernate détecte et enregistre le changement. |
| IncidentService.affecterPrestataire | La recherche du prestataire et la modification de l’incident sont regroupées dans une transaction. |
| IncidentService.resoudreIncident | La résolution, le comptage des incidents actifs et l’éventuelle remise en service de l’équipement sont regroupés dans une transaction. |
| EvaluationService.evaluerPrestataire | La vérification de l’incident, le contrôle d’une évaluation existante et la création de l’évaluation sont regroupés. L’unicité est également protégée par la base. |
| StationService | readOnly = true délimite les opérations de consultation. |
| EquipementService | readOnly = true permet notamment de préparer la fiche et ses DTO dans une transaction de lecture. |
| SupervisionService | readOnly = true encadre la récupération des incidents et la préparation des données de supervision. |
| InitialisationUtilisateurs.run | Les créations des comptes absents sont regroupées dans une transaction. |
| InitialisationDonneesDemo.run | La création des données métier de démonstration est regroupée pour éviter une initialisation partielle en cas d’échec. |

Dans les transactions de modification, Hibernate détecte les changements
des entités gérées : un appel save supplémentaire n’est pas nécessaire
pour chaque modification.

readOnly = true exprime une intention de lecture et permet des
optimisations ; ce n’est pas une protection d’accès utilisateur.

Les exceptions métier utilisées héritent de RuntimeException.
Par défaut, lorsqu’une telle exception sort de la méthode transactionnelle,
Spring annule la transaction.

Les transactions contribuent au respect de RG10 : les modifications
regroupées dans une opération sont validées ensemble ou annulées
en cas d’échec entraînant un rollback.

Aucun test de panne simulée n’a été ajouté pour démontrer le rollback.
Les tests existants vérifient les résultats métier et les opérations refusées.
Une transaction ne garantit pas, à elle seule, l’absence de tous les
conflits entre opérations concurrentes.

### Contrat de l’API REST — F12

L’API utilise le préfixe /api et échange des données JSON.
Le contrat commun couvre les consultations et les fonctionnalités
de gestion des incidents développées par le binôme.

Les routes ci-dessous constituent le contrat prévu.
Elles ne sont pas toutes implémentées à ce stade.

| Méthode | Route | Fonction | Accès prévu |
|---|---|---|---|
| GET | /api/stations | Lister les stations | Les deux profils |
| GET | /api/stations/{id}/equipements | Lister les équipements d’une station | Les deux profils |
| GET | /api/equipements/{id} | Consulter la fiche équipement et ses incidents | Les deux profils |
| POST | /api/equipements/{id}/incidents | Déclarer un incident — F4 | Les deux profils |
| GET | /api/incidents/{id} | Consulter le détail d’un incident — F5 | Les deux profils |
| PATCH | /api/incidents/{id}/description | Modifier la description — F6 | Selon le déclarant, le profil et le statut |
| PUT | /api/incidents/{id}/prestataire | Affecter ou réaffecter un prestataire — F7 | Responsable |
| POST | /api/incidents/{id}/resolution | Résoudre un incident — F8 | Responsable |
| GET | /api/incidents/historique | Consulter les incidents résolus — F9 | Les deux profils |
| POST | /api/incidents/{id}/evaluation | Évaluer le prestataire — F10 | Responsable |
| GET | /api/prestataires | Lister les prestataires pour l’affectation | Responsable |

#### Données reçues

- Déclaration : description de l’incident.
  L’équipement est identifié dans l’URL et le déclarant est récupéré
  depuis la session authentifiée.

- Modification : nouvelle description.

- Affectation ou réaffectation : identifiant du prestataire.

- Résolution : aucun corps de requête ; la date est déterminée côté serveur.

- Évaluation : note entière de 1 à 5 et commentaire facultatif.

#### Droits et règles métier

Un agent peut modifier uniquement la description de ses propres incidents
tant qu’ils sont non affectés.

Le responsable peut modifier la description de tout incident non résolu.
L’affectation, la réaffectation, la résolution et l’évaluation lui sont réservées.

Les autorisations du périmètre réalisé sont vérifiées côté serveur.
Les droits décrits pour les autres routes constituent le contrat prévu
et restent à appliquer et à tester lors de leur réalisation.
Les contrôleurs REST réutilisent les services métier utilisés ou prévus
pour les pages Thymeleaf pour éviter de dupliquer les règles et les transactions.

#### Réponses et erreurs

Les réponses exposent des DTO, sans sérialiser directement les entités JPA.

Statuts prévus :

- 200 : consultation réussie.

- 201 : création d’un incident ou d’une évaluation.

- 204 : modification, affectation ou résolution réussie, sans corps de réponse.

- 400 : paramètre ou donnée invalide.

- 302 : utilisateur non connecté, redirection vers /login.

- 403 : droits insuffisants ou jeton CSRF absent ou invalide.

- 404 : ressource inexistante.

- 409 : opération incompatible avec l’état métier de la ressource.

Les erreurs de l’API sont renvoyées en JSON avec un message en français.
Leur gestion est distincte des pages d’erreur HTML.
Les champs des DTO et le format commun des erreurs du périmètre
REST réalisé sont définis dans les sous-parties suivantes.

#### Authentification

L’authentification repose sur la session Spring Security.
La protection CSRF est conservée pour les opérations de modification.

#### Articulation avec les pages web et périmètre F12

Les routes Thymeleaf et les routes REST sont distinctes.
Une page /incidents/{id} produit du HTML, tandis que
/api/incidents/{id} produit du JSON.

Le périmètre REST actuellement réalisé comprend :

- La liste des stations.

- Les équipements d’une station.

- La fiche d’un équipement avec ses incidents.

- La résolution d’un incident par le responsable.

Les autres routes constituent le contrat commun prévu pour une extension
de l’API. Leur présence dans ce document ne signifie pas qu’elles sont réalisées.

#### Réponses JSON du périmètre initial F12

Les identifiants sont des nombres entiers.
Les valeurs des énumérations sont transmises sous leur nom Java.
Les dates utilisent le format ISO 8601 sans fuseau, conformément
au modèle actuel en LocalDateTime : 2026-11-02T14:30:00.

##### `GET /api/stations`

Retourne un tableau de stations triées par nom croissant.
Chaque station contient :

- id : identifiant.

- nom : nom de la station.

Une liste vide retourne [] avec le statut 200.

##### `GET /api/stations/{id}/equipements`

Retourne les équipements de la station, triés par référence croissante.
Chaque équipement contient :

- id : identifiant.

- reference : référence unique.

- libelle : libellé.

- type : valeur de TypeEquipement.

- etat : valeur de EtatEquipement.

Une station existante sans équipement retourne [] avec le statut 200.
Une station inexistante retourne une erreur 404.

##### `GET /api/equipements/{id}`

Retourne une fiche contenant :

- id : identifiant de l’équipement.

- reference : référence unique.

- libelle : libellé.

- type : valeur de TypeEquipement.

- etat : valeur de EtatEquipement.

- stationId : identifiant de la station.

- stationNom : nom de la station.

- incidents : tableau des incidents, triés par ouverture décroissante.

Chaque incident contient :

- id : identifiant.

- description : description.

- statut : NON_AFFECTE, AFFECTE ou RESOLU.

- ouvertLe : date d’ouverture.

- resoluLe : date de résolution, ou null si l’incident n’est pas résolu.

Un équipement sans incident contient un tableau incidents vide.
Un équipement inexistant retourne une erreur 404.

##### `POST /api/incidents/{id}/resolution`

La requête ne contient pas de corps.
Elle nécessite une session responsable et un jeton CSRF valide.

La résolution utilise la date du serveur et réutilise le service métier.
L’équipement est remis en service uniquement s’il ne reste aucun
incident actif.

Une résolution réussie retourne le statut 204, sans corps.
Un incident inexistant retourne 404.
Un incident déjà résolu retourne 409.

#### Format commun des erreurs JSON

Les erreurs du périmètre initial exposent :

- statut : code HTTP numérique.

- message : explication en français.

Exemple pour une ressource absente :

```json
{
  "statut": 404,
  "message": "Aucun équipement ne correspond à l’identifiant 999999"
}
```

Le champ statut correspond au statut HTTP de la réponse.
Les réponses ne contiennent ni trace technique ni mot de passe.

Les erreurs de sécurité sont également traitées au niveau
des filtres Spring Security : un utilisateur non connecté est redirigé
vers /login avec le statut 302 ; les refus d’accès 403 produisent du JSON
sur les routes /api.

Une requête de modification sans jeton CSRF valide peut être refusée
avec le statut 403 avant la vérification de l’authentification.
La redirection vers /login avec le statut 302 est attendue sur une
consultation anonyme et sur une modification anonyme munie d’un jeton CSRF valide.
Les tests correspondants ont été adaptés ; leur nouvelle exécution reste à confirmer.

## LOT ALPHA

TBD

### Export CSV de l’historique — Tim HRDY

#### Objectif

Permettre au responsable de récupérer l’historique des incidents résolus
dans un tableur, par exemple pour un bilan mensuel avec un prestataire.

#### Réalisation

Un lien « Télécharger l’historique (CSV) » sur la page historique appelle
GET /incidents/historique/export. Le fichier contient une ligne par incident
résolu, dans le même ordre que la page (plus récent d’abord), avec la station,
l’équipement, la description, les dates d’ouverture et de résolution,
le prestataire et la note.

#### Choix techniques

- Le séparateur est le point-virgule, que Excel en français ouvre directement en colonnes.
- Une valeur qui contient un point-virgule ou des guillemets est mise entre guillemets,
  et les guillemets intérieurs sont doublés, comme le prévoit le format CSV.
- Le fichier commence par un BOM UTF-8 pour que Excel affiche correctement les accents.
- La construction du fichier est dans ExportCsvService, le contrôleur ne fait que l’envoyer.
- Le contrôleur renvoie un ResponseEntity car il faut régler deux en-têtes :
  le type text/csv et le nom du fichier téléchargé (Content-Disposition).
- Aucune bibliothèque ajoutée : le format reste simple et un StringBuilder suffit.

#### Essais et résultats

ExportCsvServiceTest vérifie l’en-tête, le format des dates et l’échappement
d’une description contenant un point-virgule et des guillemets.
IncidentWebTest vérifie que le téléchargement renvoie un fichier text/csv.

#### Limites

L’export reprend tout l’historique, sans filtre par période ou par station.

## BILAN

Développeurs:

- Yoni ATLAN

- Tim HRDY
