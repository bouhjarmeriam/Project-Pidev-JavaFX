🏥 ClinCare – Application Desktop pour la Gestion d’une Clinique

  
    
  


📖 Description du Projet
ClinCare est une application desktop complète développée en JavaFX, dans le cadre du projet intégré Web-Java de 3e année universitaire (2024–2025).Elle vise à faciliter la gestion administrative et médicale d’une clinique grâce à une interface graphique moderne et modulaire.
🗂 Table des Matières

Pré-requis
Installation
Utilisation
Fonctionnalités principales
Démo
Contact


✅ Pré-requis
Avant d'exécuter le projet, assurez-vous d'avoir :

JDK 17
Un IDE (IntelliJ IDEA ou VSCode)
JavaFX
MySQL
Git
Maven


⚙ Installation
git clone https://github.com/bouhjarmeriam/Project-Pidev-JavaFX.git
cd Project-Pidev-JavaFX
mvn clean install


🚀 Utilisation

Ouvrir le fichier src/main/java/main/MainApp.java
Lancer l'application depuis l'IDE


🔧 Fonctionnalités principales
👤 Gestion des utilisateurs

Gestion des utilisateurs (User, Médecin, Patient, Pharmacien, Staff)
Architecture orientée services sans héritage entre les classes
Chaque type d'utilisateur possède sa propre entité Java
Support complet CRUD, hashage sécurisé des mots de passe, envoi d’e-mails à la création et assignation dynamique des rôles

🏢 Gestion des infrastructures cliniques

Gestion des départements, étages et salles avec interfaces dynamiques
Réservation de salles, visualisation des disponibilités et statistiques détaillées par étage/département
Support de l’import/export CSV pour faciliter l’analyse ou migration des données

💊 Gestion des médicaments et des commandes

Le pharmacien gère les médicaments (CRUD), avec un chatbot intelligent d’assistance
Statistiques selon le type de médicament et détection automatique des expirations
Le patient peut passer des commandes avec paiement sécurisé via Stripe et facturation PDF automatisée

🩺 Gestion des consultations, services médicaux et évaluations

Interfaces distinctes : patient, administrateur, gestion des services
Notation des services, export PDF, graphes statistiques, SMS via Twilio, multi-langues, et recherche avancée
Design moderne avec animations, ombres portées, dégradés et code bien structuré

📋 Gestion des dossiers médicaux et séjours

Opérations CRUD avec validation de saisie sur les dossiers et séjours hospitaliers
Lecture par code scanner pour accès rapide aux données médicales
Planification via calendrier interactif (hospitalisations, rendez-vous, opérations)
Statistiques médicales exportables en PDF, améliorant la planification et la communication médicale

➕ Autres fonctionnalités

Formulaires interactifs avec upload d’images
Recherches dynamiques et filtrage des données
Système de recommandation personnalisé
Chatbot de support intégré


🎬 Démo
👉 Voir la démo vidéo de ClinCare

📬 Contact

GitHub: @bouhjarmeriam
Facebook: ClinCare

