# Plics SW configuration audit

Reviewed on 16 October 2026. This document names locations and configuration contracts without copying credentials.

| Location | Value class | Current treatment | Action |
| --- | --- | --- | --- |
| `core/telegram/TelegramConfig.java` and `TelegramNotifierFactory.java` | Telegram bot token and destination chat | Read at runtime from `PLICS_TELEGRAM_BOT_TOKEN` and `PLICS_TELEGRAM_CHAT_ID`; absent values disable delivery | Configure only on machines authorized to send diagnostics; rotate the old bot token because an earlier revision contained recoverable ciphertext and its fixed key |
| `core/Data.java` | Public support number and website base URL | Override with `PLICS_SUPPORT_WHATSAPP` and `PLICS_WEBSITE_URL`; public defaults remain | Use repository/organization variables for CI display metadata if needed; these are not secrets |
| `flyway_migrations/V16__dados_padrao.sql` | Initial administrator credentials | Existing public first-install default remains for compatibility; sign-out no longer resets a changed password to that default | Design a first-run credential setup and a safe migration of existing installations. A build-time environment variable would embed the credential into every distributed installer. |
| `packaging.toml` | App name, version, icons and JVM options | Versioned metadata | Keep public; never add bot tokens or passwords. |

The release workflow does not receive Telegram credentials. These are runtime inputs, not build inputs. Existing Git history is immutable in normal operation; deleting a literal from the current source does not revoke a previously exposed credential. This audit intentionally does not print or decrypt old values.
