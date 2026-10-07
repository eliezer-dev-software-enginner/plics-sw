#!/usr/bin/env python3
"""Compatibility command backed by megalodonte-packaging."""
import sys
from package import main

if __name__ == "__main__":
    raise SystemExit(main(['build', 'flatpak'] + sys.argv[1:]))
