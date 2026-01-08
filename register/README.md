# Guelya Time - Application de Recommandation de Films

Application web de recommandation de films personnalisées avec une page d'inscription/connexion complète.

## 🏗️ Structure du Projet

```
register/
├── app/                          # Pages Next.js (App Router)
│   ├── layout.tsx               # Layout principal
│   ├── page.tsx                 # Page d'accueil
│   ├── globals.css              # Styles globaux
│   ├── register/
│   │   └── page.tsx             # Page d'inscription
│   └── login/
│       └── page.tsx             # Page de connexion
├── components/
│   ├── RegisterForm.tsx         # Formulaire d'inscription
│   ├── LoginForm.tsx            # Formulaire de connexion
│   └── ui/
│       ├── Button.tsx           # Composant bouton
│       ├── Input.tsx            # Composant input
│       ├── Toast.tsx            # Composant notification
│       └── PasswordStrengthIndicator.tsx
├── lib/
│   ├── api.ts                   # Client API Axios
│   └── validations.ts           # Règles de validation
├── types/
│   └── user.ts                  # Types TypeScript
├── backend/                      # Backend Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/guelyatime/
│       ├── controller/
│       ├── dto/
│       ├── exception/
│       ├── model/
│       ├── repository/
│       ├── security/
│       └── service/
└── package.json
```

## 🚀 Installation

### Frontend (Next.js)

```bash
# Installer les dépendances
npm install

# Copier les variables d'environnement
cp .env.example .env.local

# Lancer le serveur de développement
npm run dev
```

L'application sera accessible sur http://localhost:3000

### Backend (Spring Boot)

```bash
cd backend

# Compiler le projet
./mvnw clean install

# Lancer le serveur
./mvnw spring-boot:run
```

L'API sera accessible sur http://localhost:8080

### Neo4j

Assurez-vous que Neo4j est installé et lancé sur le port 7687.

## ✨ Fonctionnalités

### Page d'inscription
- ✅ Validation en temps réel des champs
- ✅ Indicateur de force du mot de passe
- ✅ Confirmation du mot de passe
- ✅ Messages d'erreur sous chaque champ
- ✅ Loading state sur le bouton submit
- ✅ Toast/notification de succès
- ✅ Redirection vers /login après inscription
- ✅ Design responsive (mobile-first)

### Page de connexion
- ✅ Validation des champs
- ✅ Afficher/masquer le mot de passe
- ✅ Lien "Mot de passe oublié"
- ✅ Toast de succès/erreur
- ✅ Design cohérent avec l'inscription

## 📡 API Endpoints

### POST /api/auth/register
Inscription d'un nouvel utilisateur.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Réponses:**
- `201`: Inscription réussie
- `400`: Erreur de validation

### POST /api/auth/login
Connexion d'un utilisateur.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

**Réponses:**
- `200`: Connexion réussie avec token JWT
- `401`: Identifiants invalides

## 🛡️ Validation des champs

| Champ | Règles |
|-------|--------|
| username | 3-20 caractères, lettres/chiffres/underscores |
| email | Format email valide |
| password | Min 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre |
| firstName | Optionnel, max 50 caractères |
| lastName | Optionnel, max 50 caractères |

## 🎨 Stack Technique

### Frontend
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **React Hook Form**
- **Axios**

### Backend
- **Spring Boot 3.2**
- **Spring Security**
- **Spring Data Neo4j**
- **JWT (jjwt)**
- **Bean Validation**

## 📱 Responsive Design

L'interface est conçue en mobile-first avec des breakpoints pour :
- Mobile: < 640px
- Tablette: 640px - 1024px
- Desktop: > 1024px

## 🔐 Sécurité

- Mots de passe hashés avec BCrypt
- Tokens JWT pour l'authentification
- Protection CORS configurée
- Validation côté client ET serveur
