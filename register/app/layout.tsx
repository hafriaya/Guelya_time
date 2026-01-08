import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Guelya Time - Recommandation de Films",
  description:
    "Découvrez des films personnalisés selon vos goûts avec Guelya Time, votre assistant de recommandation de films intelligent.",
  keywords: ["films", "recommandation", "cinéma", "streaming", "watchlist"],
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fr">
      <body className={inter.className}>{children}</body>
    </html>
  );
}
