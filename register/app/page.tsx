import Link from "next/link";

export default function HomePage() {
  return (
    <main className="min-h-screen flex items-center justify-center bg-bg-primary px-4">
      {/* Décoration d'arrière-plan */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-accent-primary rounded-full opacity-10 blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-accent-secondary rounded-full opacity-10 blur-3xl" />
      </div>

      <div className="relative text-center">
        {/* Logo */}
        <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-accent-primary to-accent-secondary rounded-3xl shadow-2xl shadow-accent-primary/30 mb-8">
          <svg
            className="w-10 h-10 text-white"
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

        {/* Titre */}
        <h1 className="text-5xl font-bold text-text-primary mb-4">
          <span className="text-gradient">Guelya Time</span>
        </h1>

        {/* Description */}
        <p className="text-xl text-text-secondary max-w-md mx-auto mb-8">
          Découvrez des films personnalisés selon vos goûts grâce à notre
          système de recommandation intelligent.
        </p>

        {/* Boutons d'action */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href="/register"
            className="inline-flex items-center justify-center px-8 py-3 bg-gradient-to-r from-accent-primary to-accent-secondary text-white font-medium rounded-xl shadow-lg hover:from-accent-hover hover:to-accent-primary transition-all duration-200 transform hover:scale-105 glow-purple"
          >
            Commencer gratuitement
          </Link>
          <Link
            href="/login"
            className="inline-flex items-center justify-center px-8 py-3 border-2 border-accent-primary text-accent-primary font-medium rounded-xl hover:bg-accent-light transition-all duration-200"
          >
            Se connecter
          </Link>
        </div>

        {/* Features rapides */}
        <div className="mt-16 grid grid-cols-1 sm:grid-cols-3 gap-8 max-w-3xl mx-auto">
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-accent-light text-accent-primary rounded-xl mb-3">
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-text-primary">IA Intelligente</h3>
            <p className="text-sm text-text-secondary mt-1">
              Recommandations basées sur vos préférences
            </p>
          </div>

          <div className="text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-accent-light text-accent-primary rounded-xl mb-3">
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-text-primary">Watchlist</h3>
            <p className="text-sm text-text-secondary mt-1">
              Sauvegardez vos films préférés
            </p>
          </div>

          <div className="text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-accent-light text-accent-primary rounded-xl mb-3">
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-text-primary">Communauté</h3>
            <p className="text-sm text-text-secondary mt-1">
              Partagez avec vos amis
            </p>
          </div>
        </div>
      </div>
    </main>
  );
}
