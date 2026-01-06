package my_app.core;

import megalodonte.theme.*;

public class Themes {
    public static final Theme DARK = new Theme() {
        @Override
        public ThemeColors colors() {
            return new ThemeColors(
                    "#1e293b", // background //"#1e293b"
                    "#1a2235", // surface  //"#1a2235"
                    "#2563eb", // primary
                    "#334155", // secondary
                    "#ffffff", // text primary
                    "#94a3b8", // text secondary
                    "#334155"  // border
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
                    "#f8fafc", // background: Branco levemente azulado (mais suave que #ffffff)
                    "#ffffff", // surface: Branco puro para os Cards e tabelas se destacarem
                    "#2563eb", // primary: Mantemos o seu azul vibrante para botões
                    "#e2e8f0", // secondary: Cinza claro para elementos secundários
                    "#0f172a", // text primary: Azul quase preto para máximo contraste
                    "#64748b", // text secondary: Cinza médio para textos de apoio
                    "#e2e8f0"  // border: Cinza suave para divisórias e bordas
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
            return new ThemeRadius(6, 10, 14); // Bordas um pouco mais arredondadas combinam com temas claros
        }

        @Override
        public ThemeBorder border() {
            return new ThemeBorder(1);
        }
    };
}
