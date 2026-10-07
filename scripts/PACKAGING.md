# Empacotamento compartilhado

A implementação está na biblioteca `megalodonte-packaging`, compartilhada por todos os clientes.
`packaging.toml` contém apenas os dados específicos deste aplicativo. A versão vem de
`gradle.properties` quando presente; a configuração não copia os algoritmos da biblioteca.

```sh
python scripts/package.py show
python scripts/create-msi.py --dry-run --platform windows
python scripts/create-deb.py --dry-run --platform linux
python scripts/create-msi.py
python scripts/create-deb.py
```

No workspace, o adaptador encontra automaticamente a mesma biblioteca fonte. Fora dele,
instale-a com `python -m pip install -e CAMINHO/megalodonte-packaging` ou instale seu wheel.
Use Python 3.11+, JDK completo e o SDK JavaFX configurado. MSI exige Windows/WiX;
DEB exige Linux e as ferramentas do jpackage. Não há empacotamento cruzado.

`--smoke-test` abre o aplicativo brevemente; `--open` abre a pasta de saída.
Flatpak não instala automaticamente: acrescente `--install-flatpak` para instalar.
O perfil updater exige que a classe correspondente exista no JAR; o perfil store
apenas acrescenta as opções configuradas para a Microsoft Store.

Veja o README da biblioteca para API Python, configuração completa, CI e versões MSI.
