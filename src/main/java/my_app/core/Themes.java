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
}
