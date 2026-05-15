# Dossier Professionnel — Concepteur Développeur d'Applications

**Candidat** : Florian Chague
**Centre de formation** : ADRAR Ramonville-Saint-Agne
**Promotion** : 2025-2026

> Note de rédaction : ce document est conçu pour être copié-collé dans le template Word officiel du DP. Chaque section ci-dessous correspond à une cellule de la fiche d'exemple de pratique professionnelle (5 questions standardisées par exemple).

---

## Activité-type 1 — Développer une application sécurisée

### Exemple n°1 — CP 1 : Installer et configurer son environnement de travail en fonction du projet

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Dans le cadre du module Symfony de la formation CDA, j'ai eu à mettre en place un environnement de développement web complet pour un projet de gestion d'articles de blog (entités Article, Category, User avec leurs relations). La consigne était de partir d'un poste vierge et d'arriver à un environnement opérationnel permettant d'écrire du code, de migrer un schéma de base de données et de lancer des fixtures de jeu d'essai.

Les opérations que j'ai effectuées ont été les suivantes : installation de PHP 8.2 et de l'extension `pdo_mysql`, installation de Composer pour la gestion des dépendances PHP, installation de la Symfony CLI pour piloter le serveur de développement et générer des squelettes via le maker bundle, installation d'un MySQL local via MAMP, création du fichier `.env.local` avec la `DATABASE_URL` correspondant au serveur local, installation des dépendances du projet via `composer install`, lancement des 15 migrations Doctrine pour matérialiser le schéma, et exécution des fixtures Faker pour peupler la base de 20 utilisateurs, 8 catégories et 100 articles.

Le contrôle de bonne installation s'est fait en lançant `symfony server:start` et en vérifiant que les routes `/articles`, `/categories`, `/users` répondaient correctement avec les données seed.

#### 2. Précisez les moyens utilisés

- **Langage et runtime** : PHP 8.2
- **Gestionnaire de dépendances** : Composer
- **Framework** : Symfony 7.4 (framework-bundle, twig-bundle, dotenv, console, yaml)
- **ORM** : Doctrine ORM 3.6, Doctrine DBAL, doctrine-bundle 2.18, doctrine-migrations-bundle 3.7
- **Base de données locale** : MySQL via MAMP
- **Outillage CLI** : Symfony CLI, symfony/maker-bundle 1.65
- **Données de test** : fakerphp/faker 1.24 + doctrine-fixtures-bundle 4.3
- **IDE** : Visual Studio Code avec extensions PHP Intelephense, Symfony Support, Doctrine ORM Support
- **Versionnement** : Git, dépôt distant sur GitHub (`IztochenValk/yoann-symfony`)
- **Documentation** : documentation officielle Symfony 7.4, documentation Doctrine ORM 3, supports de cours ADRAR

#### 3. Avec qui avez-vous travaillé ?

