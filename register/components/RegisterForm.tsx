"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { PasswordStrengthIndicator } from "@/components/ui/PasswordStrengthIndicator";
import { useToast } from "@/components/ui/Toast";
import { validationRules } from "@/lib/validations";
import { authApi, getApiErrorMessage, getFieldErrors } from "@/lib/api";
import { RegisterRequest } from "@/types/user";

interface RegisterFormData extends RegisterRequest {
  confirmPassword: string;
}

export function RegisterForm() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const { showToast, ToastContainer } = useToast();

  const {
    register,
    handleSubmit,
    watch,
    setError,
    formState: { errors },
  } = useForm<RegisterFormData>({
    mode: "onChange",
    defaultValues: {
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
      firstName: "",
      lastName: "",
    },
  });

  const password = watch("password");

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);

    try {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { confirmPassword, ...registerData } = data;
      
      await authApi.register(registerData);

      showToast(
        "Inscription réussie ! Redirection vers la connexion...",
        "success"
      );

      // Redirection après un court délai pour voir le message
      setTimeout(() => {
        router.push("/login");
      }, 2000);
    } catch (error) {
      // Gérer les erreurs de validation du serveur
      const fieldErrors = getFieldErrors(error);
      if (fieldErrors) {
        Object.entries(fieldErrors).forEach(([field, message]) => {
          setError(field as keyof RegisterFormData, {
            type: "server",
            message,
          });
        });
      }

      showToast(getApiErrorMessage(error), "error");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <ToastContainer />
      <div className="w-full max-w-md mx-auto">
        {/* Logo et titre */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-accent-primary to-accent-secondary rounded-2xl shadow-lg shadow-accent-primary/30 mb-4">
            <svg
              className="w-8 h-8 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z"
              />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-text-primary">
            Rejoindre Guelya Time
          </h1>
          <p className="text-text-secondary mt-2">
            Créez votre compte et découvrez des films faits pour vous
          </p>
        </div>

        {/* Formulaire */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          {/* Nom d'utilisateur */}
          <Input
            label="Nom d'utilisateur"
            placeholder="john_doe"
            autoComplete="username"
            required
            error={errors.username?.message}
            {...register("username", validationRules.username)}
          />

          {/* Email */}
          <Input
            label="Email"
            type="email"
            placeholder="vous@exemple.com"
            autoComplete="email"
            required
            error={errors.email?.message}
            {...register("email", validationRules.email)}
          />

          {/* Prénom et Nom */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Prénom"
              placeholder="Jean"
              autoComplete="given-name"
              error={errors.firstName?.message}
              {...register("firstName", validationRules.firstName)}
            />
            <Input
              label="Nom"
              placeholder="Dupont"
              autoComplete="family-name"
              error={errors.lastName?.message}
              {...register("lastName", validationRules.lastName)}
            />
          </div>

          {/* Mot de passe */}
          <div>
            <Input
              label="Mot de passe"
              type="password"
              placeholder="••••••••"
              autoComplete="new-password"
              showPasswordToggle
              required
              error={errors.password?.message}
              {...register("password", validationRules.password)}
            />
            <PasswordStrengthIndicator password={password || ""} />
          </div>

          {/* Confirmation mot de passe */}
          <Input
            label="Confirmer le mot de passe"
            type="password"
            placeholder="••••••••"
            autoComplete="new-password"
            showPasswordToggle
            required
            error={errors.confirmPassword?.message}
            {...register("confirmPassword", {
              required: "Veuillez confirmer votre mot de passe",
              validate: (value) =>
                value === password || "Les mots de passe ne correspondent pas",
            })}
          />

          {/* Bouton de soumission */}
          <Button
            type="submit"
            fullWidth
            isLoading={isLoading}
            className="mt-6"
          >
            Créer mon compte
          </Button>
        </form>

        {/* Lien vers connexion */}
        <div className="mt-6 text-center">
          <p className="text-text-secondary">
            Déjà un compte ?{" "}
            <Link
              href="/login"
              className="font-medium text-accent-primary hover:text-accent-hover transition-colors"
            >
              Se connecter
            </Link>
          </p>
        </div>

        {/* Conditions d'utilisation */}
        <p className="mt-6 text-center text-xs text-text-muted">
          En créant un compte, vous acceptez nos{" "}
          <Link href="/terms" className="text-accent-primary hover:underline">
            Conditions d&apos;utilisation
          </Link>{" "}
          et notre{" "}
          <Link href="/privacy" className="text-accent-primary hover:underline">
            Politique de confidentialité
          </Link>
        </p>
      </div>
    </>
  );
}
