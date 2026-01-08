// Schémas de validation pour les formulaires

export const validationRules = {
  username: {
    required: "Le nom d'utilisateur est requis",
    minLength: {
      value: 3,
      message: "Le nom d'utilisateur doit contenir au moins 3 caractères",
    },
    maxLength: {
      value: 20,
      message: "Le nom d'utilisateur ne peut pas dépasser 20 caractères",
    },
    pattern: {
      value: /^[a-zA-Z0-9_]+$/,
      message:
        "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores",
    },
  },
  email: {
    required: "L'email est requis",
    pattern: {
      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
      message: "Format d'email invalide",
    },
  },
  password: {
    required: "Le mot de passe est requis",
    minLength: {
      value: 8,
      message: "Le mot de passe doit contenir au moins 8 caractères",
    },
    validate: {
      hasUppercase: (value: string) =>
        /[A-Z]/.test(value) ||
        "Le mot de passe doit contenir au moins une majuscule",
      hasLowercase: (value: string) =>
        /[a-z]/.test(value) ||
        "Le mot de passe doit contenir au moins une minuscule",
      hasNumber: (value: string) =>
        /[0-9]/.test(value) ||
        "Le mot de passe doit contenir au moins un chiffre",
    },
  },
  firstName: {
    maxLength: {
      value: 50,
      message: "Le prénom ne peut pas dépasser 50 caractères",
    },
  },
  lastName: {
    maxLength: {
      value: 50,
      message: "Le nom ne peut pas dépasser 50 caractères",
    },
  },
};

// Calcul de la force du mot de passe
export interface PasswordStrength {
  score: number; // 0-4
  label: string;
  color: string;
}

export function calculatePasswordStrength(password: string): PasswordStrength {
  let score = 0;

  if (!password) {
    return { score: 0, label: "Très faible", color: "bg-border" };
  }

  // Longueur
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;

  // Complexité
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^a-zA-Z0-9]/.test(password)) score++;

  // Limiter le score à 4
  score = Math.min(score, 4);

  const strengthLevels: PasswordStrength[] = [
    { score: 0, label: "Très faible", color: "bg-status-error" },
    { score: 1, label: "Faible", color: "bg-orange-500" },
    { score: 2, label: "Moyen", color: "bg-status-warning" },
    { score: 3, label: "Fort", color: "bg-lime-500" },
    { score: 4, label: "Très fort", color: "bg-status-success" },
  ];

  return strengthLevels[score];
}