Travail en autonomie, avec l'appui du formateur référent du module Symfony de l'ADRAR pour les questions de configuration spécifiques (résolution d'un conflit de version PHP entre MAMP et le PHP système, configuration de la `DATABASE_URL` au format Doctrine `mysql://user:pass@host/db?serverVersion=...`).

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle, Ramonville-Saint-Agne
- **Chantier, atelier, service** : Module Symfony — formation Concepteur Développeur d'Applications, promotion 2025-2026
- **Période d'exercice** : Février 2026

#### 5. Informations complémentaires (facultatif)

L'environnement mis en place a été réutilisé sur l'ensemble des séances du module Symfony qui ont suivi, ce qui a validé la solidité de la configuration initiale. La séparation entre `.env` (commité) et `.env.local` (gitignored) a permis de partager la configuration générique du projet via Git sans exposer mes identifiants MySQL locaux.

---

### Exemple n°2 — CP 2 : Développer des interfaces utilisateurs

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

En tant que développeur JavaScript chez Pontica Solutions à Varna (Bulgarie), j'ai conçu et développé un script utilisateur (userscript) appelé **Highlight Intercom**, dont la mission était d'améliorer l'ergonomie de l'interface support Intercom utilisée quotidiennement par les agents du produit Sutart. La problématique métier identifiée : sur l'inbox Intercom, un agent qui jonglait entre une vingtaine de conversations actives n'avait aucun moyen visuel de distinguer rapidement les chats où il s'était déjà engagé des chats nouveaux. Le résultat était une perte de temps et des messages doublonnés involontaires.

J'ai développé une surcouche injectée directement dans l'interface Intercom (qui est une Single Page Application que je ne contrôlais pas) via le mécanisme des userscripts Tampermonkey. Le script injecte deux éléments d'interface dans le header de l'inbox : un sélecteur de couleur (Spectrum.js) permettant à chaque agent de choisir sa couleur de surlignage personnelle, et un bouton de reset (icône poubelle) pour réinitialiser le tracking. Il colorise ensuite en temps réel les conversations de la liste auxquelles l'agent connecté a déjà participé, en s'appuyant sur les routes hash de la SPA et sur le DOM observé via `DOMSubtreeModified` et `ajaxComplete`.

Le challenge technique majeur a été l'observation du DOM d'une application tierce qui se ré-écrit à chaque navigation interne : il a fallu mettre en place un mécanisme de polling adaptatif (setTimeout récursif limité par la présence des sélecteurs cibles) pour détecter les remontages d'éléments sans saturer le navigateur.

#### 2. Précisez les moyens utilisés

- **Langages** : JavaScript (vanilla), HTML, CSS
- **Bibliothèques** : jQuery 3, jQuery.address (parsing d'URL), Spectrum.js 1.8 (color picker)
- **APIs** : DOM API, MutationObserver-like via `DOMSubtreeModified`, événements `hashchange`, `ajaxComplete`
- **Persistance locale** : `localStorage` du navigateur pour la liste des conversations engagées par l'agent et sa couleur préférée
- **Mécanismes de distribution** : userscripts Tampermonkey / Greasemonkey / Violentmonkey, métadonnées `@match`, `@require`, `@resource`, `@grant GM_addStyle`, `@grant GM_getResourceText`
- **Plateforme de publication** : OpenUserJS (https://openuserjs.org), license MIT
- **Source de référence** : documentation Intercom (sélecteurs CSS de l'interface admin), documentation Spectrum, documentation Tampermonkey
- **Outils de débogage** : Chrome DevTools, console réseau pour suivre les requêtes XHR Intercom

#### 3. Avec qui avez-vous travaillé ?

En autonomie technique, en collaboration directe avec une dizaine d'agents support de Pontica Solutions à qui j'ai présenté le script en démo. J'ai recueilli leurs retours d'usage (notamment sur la couleur par défaut trop saturée, ce qui a motivé l'intégration du color picker), et ajusté le script en conséquence. Distribution ensuite via l'URL d'install OpenUserJS partagée en interne.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : Pontica Solutions, Varna, Bulgarie
- **Chantier, atelier, service** : Équipe support technique du produit Sutart, outillage des agents
- **Période d'exercice** : 2020 – 2021

#### 5. Informations complémentaires (facultatif)

Le script est toujours en ligne et accessible publiquement sur OpenUserJS (https://openuserjs.org/scripts/Ayanokoji111/Highlight_Intercom) avec **172 installations cumulées** au moment de la rédaction de ce dossier. C'est un projet personnel à l'origine, qui a démontré sa valeur en interne avant d'être ouvert à la communauté plus large des agents support d'autres entreprises utilisant Intercom.

---

### Exemple n°3 — CP 3 : Développer des composants métier

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Dans le projet Symfony de la formation ADRAR (application de blog), j'ai développé les composants métier permettant la gestion d'articles publiés par des utilisateurs, avec association à plusieurs catégories. Le périmètre fonctionnel couvert : modèle d'entité Article avec ses attributs (titre, contenu, image, dates de création / mise à jour / publication), relation many-to-one vers l'utilisateur auteur (`writeBy`), relation many-to-many avec les catégories, contrôleur de liste triant par date de publication décroissante, contrôleur de détail récupérant un article par son identifiant via le mécanisme de paramètre converters de Symfony.

J'ai implémenté la logique de gestion des catégories côté entité Article : les méthodes `addCategory`, `addCategories` (pour ajouter un lot en une seule opération), `removeCategory`, avec la symétrie inversée gérée automatiquement (l'ajout d'une catégorie à un article ajoute aussi l'article à la catégorie pour maintenir la cohérence côté ORM Doctrine). J'ai également développé le data fixture Faker qui peuple la base avec 20 utilisateurs, 8 catégories et 100 articles, chaque article ayant entre 1 et 3 catégories tirées aléatoirement, ce qui crée un graphe relationnel réaliste pour le développement.

#### 2. Précisez les moyens utilisés

- **Framework** : Symfony 7.4 (FrameworkBundle, TwigBundle, Routing avec attributs PHP 8 `#[Route]`)
- **ORM** : Doctrine ORM 3.6 avec annotations / attributs PHP `#[ORM\Entity]`, `#[ORM\ManyToOne]`, `#[ORM\ManyToMany]`, `#[ORM\Column]`
- **Architecture** : pattern MVC, Repositories Symfony (`ArticleRepository`, `CategoryRepository`, `UserRepository`) étendant `ServiceEntityRepository`
- **Données de test** : Faker (factory FR_FR) intégré via DoctrineFixturesBundle pour générer un jeu de données cohérent
- **Templating** : Twig 3 pour le rendu des pages de liste et de détail
- **Outillage** : symfony/maker-bundle pour la génération de squelettes d'entités et de contrôleurs

#### 3. Avec qui avez-vous travaillé ?

Travail individuel sur le sujet pédagogique distribué par le formateur du module Symfony. Échanges techniques en cours de séance avec deux autres apprenants de la promotion qui suivaient le même sujet (notamment sur la subtilité du `inversedBy` des relations bidirectionnelles, qui change la table propriétaire de la relation).

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle, Ramonville-Saint-Agne
- **Chantier, atelier, service** : Module Symfony — formation CDA, promotion 2025-2026
- **Période d'exercice** : Février 2026

#### 5. Informations complémentaires (facultatif)

Le code source est consultable sur GitHub : https://github.com/IztochenValk/yoann-symfony. L'historique des migrations Doctrine (15 versions du schéma générées au fil de l'évolution du modèle) illustre la démarche incrémentale : chaque évolution du modèle métier (ajout d'un champ image_article, passage du titre en VARCHAR(255), introduction de la relation many-to-many catégories) a été matérialisée par une migration versionnée plutôt que par une modification directe du schéma, ce qui permet à n'importe quel poste de développement de rejouer l'historique pour reproduire la base.

---

### Exemple n°4 — CP 4 : Contribuer à la gestion d'un projet informatique

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

J'ai obtenu en 2017 le **diplôme Chef de Projet Digital** de la CCI Ouest-Normandie (Caen), formation de 9 mois centrée sur la gestion de projets web. La formation a couvert le cadrage d'un besoin client, la rédaction d'un cahier des charges, la priorisation d'un backlog, le découpage en lots, le suivi d'avancement via outils type Trello / Jira, la communication client et la livraison itérative.

Plus récemment, dans le cadre de mon activité de **Technical Support Analyst chez HCL Technologies** (Dublin, 2021-2022, en remote) sur le produit Google Ad Manager, j'ai été amené à contribuer à la gestion d'incidents techniques de niveau 2/3 impliquant plusieurs équipes (équipe support de premier niveau, équipe d'ingénierie Google, équipe client). Mon rôle consistait à reproduire le problème côté navigateur (analyse JavaScript, inspection des pixels de tracking, vérification des appels réseau), produire un dossier d'investigation reproductible, prioriser le ticket selon le critère d'impact business du client, et coordonner la communication entre les différents intervenants. J'ai utilisé un outil de ticketing interne pour le suivi et la documentation des cas, et j'ai contribué à enrichir la base de connaissances de l'équipe en formalisant des templates d'investigation pour les patterns de bugs récurrents.

#### 2. Précisez les moyens utilisés

- **Méthodologie** : gestion de projet en mode agile / itératif (formation CCI), gestion d'incidents en mode ITIL (HCL)
- **Outils de suivi** : Trello (formation CCI), outil de ticketing interne HCL, suivi de cases multi-équipes
- **Outils de communication** : email professionnel, chat Slack / Teams selon le contexte, conférences téléphoniques pour les incidents critiques
- **Outils d'investigation technique** : Chrome DevTools, Postman, capture de trafic réseau, lecture de logs côté client
- **Documentation** : rédaction de comptes-rendus d'investigation, mise à jour de la base de connaissances internes

#### 3. Avec qui avez-vous travaillé ?

- **Formation CCI** : promotion d'une quinzaine d'apprenants, encadrement par les formateurs CCI, projets de groupe avec rendus oraux
- **HCL Technologies** : équipe de support technique internationale (collègues basés en Inde, en Roumanie, en Pologne), interaction avec les ingénieurs Google Ad Manager pour les escalades, et avec les responsables technique côté clients (annonceurs et éditeurs Ad Manager)

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : (1) CCI Ouest-Normandie, Caen ; (2) HCL Technologies, Dublin (en remote)
- **Chantier, atelier, service** : (1) Diplôme Chef de Projet Digital ; (2) Support Google Ad Manager — niveau 2/3
- **Période d'exercice** : (1) 2017 ; (2) 2021 – 2022

#### 5. Informations complémentaires (facultatif)

Le diplôme CCI est disponible en lecture sur mon portfolio : https://florianchague.dev/docs/diplome-cci.jpg. Mon expérience HCL sur Google Ad Manager m'a en parallèle donné une compétence approfondie en debug JavaScript côté navigateur (pixels de tracking, intégrations cross-domain), qui me sert directement dans mes projets de développement actuels.

---

## Activité-type 2 — Concevoir et développer une application sécurisée organisée en couches

### Exemple n°5 — CP 5 : Analyser les besoins et maquetter une application

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Dans le cadre de l'examen Figma de la formation ADRAR, j'ai dû concevoir entièrement les maquettes haute fidélité d'une application mobile de e-commerce pour un label de musique punk fictif baptisé **HBM**. Le sujet imposait une boutique en ligne de posters d'artistes, avec navigation, présentation du label, vitrine produits et tunnel d'achat, le tout en mobile-first sur un format de type iPhone. Le délai imparti pour l'examen était fixe et contraignant.

J'ai mené la conception en plusieurs étapes :

1. **Analyse du brief** : décomposition des fonctionnalités attendues (About, Artistes, News, Shop, Contact, Legal), identification des écrans nécessaires (drawer de navigation, écran d'accueil, fiche produit, carrousel de posters, panier).
2. **Définition de la charte visuelle** : direction artistique cohérente avec l'univers punk (palette sombre dominante, accents rouge sang pour le branding "HBM" et les prix, jaune vif pour les CTA principaux type "Add to Cart"). Le contraste élevé sert à la fois l'identité musicale et la lisibilité mobile.
3. **Maquettage mobile-first dans Figma** : construction des frames iPhone avec respect des safe areas, composants réutilisables (header avec logo HBM, icônes recherche et menu burger, footer "Powered by ADRAR"), variantes d'état pour le carrousel produits.
4. **Production des écrans clés** : drawer de navigation, vue catalogue carrousel produits ("Stay Punk", "Spirit of 76", chacun à 9.99$), cartes produits avec image plein cadre + titre + prix + CTA "Add to Cart" en jaune.

**Honnêteté sur le résultat** : je n'ai **pas réussi à finaliser les maquettes dans le temps imparti de l'examen**, ce qui s'est traduit par un rendu partiel et une note insuffisante. La direction artistique, le drawer de navigation et le carrousel produits étaient livrés et fonctionnels au sein du prototype, mais les écrans du tunnel d'achat (panier, validation de commande, confirmation) ainsi que la fiche artiste détaillée n'ont pas pu être produits dans le délai. Cet échec ponctuel a été pour moi un retour d'expérience structurant sur la priorisation et le découpage du temps en contexte contraint : depuis, je commence systématiquement par produire des wireframes basse fidélité de l'ensemble des écrans avant d'investir du temps sur la haute fidélité, pour garantir un livrable minimal couvrant le périmètre complet, quitte à raffiner ensuite si le temps le permet.

#### 2. Précisez les moyens utilisés

- **Outil de maquettage** : Figma (frames iPhone, composants réutilisables, prototypage interactif via les liens entre frames)
- **Méthodologie** : approche mobile-first, design system embryonnaire (couleurs nommées, typographies cohérentes, espacement systémique)
- **Références d'inspiration** : sites de labels musicaux indépendants, codes graphiques de l'univers punk (typographies grungy, photographies high-contrast)
- **Documentation produite** : notes d'intentions de design dans Figma (cartouches d'annotation à côté des frames pour expliquer les choix UX)

#### 3. Avec qui avez-vous travaillé ?

Travail individuel dans le cadre de l'examen, sous l'encadrement et l'évaluation du formateur référent du module UX/UI design de l'ADRAR.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle, Ramonville-Saint-Agne
- **Chantier, atelier, service** : Module UX/UI design — formation CDA, examen Figma sur le sujet HBM (label punk fictif)
- **Période d'exercice** : 2025 (cours de formation)

#### 5. Informations complémentaires (facultatif)

Trois écrans produits sont conservés en illustration : le drawer de navigation principal (About / Artistes / News / Shop / Contact / Legal), le carrousel produit avec le poster "Stay Punk" 9.99$ et la fiche "Spirit of 76". Le footer "Powered by ADRAR" rappelle l'origine pédagogique du sujet. Au-delà de la note insuffisante de l'examen, le rendu partiel reste pour moi un livrable réutilisable sur la dimension maquettage et direction artistique, et la leçon de priorisation tirée de cet échec a été appliquée immédiatement sur les projets ultérieurs (mémoire de soutenance, projets perso du portfolio).

---

### Exemple n°6 — CP 6 : Définir l'architecture logicielle d'une application

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Sur le projet Symfony de la formation ADRAR (application de blog avec articles, catégories et utilisateurs), j'ai défini une architecture logicielle en couches respectant les conventions du framework et les principes de séparation des responsabilités. Le découpage retenu :

- **Couche présentation** : contrôleurs Symfony (`ArticleController`, `CategoryController`, `UserController`, `HomeController`) avec routage déclaratif via attributs PHP 8 `#[Route]`, rendu via templates Twig (`templates/article/list-article.html.twig`, `single-article.html.twig`).
- **Couche métier** : entités Doctrine (`Article`, `Category`, `User`) qui portent les invariants du domaine et les méthodes de manipulation de leurs relations (`addCategory`, `removeCategory`, etc.).
- **Couche d'accès aux données** : repositories Doctrine (`ArticleRepository`, `CategoryRepository`, `UserRepository`) étendant `ServiceEntityRepository`, qui exposent les méthodes de requêtage typées (`findBy`, `findOneBy`, plus des méthodes custom le cas échéant).
- **Couche d'infrastructure** : configuration centralisée dans `config/packages/` (doctrine, framework, routing, twig), variables d'environnement dans `.env` et `.env.local`, migrations versionnées dans `migrations/`.
- **Couche de chargement de données** : `App\DataFixtures\AppFixtures` qui peuple la base à des fins de développement (20 users, 8 catégories, 100 articles via Faker).

J'ai matérialisé les principales décisions d'architecture sous forme de notes : pourquoi les ManyToMany Article ↔ Category n'ont pas besoin de table d'association explicite (Doctrine la génère automatiquement), pourquoi les fixtures sont dans un namespace séparé (pour pouvoir les exclure facilement de la prod), pourquoi le routage est défini par attributs plutôt qu'en YAML (lisibilité, refactor IDE-aware).

#### 2. Précisez les moyens utilisés

- **Framework** : Symfony 7.4 avec son organisation de répertoires standard (`src/Controller`, `src/Entity`, `src/Repository`, `src/DataFixtures`, `config/`, `migrations/`, `templates/`)
- **ORM** : Doctrine ORM 3.6 + Doctrine Migrations Bundle 3.7 pour le versionnement du schéma
- **Pattern** : MVC adapté à l'écosystème Symfony, avec couche Repository pour la persistance
- **Templating** : Twig 3 (séparation présentation / logique)
- **Routing** : attributs PHP 8 `#[Route]` directement sur les méthodes de contrôleur
- **Documentation** : documentation officielle Symfony et Doctrine pour valider les conventions

#### 3. Avec qui avez-vous travaillé ?

Travail individuel sur le sujet pédagogique, avec validation des choix d'architecture auprès du formateur référent du module Symfony lors d'un point de mi-parcours.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle
- **Chantier, atelier, service** : Module Symfony — formation CDA
- **Période d'exercice** : Février 2026

#### 5. Informations complémentaires (facultatif)

Cette architecture en couches a été conçue pour faciliter trois choses : la testabilité (chaque couche peut être instanciée et stubée indépendamment), la lisibilité (un nouveau lecteur sait où chercher une logique donnée), et l'évolutivité (l'ajout d'une nouvelle entité — par exemple Comment — suit le même pattern à dupliquer : entité + repository + contrôleur + migration).

---

### Exemple n°7 — CP 7 : Concevoir et mettre en place une base de données relationnelle

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Dans le cadre du module Base de données de la formation ADRAR, j'ai conçu de bout en bout la base de données relationnelle d'une application de jeu de rôle multijoueur dans laquelle des joueurs créent des personnages typés (mages, guerriers, etc.) et s'échangent des messages internes. Le sujet du rattrapage MCD/MLD était imposé, à partir d'un cahier de besoin fonctionnel décrivant les acteurs et leurs interactions.

J'ai produit trois livrables enchaînés :

1. **Modèle Conceptuel de Données (MCD)** — formalisation Merise. Quatre entités identifiées : `Player` (joueur, avec email, pseudo, mot de passe), `Message` (avec topic, contenu, date), `Character` (personnage du joueur), `Type` (classe du personnage, avec ses statistiques de points de vie, attaque, défense). Trois associations : `Sends/Receives` entre Player et Message (chaque message a un expéditeur 1,1 et un destinataire 1,1, chaque joueur peut envoyer ou recevoir 0,n messages), `Owns` entre Player et Character (0,n côté Player car un joueur peut détenir plusieurs personnages, 1,1 côté Character car un personnage appartient à un et un seul joueur), `Is of type` entre Character et Type (1,1 côté Character car obligatoire, 0,n côté Type car une classe peut être partagée par plusieurs personnages).

2. **Modèle Logique de Données (MLD)** — dérivé du MCD selon les règles Merise. Chaque entité devient une table. Les associations 1,1 ↔ 0,n donnent une clé étrangère sur la table côté Many : `Message.id_Player` (sender), `Message.id_Player_1` (receiver), `Character.id_Player`, `Character.id_Type`. Les types et longueurs SQL ont été spécifiés (`INT`, `VARCHAR(50)`, `DATETIME`) en fonction des contraintes métier (un email respecte `UNIQUE`, un mot de passe est limité à 50 caractères car stocké pré-hash dans cet exercice pédagogique).

3. **Dictionnaire de données** — tableau récapitulatif pour chaque table : nom court (id_Player, email, pseudo...), nom complet (description en clair), format SQL, longueur, commentaires sur l'usage métier de chaque colonne (par exemple : "email — doit être unique" ou "pseudo — doit être unique également même si pas indispensable au login").

#### 2. Précisez les moyens utilisés

- **Outil de modélisation** : Looping (logiciel libre français de modélisation Merise) pour produire les diagrammes MCD et MLD exportables
- **Méthodologie** : formalisation Merise, vocabulaire et notations standard (cardinalités min,max ; clés primaires soulignées ; clés étrangères en italique)
- **Outil de dictionnaire** : Microsoft Excel pour le tableau récapitulatif des champs
- **Référence pédagogique** : cours du module Base de données de l'ADRAR, supports sur la conception MCD/MLD
- **Validation** : auto-relecture, vérification que chaque cardinalité du MCD se traduit correctement en clé étrangère dans le MLD

#### 3. Avec qui avez-vous travaillé ?

Travail individuel dans le cadre du rattrapage, avec correction et retours du formateur référent du module Base de données après remise des livrables.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle
- **Chantier, atelier, service** : Module Base de données — formation CDA, exercice de rattrapage
- **Période d'exercice** : 2026

#### 5. Informations complémentaires (facultatif)

L'exercice a permis de manipuler concrètement la subtilité de la transformation MCD → MLD sur des cardinalités non triviales (notamment le double Sends / Receives qui amène deux FK pointant sur la même table Player depuis Message — d'où la nomenclature `id_Player` / `id_Player_1` dans le MLD). Le livrable final comprend le diagramme MCD, le diagramme MLD, le dictionnaire de données et un fichier source Looping (`.lo1`) permettant de rouvrir et modifier le schéma.

---

### Exemple n°8 — CP 8 : Développer des composants d'accès aux données SQL et NoSQL

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Dans le module Base de données de la formation ADRAR, j'ai réalisé un TP SQL complet sur une base de données fictive nommée `alltrade`, qui modélise une plateforme de vente avec utilisateurs (généralisation User → Seller / Customer), tickets de vente, produits et catégories. Le TP couvrait l'ensemble du cycle de manipulation d'une base de données relationnelle MySQL : modélisation physique en SQL, jeu de données, requêtes de mise à jour et de suppression, évolutions du schéma.

J'ai écrit trois scripts SQL distincts :

1. **`01-structure.sql`** — création de la base de données et de l'ensemble des tables. Particularité pédagogique du TP : l'héritage **XT (Exclusif + Total)** entre User et ses deux entités filles Seller / Customer. J'ai matérialisé cet héritage par une table parente `User_` et deux tables filles partageant la clé primaire via `FOREIGN KEY ... REFERENCES User_(id_user) ON DELETE CASCADE`. J'ai aussi implémenté plusieurs contraintes `CHECK` (`age_user >= 12`, `price_product >= 0`, `quantite > 0` sur la table d'association `ticket_product`), des contraintes `UNIQUE` (sur l'email utilisateur), et des clés étrangères avec `ON DELETE CASCADE` partout où la suppression du parent devait logiquement entraîner celle des enfants.

2. **`02-insert.sql`** — peuplement de la base avec un jeu de données réaliste : 10 utilisateurs typés Seller / Customer, 10 produits, 5 catégories, 7 tickets liés à différentes paires vendeur/client, et la table d'association `product_category` pour catégoriser les produits.

3. **`03-update_delete.sql`** — mises à jour ciblées (modification de l'email d'un utilisateur via `WHERE id_user = 3`, multiplication globale des prix par 1.15), et suppressions sélectives à l'aide d'expressions régulières MySQL (suppression des produits dont le nom commence par les lettres a à g via `RLIKE '[a-gA-G].*'`).

Le TP comprenait également des opérations d'évolution de schéma via `ALTER TABLE` : ajout d'une colonne `quantite` à la table d'association `ticket_product` avec sa contrainte CHECK, renommage de la colonne `client_ID` en `customer_ID` (via `CHANGE COLUMN`), changement de type de `seller_ID` (de `INT` à `VARCHAR(20)` via `MODIFY COLUMN`), suppression de la colonne `age_user` et ajout d'une colonne `day_of_born` typée `DATE` pour un calcul d'âge plus précis.

#### 2. Précisez les moyens utilisés

- **SGBD** : MySQL 8 (moteur InnoDB, charset utf8mb4)
- **Langage** : SQL ANSI / dialecte MySQL (DDL, DML, expressions régulières via `RLIKE`)
- **Client SQL** : MySQL Workbench ou ligne de commande `mysql -u root -p`
- **Concepts mis en pratique** : généralisation/spécialisation (héritage XT), clés primaires et étrangères, contraintes d'intégrité (CHECK, UNIQUE, NOT NULL, ON DELETE CASCADE), tables d'association pour les relations N:N, ALTER TABLE chirurgical
- **Versionnement** : scripts SQL séparés et numérotés (01, 02, 03) pour pouvoir rejouer la séquence dans l'ordre depuis zéro

#### 3. Avec qui avez-vous travaillé ?

Travail individuel sur le sujet, avec un point intermédiaire avec le formateur référent du module Base de données pour valider la mécanique de l'héritage XT.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : ADRAR Formation Professionnelle
- **Chantier, atelier, service** : Module Base de données — formation CDA, TP1 SQL
- **Période d'exercice** : 2026

#### 5. Informations complémentaires (facultatif)

L'autre exemple de pratique sur cette compétence est l'utilisation de **Doctrine ORM** dans mon projet Symfony de formation (https://github.com/IztochenValk/yoann-symfony). Dans ce projet, je ne manipule pas directement le SQL : c'est Doctrine qui le génère à partir du mapping objet (annotations `#[ORM\Column]`, `#[ORM\ManyToOne]`, etc.) et qui matérialise chaque évolution du schéma via une migration versionnée (15 migrations Doctrine cumulées sur le projet). Les requêtes d'accès passent par les repositories typés (`ArticleRepository::findBy([], ['publishedAt' => 'DESC'])`) ou par le QueryBuilder Doctrine pour les besoins plus complexes. Les deux approches (SQL direct dans le TP `alltrade`, ORM dans le projet Symfony) m'ont permis de manipuler les deux extrêmes du spectre d'accès aux données SQL et de comprendre les trade-offs (contrôle fin vs productivité, dette de mapping vs requêtes optimisées).

---

## Activité-type 3 — Préparer le déploiement d'une application sécurisée

### Exemple n°9 — CP 9 : Préparer et exécuter les plans de tests d'une application

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

En tant que **Technical Support Analyst chez HCL Technologies** sur le produit Google Ad Manager, j'ai mené quotidiennement des activités assimilables à de la préparation et l'exécution de plans de tests : reproduction de bugs signalés par les clients (annonceurs et éditeurs Ad Manager), conception de scénarios de test ciblés permettant d'isoler la cause racine, et exécution de ces scénarios dans un environnement contrôlé.

Pour chaque ticket de support de niveau 2/3, je suivais une démarche structurée :

1. **Lecture du dossier client** : symptôme observé, navigateur utilisé, étapes que le client dit avoir suivies, configuration du compte Ad Manager.
2. **Reproduction initiale** : tentative de reproduire le bug à l'identique sur mon environnement test (compte de support dédié, navigateur paramétrable, network throttling au besoin pour simuler des latences).
3. **Conception de scénarios de variation** : si le bug ne se reproduit pas du premier coup, j'identifiais les variables non maîtrisées (version du navigateur, extension AdBlock, statut de la session, format de la requête publicitaire) et je testais méthodiquement chaque variable une à une.
4. **Capture des artefacts** : screenshots, exports HAR du trafic réseau, dump des cookies / localStorage / sessionStorage, logs de la console JavaScript.
5. **Rédaction d'un compte-rendu reproductible** : étapes exactes à suivre, environnement, artefacts attachés, hypothèse de cause racine.
6. **Validation du correctif côté ingénierie** : une fois le fix livré, je rejouais l'intégralité du scénario pour vérifier la résolution avant de fermer le ticket avec le client.

J'ai également contribué à enrichir la base de connaissances interne en formalisant des templates d'investigation pour les patterns récurrents (problèmes de tracking pixel cross-domain, conflits de cookies tiers post-Chrome 90, bugs de rendu de creative en iframe).

Côté projet Symfony de la formation ADRAR, j'ai par ailleurs utilisé les **DataFixtures** Faker pour garantir un jeu de données de test reproductible : à chaque réexécution de `bin/console doctrine:fixtures:load`, la base était remise à un état déterminé (20 users, 8 catégories, 100 articles avec leurs relations), ce qui permettait à tout intervenant de partir d'un état connu pour ses tests manuels.

#### 2. Précisez les moyens utilisés

- **Outils de reproduction** : Chrome avec DevTools, Firefox, parfois Safari ; profils de navigateur isolés ; postures réseau via Network throttling
- **Outils d'investigation** : DevTools Network panel, Console panel, Application panel (cookies / storage), Elements panel pour l'inspection du DOM Ad Manager
- **Outils de capture** : exports HAR (HTTP Archive), screenshots annotés, captures vidéo
- **Outils de ticketing** : système interne HCL, indexation par client et par sévérité
- **Outils côté Symfony** : `bin/console doctrine:fixtures:load --no-interaction`, Faker pour la génération aléatoire mais reproductible (avec un seed fixé)
- **Documentation** : base de connaissances interne HCL, documentation officielle Google Ad Manager pour les standards de tracking

#### 3. Avec qui avez-vous travaillé ?

Équipe support technique HCL internationale (Inde, Roumanie, Pologne) pour la rotation et l'escalade des tickets ; équipe d'ingénierie Google Ad Manager pour la remontée des bugs côté produit ; responsables techniques côté clients (annonceurs et éditeurs publicitaires) pour la validation finale du correctif.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : HCL Technologies, Dublin (en remote)
- **Chantier, atelier, service** : Support Google Ad Manager — niveau 2/3
- **Période d'exercice** : 2021 – 2022

#### 5. Informations complémentaires (facultatif)

L'expérience HCL m'a appris la rigueur de la reproductibilité (un bug "qui marche chez moi" ne compte pas tant que je n'ai pas écrit la séquence exacte qui le déclenche chez le client) et la valeur d'un état initial connu (d'où l'usage systématique des fixtures Faker dans mes projets Symfony aujourd'hui).

---

### Exemple n°10 — CP 10 : Préparer et documenter le déploiement d'une application

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Pour distribuer publiquement mon userscript **Highlight Intercom** (développé chez Pontica Solutions en 2020), j'ai préparé et documenté son déploiement sur la plateforme **OpenUserJS**, qui est un registre public dédié aux userscripts compatibles Tampermonkey, Greasemonkey et Violentmonkey. L'enjeu : permettre à n'importe quel agent support utilisant Intercom de découvrir, installer, et recevoir automatiquement les mises à jour du script, sans devoir copier-coller le code à la main.

J'ai produit les éléments de déploiement suivants :

1. **Bloc de métadonnées du userscript** (`==UserScript==` … `==/UserScript==`) avec les directives standardisées :
   - `@name` (nom affiché à l'installation)
   - `@namespace` (identifiant unique : `Pontica solutions`)
   - `@version` (versionnement sémantique : 0.1 pour la première publication)
   - `@license` (MIT)
   - `@copyright` (mention 2020, Ayanokojiii)
   - `@description` (description courte de la valeur)
   - `@author` (auteur identifié)
   - `@downloadURL` (URL d'installation directe depuis OpenUserJS)
   - `@updateURL` (URL des métadonnées pour la vérification automatique des mises à jour par Tampermonkey)
   - `@match` (filtre d'URL strict sur `https://app.intercom.com/a/apps/*`)
   - `@require` (URLs des bibliothèques externes : jQuery.address, Spectrum)
   - `@resource` (CSS de Spectrum chargé en tant que ressource embarquée)
   - `@grant` (permissions Tampermonkey requises : `GM_getResourceText`, `GM_addStyle`)
   - `@noframes` (empêche l'injection dans les iframes Intercom internes)

2. **Page de documentation publique** sur OpenUserJS avec description, captures d'écran, et instructions d'installation à destination des agents support.

3. **Validation du contrat de mise à jour** : test d'un workflow complet où je publie une nouvelle version 0.2, et Tampermonkey détecte la nouvelle version via `@updateURL` et la propose automatiquement aux utilisateurs déjà équipés.

#### 2. Précisez les moyens utilisés

- **Plateforme de distribution** : OpenUserJS (https://openuserjs.org)
- **Système de versionnement** : champ `@version` du userscript, suivant les conventions de Tampermonkey (incrément déclenche la proposition de mise à jour)
- **Mécanisme de licence** : license MIT déclarée via la directive `@license` et le copyright statement en commentaire JavaScript
- **Mécanisme de mise à jour** : couple `@downloadURL` / `@updateURL` pointant sur les endpoints OpenUserJS qui servent respectivement le code et la meta `.user.js` / `.meta.js`
- **Documentation utilisateur** : champ description du userscript + page OpenUserJS publique
- **Documentation technique** : commentaires inline dans le code (intention de chaque IIFE, sélecteurs CSS cibles Intercom, structure du localStorage)

#### 3. Avec qui avez-vous travaillé ?

Distribution coordonnée avec les agents support Pontica Solutions à qui j'ai d'abord partagé l'URL d'installation OpenUserJS en interne, en récoltant leurs retours d'usage avant l'ouverture publique du script. Publication publique ensuite ouverte aux utilisateurs Intercom au-delà de Pontica.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : Pontica Solutions, Varna, Bulgarie ; distribution publique via OpenUserJS
- **Chantier, atelier, service** : Outillage support, distribution communautaire
- **Période d'exercice** : 2020 – 2021 (distribution initiale) ; en ligne jusqu'à aujourd'hui

#### 5. Informations complémentaires (facultatif)

Lien public : https://openuserjs.org/scripts/Ayanokoji111/Highlight_Intercom. À la date de rédaction du dossier, le script totalise **172 installations cumulées**, ce qui valide à la fois l'utilité fonctionnelle et la bonne santé du circuit de distribution.

---

### Exemple n°11 — CP 11 : Contribuer à la mise en production dans une démarche DevOps

#### 1. Décrivez les tâches ou opérations que vous avez effectuées, et dans quelles conditions

Pour mon **portfolio personnel** (https://florianchague.dev), j'ai mis en place une chaîne de déploiement automatisée fidèle aux principes DevOps : code source versionné sur GitHub, build et déploiement déclenchés automatiquement à chaque push sur la branche principale, environnement de production accessible publiquement avec terminaison HTTPS et certificat valide.

Les opérations effectuées :

1. **Versionnement** : initialisation du dépôt Git, organisation de la branche `main` comme branche de production, mise en place d'un `.gitignore` excluant les artefacts de build (`.output/`, `node_modules/`).
2. **Pipeline d'intégration et de déploiement continu** : configuration via la plateforme d'hébergement Vercel qui scrute le dépôt GitHub et déclenche à chaque push un build Nuxt (`nuxt build`) puis un déploiement de la sortie sur son edge network. Le pipeline expose les logs de build en temps réel et permet de revenir à une version antérieure en un clic (rollback).
3. **Gestion des variables d'environnement** : injection des variables sensibles (clés d'API tierces, IDs analytics) via l'interface Vercel plutôt qu'en dur dans le repo.
4. **Domaine et certificat TLS** : configuration du domaine personnalisé `florianchague.dev`, génération automatique du certificat Let's Encrypt par Vercel, redirection HTTPS forcée.
5. **Vérification post-déploiement** : smoke test manuel des routes critiques (`/`, `/projets`, `/visual-lab`, `/#contact`) après chaque déploiement, validation de l'export PDF du CV (`/docs/cv.pdf`).

J'ai par ailleurs assuré la **mise en production publique** de mon userscript Highlight Intercom (cf. CP 10) via OpenUserJS, avec un cycle classique DevOps appliqué à un userscript : versionnement, déclenchement de mise à jour automatique côté clients via le champ `@updateURL`, traçabilité des installations.

#### 2. Précisez les moyens utilisés

- **Versionnement** : Git, hébergement GitHub (dépôts publics)
- **Pipeline CI/CD** : Vercel (build/deploy automatique à chaque push), alternativement GitHub Actions pour les automatisations complémentaires (linting, vérification du build)
- **Build** : Nuxt 4 (commande `nuxt build` produisant le bundle SSR dans `.output/`)
- **Terminaison TLS** : certificat Let's Encrypt géré par Vercel, renouvellement automatique
- **Domaine** : DNS pointé vers Vercel via un enregistrement CNAME
- **Distribution userscript** : OpenUserJS comme registre public, contrat de mise à jour via `@downloadURL` / `@updateURL`
- **Documentation publique** : README.md à la racine de chaque dépôt GitHub, pages projet sur le portfolio

#### 3. Avec qui avez-vous travaillé ?

Travail individuel sur les deux mises en production (portfolio + userscript). Coordination ponctuelle avec le support Vercel sur des questions de DNS / certificat lors du premier rattachement du domaine.

#### 4. Contexte

- **Nom de l'entreprise, organisme ou association** : Projets personnels — portfolio (https://florianchague.dev) ; Pontica Solutions pour le userscript
- **Chantier, atelier, service** : Mise en ligne et maintenance d'un portfolio professionnel ; distribution publique d'un outil métier
- **Période d'exercice** : 2025 – 2026 pour le portfolio ; 2020 et toujours en ligne pour le userscript

#### 5. Informations complémentaires (facultatif)

L'approche DevOps me semble particulièrement adaptée aux projets web modernes où la fréquence de mise en production est élevée : les automatisations en place sur mon portfolio me permettent de pousser un correctif (faute de frappe sur un projet, ajout d'une nouvelle expérience pro) en moins de 30 secondes du commit Git au déploiement effectif visible publiquement, avec un coût opérationnel quasi nul.

---

## Titres, diplômes, CQP, attestations de formation (facultatif)

| Intitulé | Organisme | Année |
|---|---|---|
| Concepteur Développeur d'Applications — Spécialisation DevOps | ADRAR Formation Professionnelle, Ramonville-Saint-Agne | 2025 – 2026 (en cours) |
| Diplôme Chef de Projet Digital | CCI Ouest-Normandie, Caen | 2017 |
| Licence en Droit Public | Université Montpellier I | 2012 |

---

## Déclaration sur l'honneur

Je soussigné(e) **Florian Chague**, candidat(e) au titre professionnel de Concepteur Développeur d'Applications, déclare sur l'honneur que les exemples de pratique professionnelle présentés dans ce dossier sont issus de mes propres expériences (formations, projets personnels, missions professionnelles passées) et que les éléments fournis sont sincères et exacts à la date de signature.

Fait à _________________________, le _________________________.

Signature : _________________________

---

## Annexes — Documents illustrant la pratique professionnelle (facultatif)

- **Code source du userscript Highlight Intercom** : https://openuserjs.org/scripts/Ayanokoji111/Highlight_Intercom
- **Dépôt GitHub du projet Symfony Yoann (blog)** : https://github.com/IztochenValk/yoann-symfony
- **Portfolio personnel** : https://florianchague.dev
- **Projets personnels** : https://florianchague.dev/projets
- **Laboratoire visuel (expérimentations)** : https://florianchague.dev/visual-lab
- **Diplôme CCI Chef de Projet Digital 2017** : https://florianchague.dev/docs/diplome-cci.jpg
- **CV en anglais** : https://florianchague.dev/docs/cv.pdf
- **Maquettes Figma de l'examen HBM** (sur demande) : trois écrans mobile haute fidélité — drawer de navigation, carrousel produit "Stay Punk", fiche produit "Spirit of 76"
- **Livrables du rattrapage MCD/MLD ADRAR** (sur demande) : diagrammes Looping, dictionnaire de données Excel
- **Scripts SQL du TP `alltrade` ADRAR** (sur demande) : `01-structure.sql`, `02-insert.sql`, `03-update_delete.sql`
