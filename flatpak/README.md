# Publicando o Plics SW no Flathub

Este documento é sobre **publicar de verdade** na loja "Software" (GNOME
Software/Flathub) — pra só buildar e testar localmente, veja a seção "Flatpak (teste
local)" no [README.md](../README.md) principal.

Publicar exige uma Pull Request manual na conta do Flathub no GitHub e passar pela
revisão deles — isso é uma ação da sua conta, ninguém automatiza isso por você. Este
documento cobre o que precisa estar pronto **antes** de abrir esse PR.

## ⚠️ Bloqueio atual: a fonte do manifest não é reproduzível

O manifest hoje (`io.github.eliezerdevsoftwareenginner.PlicsSW.yml`) usa isto:

```yaml
sources:
  - type: dir
    path: app-image
```

Ou seja: ele empacota o que já está numa pasta local no seu disco (gerada pelo
`create-flatpak.py` rodando `jpackage` fora do sandbox). Isso funciona pra testar
localmente, mas a revisão do Flathub **rejeita** isso — o build deles roda numa
máquina limpa, sem acesso ao seu disco, e precisa buscar cada fonte de um lugar
público e verificável (URL + checksum, ou um repositório git).

**Antes de submeter, a fonte precisa virar uma destas duas opções:**

### Opção A — publicar um artefato pré-buildado (mais simples, combina com o fluxo que vocês já têm)

Já existe um pipeline de releases no GitHub (`UpdaterService` baixa `.msi`/`.deb` de
lá). É só estender: publicar também um `.tar.gz` do app-image do Linux
(`create-flatpak.py` já gera isso em `dist/Plics SW/` antes do passo do
flatpak-builder) como asset da release, e trocar a fonte do manifest pra:

```yaml
sources:
  - type: archive
    url: https://github.com/<usuario>/plics-sw/releases/download/v1.1.0.1/plics-sw-linux.tar.gz
    sha256: <sha256 do arquivo>
```

Esse é o mesmo padrão que apps de código fechado usam no Flathub (Chrome, Discord,
Slack — todos bundlam um binário pré-buildado baixado de uma URL fixa, em vez de
compilar dentro do sandbox). Faz sentido pro Plics SW pelo mesmo motivo.

### Opção B — buildar de verdade dentro do sandbox (mais trabalho, mas é o "padrão ouro" do Flathub)

Precisaria de uma extensão de JDK do `org.freedesktop.Sdk` (existe uma pra Java —
`org.freedesktop.Sdk.Extension.openjdk`), rodar `./gradlew shadowJar` como
`build-commands`, e resolver as dependências do Gradle de um jeito reproduzível
(Flathub exige que builds não baixem nada da internet durante o build — isso significa
vendorizar/pré-baixar todas as dependências Maven/Gradle, o que dá mais trabalho de
configurar). Não montei isso — se quiser seguir por aqui, é um passo à parte.

**Recomendo a Opção A** — é a que menos muda do que já existe.

## Checklist antes de abrir o PR

- [ ] **Fonte reproduzível** (ver acima — bloqueio principal).
- [ ] **Licença definida**: o `metainfo.xml` hoje tem `LicenseRef-proprietary` como
      placeholder. Confirme se é isso mesmo (Flathub aceita apps de código fechado —
      tem vários instalados até nesta máquina: AnyDesk, Chrome, Opera — mas precisa
      declarar corretamente).
- [ ] **Screenshots**: o Flathub exige pelo menos 1 screenshot no `metainfo.xml`
      (tag `<screenshots>`), pra aparecer na página da loja. Não tem nenhum ainda.
      Imagens hospedadas publicamente (ex: no próprio repo do GitHub, via raw URL).
- [ ] **Revisar as permissões do `finish-args`**: `--filesystem=home` e
      `--device=all` são amplas — a revisão do Flathub costuma pedir pra justificar
      ou reduzir. Ver comentários no próprio `.yml` sobre como reduzir o
      `--filesystem=home` (migrar `DB.resolveDbPath()` pra `~/.local/share/plics-sw`).
- [ ] **Repositório público**: o manifest (e o app, se for código aberto) precisa
      estar num repositório git público — é de lá que o Flathub builda.
- [ ] **Build local passando**: `python3 scripts/create-flatpak.py` sem erro
      (já confirmado ✅).

## Fluxo de submissão (visão geral)

A Flathub muda os detalhes exatos de tempos em tempos — a referência oficial e
sempre atualizada é **https://docs.flathub.org/docs/for-app-authors/submission**.
Em linhas gerais, hoje o processo é:

1. Manifest pronto (com fonte reproduzível, ver acima) num repositório git público.
2. Abrir uma Pull Request na conta do Flathub no GitHub com esse manifest — o
   processo exato de "onde" está descrito no link acima (eles têm um app de
   submissão que cria um repositório dedicado `github.com/flathub/<app-id>` pra
   cada app).
3. O sistema de CI deles (buildbot) tenta buildar o manifest numa máquina limpa.
   Se falhar, corrige e atualiza o PR.
4. Revisão manual por um mantenedor do Flathub — eles conferem metadados,
   permissões do sandbox, ícone, etc. Pode pedir ajustes.
5. Aprovado e mergeado → o app fica disponível (geralmente primeiro numa faixa
   de teste, depois promovido pro canal estável).

## Depois de publicado

Cada nova versão exige atualizar a fonte do manifest (nova URL + novo sha256 da
Opção A, ou nova tag/commit da Opção B) e abrir um PR no repositório dedicado do app
(`github.com/flathub/io.github.eliezerdevsoftwareenginner.PlicsSW`, criado durante a
submissão). Uma vez aprovado o PR de atualização, quem já tem o app instalado recebe
a atualização via `flatpak update` normalmente.
