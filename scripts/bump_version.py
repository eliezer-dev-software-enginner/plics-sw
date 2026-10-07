from package import main
import sys

if __name__ == "__main__":
    raise SystemExit(main(["version", *sys.argv[1:]]))
