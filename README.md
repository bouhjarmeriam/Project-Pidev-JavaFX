import os

# Contenu du README.md pour CliniCare
readme_content = """# 🏥 CliniCare – Application Desktop pour la Gestion d’une Clinique

<p align="center">
  <a href="https://www.facebook.com/profile.php?id=61572284563201">
    <img src="https://img.shields.io/badge/Join%20us%20on-Facebook-blue" alt="CliniCare Facebook"/>
  </a>
  <a href="https://github.com/bouhjarmeriam">
    <img src="https://img.shields.io/badge/Follow%20us%20on-GitHub-181717" alt="CliniCare GitHub"/>
  </a>
</p>

## 📖 Description du Projet

CliniCare est une application desktop complète développée en *JavaFX*, dans le cadre du projet intégré Web-Java de 3e année universitaire (2024–2025). Elle vise à faciliter la gestion administrative et médicale d’une clinique grâce à une interface graphique moderne et modulaire. L'application offre une solution tout-en-un pour gérer les utilisateurs, les infrastructures cliniques, les médicaments, les consultations, les dossiers médicaux, et bien plus, avec des fonctionnalités avancées comme un chatbot, un système de recommandation, et des exports PDF.

## 🗂 Table des Matières

- [Pré-requis](#pré-requis)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Fonctionnalités Principales](#fonctionnalités-principales)
- [Démo](#démo)
- [Contact](#contact)

## ✅ Pré-requis

Avant d'exécuter le projet, assurez-vous d'avoir installé les éléments suivants :

- *[JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)* : Pour exécuter l'application JavaFX.
- *[JavaFX SDK](https://openjfx.io/)* : Framework pour l'interface graphique.
- *[Maven](https://maven.apache.org/)* : Gestion des dépendances.
- *[MySQL](https://www.mysql.com/)* : Base de données pour stocker les données.
- *[Git](https://git-scm.com/)* : Pour cloner le repository.
- *Un IDE* : IntelliJ IDEA ou VSCode recommandé.
- *Clé API [Stripe](https://stripe.com/)* : Pour les paiements (mode test recommandé).
- *Clé API [Twilio](https://www.twilio.com/)* : Pour l'envoi de SMS.

## ⚙ Installation

1. *Cloner le repository* :
   bash
   git clone https://github.com/bouhjarmeriam/Project-Pidev-JavaFX.git
   cd Project-Pidev-JavaFX
   

2. *Installer les dépendances* :
   bash
   mvn clean install
   

3. *Configurer la base de données* :
   - Créez une base de données MySQL nommée clincare_db :
     sql
     CREATE DATABASE clincare_db;
     
   - Importez le schéma SQL (si fourni) ou configurez les tables via l'application.
   - Mettez à jour les informations de connexion dans src/main/resources/config.properties :
     properties
     db.url=jdbc:mysql://localhost:3306/clincare_db
     db.user=votre_utilisateur
     db.password=votre_mot_de_passe
     stripe.api.key=sk_test_votre_cle_stripe
     twilio.api.key=votre_cle_twilio
     

4. *Ouvrir le projet dans un IDE* :
   - Utilisez IntelliJ IDEA ou VSCode.
   - Assurez-vous que le JDK 17 et JavaFX sont configurés dans les paramètres du projet.

## 🚀 Utilisation

1. *Lancer l'application* :
   - Ouvrez le fichier src/main/java/main/MainApp.java dans votre IDE.
   - Exécutez la classe principale (MainApp).
   - Alternativement, depuis la ligne de commande :
     bash
     mvn javafx:run
     

2. *Tester les fonctionnalités* :
   - *Admin* : Connectez-vous avec un compte admin pour gérer les utilisateurs, départements, et services.
   - *Médecin/Pharmacien* : Gérez les consultations, médicaments, et commandes.
   - *Patient* : Accédez aux dossiers médicaux, passez des commandes, et planifiez des rendez-vous.
   - *Paiements* : Utilisez une carte de test Stripe (ex. 4242 4242 4242 4242) pour simuler un achat.

## 🛠 Fonctionnalités Principales

### 👥 Gestion des Utilisateurs
- *Types d'utilisateurs* : User, Médecin, Patient, Pharmacien, Staff.
- Architecture orientée services sans héritage entre les classes.
- Entités Java distinctes pour chaque type d'utilisateur.
- *CRUD complet* : Création, lecture, modification, suppression des comptes.
- *Sécurité* : Hashage sécurisé des mots de passe.
- *Emails* : Envoi d’emails automatiques à la création de compte.
- *Rôles dynamiques* : Assignation flexible des rôles selon les besoins.

### 🏢 Gestion des Infrastructures Cliniques
- Gestion des *départements, **étages, et **salles* via des interfaces dynamiques.
- *Réservation de salles* : Visualisation des disponibilités en temps réel.
- *Statistiques* : Données détaillées par étage/département (occupation, utilisation).
- *Import/Export CSV* : Analyse et migration des données simplifiées.

### 💊 Gestion des Médicaments et Commandes
- *Pharmacien* : CRUD pour les médicaments, suivi des stocks.
- *Chatbot intelligent* : Assistance pour la gestion des médicaments.
- *Statistiques* : Analyse par type de médicament, détection des expirations.
- *Commandes patients* : Paiement sécurisé via *Stripe*, facturation PDF automatisée.

### 🩺 Gestion des Consultations, Services Médicaux et Évaluations
- *Interfaces dédiées* : Patient, administrateur, gestion des services.
- *Évaluations* : Notation des services avec export PDF des résultats.
- *Statistiques* : Graphiques pour analyser les performances des services.
- *Notifications* : Envoi de SMS via *Twilio* pour rappels ou confirmations.
- *Multi-langues* : Support linguistique pour une accessibilité accrue.
- *Recherche avancée* : Filtrage des consultations et services.

### 📋 Gestion des Dossiers Médicaux et Séjours
- *CRUD sécurisé* : Gestion des dossiers médicaux et séjours hospitaliers avec validation des saisies.
- *Accès rapide* : Lecture par code scanner pour les données médicales.
- *Planification* : Calendrier interactif pour hospitalisations, rendez-vous, et opérations.
- *Export PDF* : Statistiques médicales exportables pour la planification.

### 📝 Formulaires Interactifs
- Upload d’images pour enrichir les dossiers ou consultations.
- Interfaces dynamiques avec validation en temps réel.

### 🔍 Recherches Dynamiques et Filtrage
- Recherche instantanée sur les utilisateurs, médicaments, consultations, et dossiers.
- Filtres personnalisables pour une navigation efficace.

### 🤝 Système de Recommandation Personnalisé
- Suggestions intelligentes pour les services, médicaments, ou rendez-vous basées sur les données utilisateur.

### 💬 Chatbot de Support Intégré
- Assistance automatisée pour les utilisateurs (patients, pharmaciens, staff).
- Réponses contextuelles pour améliorer l’expérience utilisateur.

## 🎬 Démo

👉 [Voir la démo vidéo de ClinCare sur YouTube](https://www.youtube.com/watch?v=exemple) (Lien à mettre à jour après publication)

## 📬 Contact

- *GitHub* : [@bouhjarmeriam](https://github.com/bouhjarmeriam)
- *Facebook* : [CliniCare](https://www.facebook.com/profile.php?id=61572284563201)
- *Email* : support@clincare.com

---

<p align="center">
  <img src="https://img.shields.io/badge/Made%20with-JavaFX-orange" alt="Made with JavaFX"/>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License MIT"/>
</p>
"""

