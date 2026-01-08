"use client";

import { ButtonHTMLAttributes, forwardRef } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "ghost" | "danger";
  isLoading?: boolean;
  fullWidth?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      variant = "primary",
      isLoading,
      fullWidth,
      className,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseStyles =
      "relative px-6 py-2.5 rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-bg-primary disabled:cursor-not-allowed";

    const variants = {
      primary:
        "bg-gradient-to-r from-accent-primary to-accent-secondary text-white hover:from-accent-hover hover:to-accent-primary focus:ring-accent-primary disabled:from-accent-primary/50 disabled:to-accent-secondary/50 disabled:text-text-muted shadow-lg hover:shadow-accent-primary/40",
      secondary:
        "bg-bg-tertiary text-text-primary hover:bg-border focus:ring-accent-primary disabled:bg-bg-secondary disabled:text-text-muted",
      outline:
        "border-2 border-accent-primary text-accent-primary hover:bg-accent-light focus:ring-accent-primary disabled:border-accent-primary/30 disabled:text-accent-primary/30",
      ghost:
        "bg-transparent text-text-secondary hover:bg-accent-light hover:text-text-primary focus:ring-accent-primary",
      danger:
        "bg-status-error text-white hover:bg-red-400 focus:ring-status-error disabled:bg-status-error/50",
    };

    return (
      <button
        ref={ref}
        className={`
          ${baseStyles}
          ${variants[variant]}
          ${fullWidth ? "w-full" : ""}
          ${className || ""}
        `}
        disabled={disabled || isLoading}
        {...props}
      >
        {isLoading && (
          <span className="absolute inset-0 flex items-center justify-center">
            <svg
              className="animate-spin h-5 w-5"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
          </span>
        )}
        <span className={isLoading ? "invisible" : ""}>{children}</span>
      </button>
    );
  }
);

Button.displayName = "Button";
