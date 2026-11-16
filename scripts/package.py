#!/usr/bin/env python3
"""Thin command adapter; the packaging implementation lives in the shared library."""
from pathlib import Path
import os
import sys

ROOT = Path(__file__).resolve().parent.parent


def load_shared_library():
    override = os.environ.get("MEGALODONTE_PACKAGING_HOME")
    if override:
        candidates = [Path(override).resolve() / "src"]
    else:
        candidates = [candidate for parent in (ROOT, *ROOT.parents)
                      for candidate in (parent / "framework/megalodonte-packaging/src",)]
    source = next((p for p in candidates if (p / "megalodonte_packaging").is_dir()), None)
    if override and source is None:
        raise SystemExit("MEGALODONTE_PACKAGING_HOME does not contain the shared library")
    if source is not None:
        sys.path.insert(0, str(source))
    try:
        from megalodonte_packaging.cli import main as cli
    except ModuleNotFoundError as error:
        if error.name != "megalodonte_packaging":
            raise
        raise SystemExit("Install the shared library: python -m pip install -e PATH/TO/megalodonte-packaging") from None
    return cli


def main(arguments=None):
    return load_shared_library()(["--project", str(ROOT), *(sys.argv[1:] if arguments is None else arguments)])


if __name__ == "__main__":
    raise SystemExit(main())
