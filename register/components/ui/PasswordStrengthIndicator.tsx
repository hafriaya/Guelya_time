"use client";

import { calculatePasswordStrength } from "@/lib/validations";

interface PasswordStrengthIndicatorProps {
  password: string;
}

export function PasswordStrengthIndicator({
  password,
}: PasswordStrengthIndicatorProps) {
  const strength = calculatePasswordStrength(password);

  return (
    <div className="mt-2">
      {/* Barre de progression */}
      <div className="flex gap-1 mb-1">
        {[0, 1, 2, 3].map((index) => (
          <div
            key={index}
            className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
              index < strength.score ? strength.color : "bg-border"
            }`}
          />
        ))}
      </div>

      {/* Label de force */}
      {password && (
        <p
          className={`text-xs transition-all duration-300 ${
            strength.score < 2
              ? "text-status-error"
              : strength.score < 3
              ? "text-status-warning"
              : "text-status-success"
          }`}
        >
          Force du mot de passe : {strength.label}
        </p>
      )}

      {/* Critères de validation */}
      {password && strength.score < 3 && (
        <ul className="mt-2 space-y-1">
          <PasswordCriterion
            met={password.length >= 8}
            text="Au moins 8 caractères"
          />
          <PasswordCriterion
            met={/[A-Z]/.test(password)}
            text="Une lettre majuscule"
          />
          <PasswordCriterion
            met={/[a-z]/.test(password)}
            text="Une lettre minuscule"
          />
          <PasswordCriterion met={/[0-9]/.test(password)} text="Un chiffre" />
        </ul>
      )}
    </div>
  );
}

interface PasswordCriterionProps {
  met: boolean;
  text: string;
}

function PasswordCriterion({ met, text }: PasswordCriterionProps) {
  return (
    <li className="flex items-center gap-2 text-xs text-text-secondary">
      {met ? (
        <svg
          className="w-3.5 h-3.5 text-status-success"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M5 13l4 4L19 7"
          />
        </svg>
      ) : (
        <svg
          className="w-3.5 h-3.5 text-text-muted"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      )}
      <span className={met ? "text-green-600" : "text-gray-500"}>{text}</span>
    </li>
  );
}
