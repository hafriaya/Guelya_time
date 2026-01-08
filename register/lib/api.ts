import axios, { AxiosError } from "axios";
import {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  ApiError,
} from "@/types/user";

// Configuration de base d'Axios
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081/api",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

// Intercepteur pour ajouter le token d'authentification
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercepteur pour gérer les erreurs
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// Services d'authentification
export const authApi = {
  /**
   * Inscription d'un nouvel utilisateur
   */
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>("/auth/register", data);
    return response.data;
  },

  /**
   * Connexion d'un utilisateur
   */
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>("/auth/login", data);
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
    }
    return response.data;
  },

  /**
   * Déconnexion
   */
  logout: () => {
    localStorage.removeItem("token");
  },

  /**
   * Vérifier si l'utilisateur est authentifié
   */
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem("token");
  },
};

// Helper pour extraire les messages d'erreur de l'API
export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }
    if (axiosError.response?.status === 400) {
      return "Données invalides. Veuillez vérifier vos informations.";
    }
    if (axiosError.response?.status === 409) {
      return "Ce nom d'utilisateur ou cet email existe déjà.";
    }
    if (axiosError.response?.status === 500) {
      return "Erreur serveur. Veuillez réessayer plus tard.";
    }
    if (axiosError.code === "ECONNABORTED") {
      return "La requête a expiré. Veuillez réessayer.";
    }
    if (!axiosError.response) {
      return "Impossible de se connecter au serveur.";
    }
  }
  return "Une erreur inattendue s'est produite.";
}

// Helper pour extraire les erreurs de validation par champ
export function getFieldErrors(
  error: unknown
): Record<string, string> | undefined {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    return axiosError.response?.data?.errors;
  }
  return undefined;
}

export default api;
