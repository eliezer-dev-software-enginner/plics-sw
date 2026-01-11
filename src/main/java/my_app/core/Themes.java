package my_app.core;


import megalodonte.theme.*;

public class Themes {
    public static final Theme DARK = new Theme() {
        @Override
        public ThemeColors colors() {
return new ThemeColors(
                "#1e293b",                    // background
                "#1a2235",                    // surface
                "#2563eb",                    // primary
                "#334155",                    // secondary
                "#ffffff",                    // text primary
                "#94a3b8",                    // text secondary
                "#334155",                    // border,
                
                // Button colors
                "#2563eb",                    // buttonPrimary
                "#4b5563",                    // buttonSecondary (darker secondary for contrast)
                "#10b981",                    // buttonSuccess
                "#f59e0b",                    // buttonWarning
                "#ef4444",                    // buttonDanger
                "rgba(255,255,255,0.1)",   // buttonGhost (subtle white)
                "#475569"                     // buttonDisabled
            );
        }

        @Override
        public ThemeTypography typography() {
            return new ThemeTypography(35, 20, 16, 13);
        }

        @Override
        public ThemeSpacing spacing() {
            return new ThemeSpacing(4, 8, 12, 20, 32);
        }

        @Override
        public ThemeRadius radius() {
            return new ThemeRadius(4, 8, 12);
        }

        @Override
        public ThemeBorder border() {
            return new ThemeBorder(1);
        }
    };

    public static final Theme LIGHT = new Theme() {
        @Override
        public ThemeColors colors() {
            return new ThemeColors(
                "#f8fafc",                    // background: Branco levemente azulado
                "#ffffff",                    // surface: Branco puro para Cards e tabelas
                "#2563eb",                    // primary: Mantém azul vibrante para botões
                "#e2e8f0",                    // secondary: Cinza claro para elementos secundários
                "#0f172a",                    // text primary: Azul quase preto para máximo contraste
                "#94a3b8",                    // text secondary: Cinza médio para textos de apoio
                "#e2e8f0",                    // border: Cinza suave para divisórias e bordas,
                
                // Button colors
                "#2563eb",                    // buttonPrimary (same as primary)
                "#64748b",                    // buttonSecondary (darker for better contrast)
                "#10b981",                    // buttonSuccess
                "#f59e0b",                    // buttonWarning
                "#ef4444",                    // buttonDanger
                "rgba(0,0,0,0.05)",        // buttonGhost (subtle black)
                "#9ca3af"                     // buttonDisabled
            );
        }

        @Override
        public ThemeTypography typography() {
            return new ThemeTypography(35, 20, 16, 13);
        }

        @Override
        public ThemeSpacing spacing() {
            return new ThemeSpacing(4, 8, 12, 20, 32);
        }

        @Override
        public ThemeRadius radius() {
            return new ThemeRadius(6, 10, 14); // Bordas um pouco mais arredondadas
        }

        @Override
        public ThemeBorder border() {
            return new ThemeBorder(1);
        }
    };
}