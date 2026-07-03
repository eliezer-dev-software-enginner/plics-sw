package my_app.core;


import megalodonte.base.theme.*;

public class Themes {
    public static final ThemeInterface DARK = new ThemeInterface() {
        @Override
        public ThemeColors colors() {
return new ThemeColors(
                "#1e293b",                    // background
                "#1a2235",                    // surface
                "#fff",                    // primary
                "#334155",                    // secondary
                "#ffffff",                    // text primary
                "#94a3b8",                    // text secondary
                "#334155",                    // border,
        "#A9A9A9"
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
        public ThemeBorder border() {
            return new ThemeBorder(1,4,8,12);
        }
    };

    public static final ThemeInterface LIGHT = new ThemeInterface() {
        @Override
        public ThemeColors colors() {
            return new ThemeColors(
                "#f8fafc",                    // background: Branco levemente azulado
                "#ffffff",                    // surface: Branco puro para Cards e tabelas
                "#2563eb",                    // primary: Mantém azul vibrante para botões
                "#e2e8f0",                    // secondary: Cinza claro para elementos secundários
                "#0f172a",                    // text primary: Azul quase preto para máximo contraste
                "#94a3b8",                    // text secondary: Cinza médio para textos de apoio
                "#1B2432",                   // border: Cinza suave para divisórias e bordas,
                "#A9A9A9"

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
        public ThemeBorder border() {
            return new ThemeBorder(1,4,8,12);
        }
    };
}