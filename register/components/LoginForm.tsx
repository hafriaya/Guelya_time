"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { useToast } from "@/components/ui/Toast";
import { validationRules } from "@/lib/validations";
import { authApi, getApiErrorMessage } from "@/lib/api";
import { LoginRequest } from "@/types/user";

export function LoginForm() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const { showToast, ToastContainer } = useToast();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>({
    mode: "onChange",
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: LoginRequest) => {
    setIsLoading(true);

    try {
      await authApi.login(data);

      showToast("Connexion réussie ! Bienvenue sur Guelya Time", "success");

      // Redirection vers le dashboard
      setTimeout(() => {
        router.push("/dashboard");
      }, 1000);
    } catch (error) {
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
            Connexion à Guelya Time
          </h1>
          <p className="text-text-secondary mt-2">
            Retrouvez vos recommandations de films personnalisées
          </p>
        </div>

        {/* Formulaire */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
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

          {/* Mot de passe */}
          <div>
            <Input
              label="Mot de passe"
              type="password"
              placeholder="••••••••"
              autoComplete="current-password"
              showPasswordToggle
              required
              error={errors.password?.message}
              {...register("password", {
                required: "Le mot de passe est requis",
              })}
            />
            <div className="mt-2 text-right">
              <Link
                href="/forgot-password"
                className="text-sm text-accent-primary hover:text-accent-hover transition-colors"
              >
                Mot de passe oublié ?
              </Link>
            </div>
          </div>

          {/* Bouton de soumission */}
          <Button
            type="submit"
            fullWidth
            isLoading={isLoading}
            className="mt-6"
          >
            Se connecter
          </Button>
        </form>

        {/* Séparateur */}
        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-border" />
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-4 bg-bg-secondary text-text-muted">ou</span>
          </div>
        </div>

        {/* Connexion sociale (optionnel) */}
        <div className="space-y-3">
          <button
            type="button"
            className="w-full flex items-center justify-center gap-3 px-4 py-2.5 border border-border rounded-lg hover:bg-bg-tertiary transition-colors"
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            <span className="text-text-primary font-medium">
              Continuer avec Google
            </span>
          </button>
        </div>

        {/* Lien vers inscription */}
        <div className="mt-6 text-center">
          <p className="text-text-secondary">
            Pas encore de compte ?{" "}
            <Link
              href="/register"
              className="font-medium text-accent-primary hover:text-accent-hover transition-colors"
            >
              S&apos;inscrire
            </Link>
          </p>
        </div>
      </div>
    </>
  );
}
