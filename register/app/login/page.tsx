import { Metadata } from "next";
import { LoginForm } from "@/components/LoginForm";

export const metadata: Metadata = {
  title: "Connexion | Guelya Time",
  description:
    "Connectez-vous à Guelya Time pour accéder à vos recommandations de films personnalisées.",
};

export default function LoginPage() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-bg-primary px-4 py-12">
      {/* Décoration d'arrière-plan */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-accent-primary rounded-full opacity-10 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-accent-secondary rounded-full opacity-10 blur-3xl" />
      </div>

      {/* Carte du formulaire */}
      <div className="relative w-full max-w-md">
        <div className="bg-bg-secondary/90 backdrop-blur-sm shadow-xl shadow-black/30 rounded-2xl p-8 border border-border">
          <LoginForm />
        </div>
      </div>
    </main>
  );
}
