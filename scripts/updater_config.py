from pathlib import Path
import os

from config import APP_NAME

ROOT = Path(__file__).resolve().parent.parent

UPDATER_NAME = f"{APP_NAME} Updater"
UPDATER_MAIN_CLASS = "my_app.updater.Main"
