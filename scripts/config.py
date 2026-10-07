"""Application configuration; use package.py for packaging commands."""
from package import ROOT, load_shared_library

load_shared_library()
from megalodonte_packaging import load_config
CONFIG = load_config(ROOT)
APP_NAME = CONFIG.name
APP_VERSION = CONFIG.version
MAIN_CLASS = CONFIG.main_class
